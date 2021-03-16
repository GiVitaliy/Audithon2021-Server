package ru.audithon.egissostat.jobs.downloadegisso;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.helpers.InMemoryTxtLog;
import ru.audithon.egissostat.domain.common.LookupObject;
import ru.audithon.egissostat.filestorage.FileStorage;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.infrastructure.mass.domain.JobState;
import ru.audithon.egissostat.infrastructure.mass.domain.SoftCancelState;
import ru.audithon.egissostat.infrastructure.mass.service.JobContextService;
import ru.audithon.egissostat.infrastructure.mass.service.JobProgressMonitor;
import ru.audithon.egissostat.infrastructure.mass.service.JobRunner;
import ru.audithon.egissostat.jobs.JobParameters;
import ru.audithon.egissostat.logic.common.AddrStateDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service(value = DownloadEgissoStatisticParameters.TYPE_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DownloadEgissoStatisticImpl extends JobRunner {

    private final FileStorage fileStorage;
    private final AddrStateDao addrStateDao;

    @Autowired
    public DownloadEgissoStatisticImpl(FileStorage fileStorage,
                                       JobContextService jobContextService,
                                       AddrStateDao addrStateDao) {
        super(jobContextService);
        this.fileStorage = fileStorage;
        this.addrStateDao = addrStateDao;
    }

    @Override
    @SneakyThrows
    public String run(JobKey jobKey, JobProgressMonitor progressMonitor, SoftCancelState cancelState, JobParameters parametersBase) {

        InMemoryTxtLog log = new InMemoryTxtLog();

        log.addLogRecord("Операция запущена.");

        progressMonitor.reportProgress(jobKey, 10000, 1, JobState.RUNNING, "Загружается список субъектов для актуализации...");

        List<LookupObject> states = addrStateDao.all();

        int i = 0;

        for(LookupObject state : states) {

            if (cancelState.isOperationInterrupted()) {
                break;
            }

            for (int year = 2018; year <= LocalDate.now().getYear(); year ++) {

                if (cancelState.isOperationInterrupted()) {
                    break;
                }

                for (int month = 1; month <= 12; month++) {

                    if (Thread.currentThread().isInterrupted() || cancelState.isCancelRequested()) {
                        cancelState.markInterrupted();
                        log.addLogRecord("Операция прервана пользователем. Загружено " + i + " субъектов РФ.");
                        break;
                    }

                    LocalDate cDate = LocalDate.of(year, month, 1);

                    // загружаем только за прошедшие даты
                    if (cDate.isBefore(LocalDate.now())) {

                        progressMonitor.reportProgress(jobKey, states.size() + 1, i,
                            JobState.RUNNING, "Загрузка данных по субъекту '" + state.getCaption() +
                                "' по состоянию на '" + cDate + "'");

                        // рандомная задержка 500-3000мс, чтобы нас сервак ЕГИССО не бортанул
                        Thread.sleep(Double.valueOf(Math.random() * 2500 + 500).longValue());
                    }
                }
            }

            i++;
        }

        if (cancelState.isOperationInterrupted()) {
            throw new BusinessLogicException("Операция прервана пользователем");
        }

        log.addLogRecord("Актуализация данных из ЕГИССО закончена.");
        log.addLogRecord("Операция успешно завершена.");

        String resFileName = "op-res/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + "/" + jobKey.getTypeId() + "/" + jobKey.getId() + ".log.txt";

        fileStorage.writeFile(resFileName, log.getUtf8LogBytes());

        return resFileName;
    }
}
