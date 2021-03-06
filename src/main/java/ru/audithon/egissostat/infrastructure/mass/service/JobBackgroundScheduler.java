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
         * ?????????????????? ???????????? ???????? ???? ???????????????????? ?????????? ?? ?????????????? ????, ?????????????? ?????? ?????????????????? (?????????????? ?? ?????????? ??????????????????????)
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
         * ???????????? ???????????????? ?????????????????????? ?????????? ???? ?????????????? ????????, ?? ???????? ?????????? ?????? ???????????????????? ???? ?????????????? - ????????????????????????
         * */
        while (runningJobs.size() < poolSize) {
            if (!tryRunNextJob(scheduleItems)) {
                break;
            }
        }
    }

    private boolean tryRunNextJob(List<JobScheduleItem> scheduleItems) {
        /*
         * ?????? ???? ???????????????????? ???????????????????????? ???????????? ???? ?????????????????? 30 ???????? (?????? ????????????????????, ???????????? ?????? ?????? ?????????? ???? ????????????
         * ?????????????????????? ?? ???????????????????? ?????????????????? ???????????????? ???????????? ???? ????????????????????, ???? ?????????????????? ???? ???? ?????? ?????????? ????????????).
         * ???????????????? ?????? ?????? ???????????? - ?????? ???? ???????????????? ???? digest'?? ???????????? - ???????????? digest, ???????????? ???????????? ??????????????????????, ?????? ????????
         * ???????? ?????????????? - ?????????????????????????? ?????????????? ?????? ??????????????????????????. ??????????????, ?????????? ?????? ?????????????????????? ?????????? ?????????????????????? ????????,
         * ?? ???? ?????? ???????????????? ?? ???????? ?????????????????????????????? ???? ?????? ???????????? ??????????-???????????? ??????????, ?? ???? ???????????? - ?????? ?? ???? ??????????????????????????
         * */
        LocalDate dateNow = LocalDate.now();

        for (JobScheduleItem scheduleItem : scheduleItems) {

            try {

                // ???????? ???? ?????????? ???????????? ????????, ?????????? ???? ?????????????????? ?????????? ????????????, ???????? ???? ?????? ???????????? ?????? ??????????????????
                for (int iDay = 30; iDay >= 0; iDay--) {
                    LocalDate dateX = dateNow.minusDays(iDay);

                    // ?????????????????? ???????????? ???????????????????? ???????????????? ???????????????????? ?? ???????????? ?? ???? ????????, ?????????????? ?????????????????? ?????? ???????????????? ????????????????????.
                    //???????? ?????????? ?????????????? ?????????? ?????????? ?????? ?????????????? - ???????????????????? ???????????? ???? ?????????????????? ???????? ?? ?????? ???? ?????????????????????? ???????????? ??????????????
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

                    // ?????? ?????????????? ???????????????? ???????????????????? ?????????? ???????? ?????????????????????????? ?????????????????? ?????????? ?????? ?????????????? ???? ???????????? ??????????????????????,
                    // ????????????????, ???????? ???? ?? ???????????????? ???????????????? ?????????????????? ?????????????? ???????????? ????????????????????. ????????????????????????????, ???????????????? ??????????????????
                    // ???????????? ????????????
                    for (JobParameters jobParameters : JobParametersHelper.buildJobStartingParameters(dateX, scheduleItem.getParameters())) {
                        // ????????????????, ?????????????????? ???? ?????? ???????????? ?????? ?????????? ???????? ?????? ???????????????? ?? ?????????????????????? ?????????? ????????????
                        String digest = JobParametersHelper.buildParamsDigest(jobParameters);
                        if (runningJobs.containsKey(digest)) {
                            continue;
                        }

                        List<Job> completedJobs = jobDao.byDigestLatest10(digest);

                        // ???????? ???????????? ???? ?????????????????? ?? ???????????? ?????????????????????? ?????? ?????????????????? ???? ?????????????????? ?????????????? ??????????????, ???? ?????????????????? ????
                        if (completedJobs.size() == 0 || completedJobs.get(0).getCreated().isBefore(LocalDateTime.of(dateX, LocalTime.MIN))) {
                            JobKey newJobKey = jobExecutionService.startJobUnsecured(jobParameters);
                            runningJobs.put(digest, newJobKey);

                            // ???????????????????????????? ?? ??????, ???????? ?????????????????????? ???? ???????????????? ?????????????????? ???????????? ??????????????
                            if (iDay > 2) {
                                logger.warn("?????????????????? ???????????? ?????????????????????? ???????????? ???? 2 ?????? ?? ?????????? ?????????????????? ???? ??????????????, ???? ???????????? - " + newJobKey.toString());
                            }

                            // ?????????????? ?? ?????????????? ?????? ???????????? ?????????? ?? ???????????????????? ?????????????????? ???????????????????? ????????????
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("???????????? ?????? ?????????????????? ???????????????? ???????????????????? ???????????????? ?????????????? ???????????????????????? ???????????????? Id = " + scheduleItem.getId());
                logger.error(String.format("%s\n%s", e.getMessage(), ExceptionUtils.getStackTrace(e)));
            }
        }

        return false;
    }
}
