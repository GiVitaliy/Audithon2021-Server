package ru.audithon.egissostat.infrastructure.mass.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.audithon.common.helpers.ObjectUtils;
import ru.audithon.common.telemetry.TelemetryServiceCore;
import ru.audithon.egissostat.infrastructure.mass.dao.JobDao;
import ru.audithon.egissostat.infrastructure.mass.dao.JobScheduleItemDao;
import ru.audithon.egissostat.infrastructure.mass.domain.*;
import ru.audithon.egissostat.infrastructure.mass.helpers.JobParametersHelper;
import ru.audithon.egissostat.jobs.JobParameters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@EnableAsync
public class JobBackgroundScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobBackgroundScheduler.class);

    @Value("${job-background-scheduler.pool-size}")
    private Integer poolSize;
    @Value("${job-background-scheduler.enabled}")
    private Boolean pollingEnabled;

    private final Map<String, JobKey> runningJobs = new HashMap<>();

    private final JobExecutionService jobExecutionService;
    private final JobScheduleItemDao jobScheduleItemDao;
    private final JobDao jobDao;
    private final TelemetryServiceCore telemetryServiceCore;

    @Autowired
    public JobBackgroundScheduler(JobExecutionService jobExecutionService,
                                  JobScheduleItemDao jobScheduleItemDao,
                                  JobDao jobDao,
                                  TelemetryServiceCore telemetryServiceCore) {
        this.jobExecutionService = jobExecutionService;
        this.jobScheduleItemDao = jobScheduleItemDao;
        this.jobDao = jobDao;
        this.telemetryServiceCore = telemetryServiceCore;
    }

    @Scheduled(fixedDelayString = "${job-background-scheduler.polling-ms}")
    public void schedule() {

        if (!pollingEnabled) {
            return;
        }

        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation("@Scheduled::JobBackgroundScheduler::schedule");
        try {

            synchronized (runningJobs) {

                updateRunningJobsState();

                runNextJobs();
            }
        } finally {
            telemetryServiceCore.exitOperation(optoken);
        }
    }

    private void updateRunningJobsState() {
        /*
         * Проверяем список нами же запущенных задач и удаляем те, которые уже завершены (неважно с каким результатом)
         * */
        Map<String, JobKey> runningJobsClone = new HashMap<>(runningJobs);

        for (String runningJobDigest : runningJobsClone.keySet()) {
            JobKey runningJobKey = runningJobsClone.get(runningJobDigest);
            JobProgress progress = jobExecutionService.getJobProgress(runningJobKey).orElse(null);

            if (progress == null || !ObjectUtils.equalsSome(progress.getState(), JobState.RUNNING, JobState.NEW)) {
                runningJobs.remove(runningJobDigest);
            }
        }
    }

    private void runNextJobs() {

        List<JobScheduleItem> scheduleItems = jobScheduleItemDao.all();

        /*
         * Просто пытаемся надобавлять задач до размера пула, а если задач для добавления не найдено - вываливаемся
         * */
        while (runningJobs.size() < poolSize) {
            if (!tryRunNextJob(scheduleItems)) {
                break;
            }
        }
    }

    private boolean tryRunNextJob(List<JobScheduleItem> scheduleItems) {
        /*
         * Тут мы перебираем незапущенные задачи за последние 30 дней (для надежности, потому что они могли не успеть
         * запуститься в отведенный временной интервал строго по расписанию, но запустить мы их все равно должны).
         * Запущена или нет задача - это мы выясняем по digest'у задачи - найден digest, занчит задача запускалась, при этом
         * даже неважно - пользователем вручную или планировщиком. Главное, чтобы это происходило позже планируемой даты,
         * а то так запустят с дуру заблаговременно на год вперед какой-нибудь отчет, а он пустой - так и не пересчитается
         * */
        LocalDate dateNow = LocalDate.now();

        for (JobScheduleItem scheduleItem : scheduleItems) {

            try {

                // Идем от самой ранней даты, чтобы не запускать новые задачи, если не все старые еще выполнены
                for (int iDay = 30; iDay >= 0; iDay--) {
                    LocalDate dateX = dateNow.minusDays(iDay);

                    // Запускаем только включенные элементы расписания и только в те часы, которые разрешены для элемента расписания.
                    //Если будет слишком много задач для запуска - запустится просто на следующий день в тот же разрешенный период времени
                    if (!scheduleItem.getEnabled()
                        || LocalDateTime.now().getHour() < scheduleItem.getAllowedHourFrom()
                        || LocalDateTime.now().getHour() >= scheduleItem.getAllowedHourToExcluded()) {
                        continue;
                    }

                    if (Objects.equals(scheduleItem.getJobPeriodicity(), JobScheduleItemPeriodicity.WEEKLY)
                        && !Objects.equals(dateX.getDayOfWeek().getValue(), scheduleItem.getPlannedDay())) {
                        continue;
                    }

                    if (Objects.equals(scheduleItem.getJobPeriodicity(), JobScheduleItemPeriodicity.MONTHLY)
                        && !Objects.equals(dateX.getDayOfMonth(), scheduleItem.getPlannedDay())) {
                        continue;
                    }

                    if (Objects.equals(scheduleItem.getJobPeriodicity(), JobScheduleItemPeriodicity.QURTERLY)
                        && (!ObjectUtils.equalsSome(dateX.getMonthValue(), 1, 4, 7, 10)
                        || !Objects.equals(dateX.getDayOfMonth(), scheduleItem.getPlannedDay()))) {
                        continue;
                    }

                    if (Objects.equals(scheduleItem.getJobPeriodicity(), JobScheduleItemPeriodicity.YEARLY)
                        && (!Objects.equals(dateX.getMonthValue(), 1)
                        || !Objects.equals(dateX.getDayOfMonth(), scheduleItem.getPlannedDay()))) {
                        continue;
                    }

                    // для каждого элемента расписания может быть сгенерировано несколько задач для запуска со своими параметрами,
                    // например, если мы в качестве значения параметра выберем список учреждений. Соответственно, отдельно запускаем
                    // каждую задачу
                    for (JobParameters jobParameters : JobParametersHelper.buildJobStartingParameters(dateX, scheduleItem.getParameters())) {
                        // проверим, выполнена ли уже задача или может быть уже запущена и выполняется прямо сейчас
                        String digest = JobParametersHelper.buildParamsDigest(jobParameters);
                        if (runningJobs.containsKey(digest)) {
                            continue;
                        }

                        List<Job> completedJobs = jobDao.byDigestLatest10(digest);

                        // Если задачу не запускали с такими параметрами или запускали до планового времени запуска, то запускаем её
                        if (completedJobs.size() == 0 || completedJobs.get(0).getCreated().isBefore(LocalDateTime.of(dateX, LocalTime.MIN))) {
                            JobKey newJobKey = jobExecutionService.startJobUnsecured(jobParameters);
                            runningJobs.put(digest, newJobKey);

                            // Предупреждение в лог, если планировщик не успевает выполнять задачи вовремя
                            if (iDay > 2) {
                                logger.warn("Обнаружен запуск планируемой задачи на 2 дня и более отстающий от графика, ид задачи - " + newJobKey.toString());
                            }

                            // выходим с успехом как только нашли и стартанули следующую подходящую задачу
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при обработке элемента расписания фонового запуска регламентных операций Id = " + scheduleItem.getId());
                logger.error(String.format("%s\n%s", e.getMessage(), ExceptionUtils.getStackTrace(e)));
            }
        }

        return false;
    }
}
