package ru.audithon.egissostat.jobs.downloadegisso;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.helpers.InMemoryTxtLog;
import ru.audithon.egissostat.domain.common.EgissoCategoryType;
import ru.audithon.egissostat.domain.common.IndicatorType;
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
import ru.audithon.egissostat.logic.common.IndicatorDao;
import ru.audithon.egissostat.logic.common.IndicatorTypeDao;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service(value = DownloadEgissoStatisticParameters.TYPE_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DownloadEgissoStatisticImpl extends JobRunner {

    private static final Logger logger = LoggerFactory.getLogger(DownloadEgissoStatisticImpl.class);

    private final FileStorage fileStorage;
    private final AddrStateDao addrStateDao;
    private final IndicatorDao indicatorDao;
    private final IndicatorTypeDao indicatorTypeDao;

    @Autowired
    public DownloadEgissoStatisticImpl(FileStorage fileStorage,
                                       JobContextService jobContextService,
                                       AddrStateDao addrStateDao,
                                       IndicatorDao indicatorDao,
                                       IndicatorTypeDao indicatorTypeDao) {
        super(jobContextService);
        this.fileStorage = fileStorage;
        this.addrStateDao = addrStateDao;
        this.indicatorDao = indicatorDao;
        this.indicatorTypeDao = indicatorTypeDao;
    }

    private static int sessionLastLoadedYear;
    private static int sessionLastLoadedMonth;
    private static int sessionLastLoadedStateNo = 84; // !!! у нас закончилось ошибкой парсинга на этом месте
    private static int sessionLastLoadedCategoryNo;

    @Override
    @SneakyThrows
    public String run(JobKey jobKey, JobProgressMonitor progressMonitor, SoftCancelState cancelState, JobParameters parametersBase) {

        InMemoryTxtLog log = new InMemoryTxtLog();

        log.addLogRecord("Операция запущена.");

        progressMonitor.reportProgress(jobKey, 10000, 1, JobState.RUNNING, "Загружается список субъектов для актуализации...");

        List<LookupObject> states = addrStateDao.all();
        states.sort(Comparator.comparing(LookupObject::getId));

        List<EgissoCategoryType> egissoCats = EgissoCategoryType.getConstantDictionaryContent();
        egissoCats.sort(Comparator.comparing(EgissoCategoryType::getId));

        int i = 0;

        for (int stateNo = 0; stateNo < states.size(); stateNo++) {

            LookupObject state = states.get(stateNo);

            if (cancelState.isOperationInterrupted()) {
                break;
            }

            for (int year = 2018; year <= LocalDate.now().getYear(); year++) {

                if (cancelState.isOperationInterrupted()) {
                    break;
                }

                for (int month = 1; month <= 12; month++) {

                    if (cancelState.isOperationInterrupted()) {
                        break;
                    }

                    for (int catNo = 0; catNo < egissoCats.size(); catNo++) {

                        EgissoCategoryType categoryType = egissoCats.get(catNo);

                        if (Thread.currentThread().isInterrupted() || cancelState.isCancelRequested()) {
                            cancelState.markInterrupted();
                            log.addLogRecord("Операция прервана пользователем. Загружено " + i + " субъектов РФ.");
                            break;
                        }

                        // это чтобы в течении одной сессии можно было запускать заново
                        long currentCycleNo = (long)stateNo * 10000000000L + year * 1000000L + month * 10000L + catNo;
                        long lastStaticCycleNo = (long)sessionLastLoadedStateNo * 10000000000L + sessionLastLoadedYear * 1000000L + sessionLastLoadedMonth * 10000L + sessionLastLoadedCategoryNo;

                        if (currentCycleNo > lastStaticCycleNo) {

                            LocalDate cDate = LocalDate.of(year, month, 1);

                            // загружаем только за прошедшие даты и только 11-ю категорию (ветераны)
                            if (cDate.isBefore(LocalDate.now()) && Objects.equals(categoryType.getId(), 13)) {

                                String stageTitle = "Загрузка данных по субъекту '" + state.getCaption() +
                                    "' по состоянию на '" + cDate + "' по категории: " + categoryType.getCaption();

                                progressMonitor.reportProgress(jobKey, states.size() + 1, i,
                                    JobState.RUNNING, stageTitle);

                                Map<String, Object> periodData = getResource(state.getId(), year, month,
                                    categoryType.getId(), log, stageTitle);

                                if (periodData != null && periodData.size() > 0 && periodData.get("data") != null) {
                                    List<Object> egissoValues = (List<Object>) periodData.get("data");

                                    for (Object raw : egissoValues) {
                                        List<Object> egissoValue = (List<Object>) raw;

                                        if (egissoValue.size() < 2
                                            || egissoValue.get(0) == null || !(egissoValue.get(0) instanceof String)
                                            || egissoValue.get(1) == null || !(egissoValue.get(1) instanceof String)) {
                                            log.addLogRecord(stageTitle + ": пропущена загрузка показателя без кода или названия категории");
                                        } else {
                                            storeIndicatorValues(cDate, state, egissoValue);
                                            log.addLogRecord(stageTitle + ": загружены успешно");
                                        }
                                    }
                                } else {
                                    log.addLogRecord(stageTitle + ": отсутствуют данные");
                                }
                            }

                            sessionLastLoadedYear = year;
                            sessionLastLoadedMonth = month;
                            sessionLastLoadedStateNo = stateNo;
                            sessionLastLoadedCategoryNo = catNo;
                        }
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

    private void storeIndicatorValues(LocalDate cDate, LookupObject state, List<Object> egissoValue) {
        String indicatorBaseCode = "ЕгиссоКат" + egissoValue.get(0);
        String categoryName = (String) egissoValue.get(1);

        IndicatorType ind2 = prepateIndicatorType(indicatorBaseCode + "Кол",
            "ЕГИССО, Кол-во получателей (все бюджеты), категория: " + categoryName);
        IndicatorType ind3 = prepateIndicatorType(indicatorBaseCode + "Сум",
            "ЕГИССО, Сумма обязательств (все бюджеты), категория: " + categoryName);
        IndicatorType ind4 = prepateIndicatorType(indicatorBaseCode + "КолФед",
            "ЕГИССО, Кол-во получателей (фед бюджет), категория: " + categoryName);
        IndicatorType ind5 = prepateIndicatorType(indicatorBaseCode + "СумФед",
            "ЕГИССО, Сумма обязательств (фед бюджет), категория: " + categoryName);
        IndicatorType ind6 = prepateIndicatorType(indicatorBaseCode + "КолВне",
            "ЕГИССО, Кол-во получателей (внебюджет), категория: " + categoryName);
        IndicatorType ind7 = prepateIndicatorType(indicatorBaseCode + "СумВне",
            "ЕГИССО, Сумма обязательств (внебюджет), категория: " + categoryName);
        IndicatorType ind8 = prepateIndicatorType(indicatorBaseCode + "КолРег",
            "ЕГИССО, Кол-во получателей (рег бюджет), категория: " + categoryName);
        IndicatorType ind9 = prepateIndicatorType(indicatorBaseCode + "СумРег",
            "ЕГИССО, Сумма обязательств (рег бюджет), категория: " + categoryName);
        IndicatorType ind10 = prepateIndicatorType(indicatorBaseCode + "КолМун",
            "ЕГИССО, Кол-во получателей (мун бюджет), категория: " + categoryName);
        IndicatorType ind11 = prepateIndicatorType(indicatorBaseCode + "СумМун",
            "ЕГИССО, Сумма обязательств (мун бюджет), категория: " + categoryName);

        indicatorDao.safelyStoreEgissoValues(cDate, state, egissoValue, ind2, ind3, ind4, ind5, ind6, ind7, ind8, ind9, ind10, ind11);
    }

    private IndicatorType prepateIndicatorType(String indicatorCode, String indicatorCaption) {
        IndicatorType indicatorType = indicatorTypeDao.byCode(indicatorCode);
        if (indicatorType == null) {

            if (indicatorCaption.length() > 256) {
                indicatorCaption = indicatorCaption.substring(0, 253) + "...";
            }

            indicatorType = indicatorTypeDao.insert(new IndicatorType(null, indicatorCode, indicatorCaption, null, false, false, "-", 2));
        }
        return indicatorType;
    }

    @SneakyThrows
    private Map<String, Object> getResource(int stateId, int year, int month, int categoryId, InMemoryTxtLog log, String stageTitle) {

        int retryNo = 0;

        while (true) {
            try {
                // рандомная задержка 500-3000мс, чтобы нас сервак ЕГИССО не бортанул
                Thread.sleep(Double.valueOf(Math.random() * 2500 + 500).longValue());

                String paramONMSZ = "-1";

                if (stateId == 111) {
                    stateId = -1;
                    paramONMSZ = "13706"; // 2790.000000 Департамент ЗТ и СЗН НАО
                } else if (stateId == 71100) {
                    stateId = -1;
                    paramONMSZ = "1407"; // 0044.000001
                } else if (stateId == 71140) {
                    stateId = -1;
                    paramONMSZ = "12415"; // 2575.000001
                }

                String uri = "http://ka.egisso.ru/reporting/Data?uuid=aa0ed8b6-e07a-4f2d-8092-73665f66394a&dataVersion=24.08.2019%2008.45.02.117" +
                    "&dsCode=table2Data&paramPeriod=" + String.format("%04d-%02d", year, month) + "-01T00%3A00%3A00.000Z&paramTerritory=" +
                    stateId + "&paramPeriodicity=-1" +
                    "&paramFix=2&paramCategory=" + categoryId + "&paramONMSZ=" + paramONMSZ + "&_dc=1615795659562";

                int timeout = 30000; //ms
                RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .setSocketTimeout(timeout).build();

                try (CloseableHttpClient httpClient =
                    HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {

                    HttpGet get = new HttpGet(uri);
                    get.addHeader("Cookie", "session-cookie-mdc=166c73e4c665d00eeafd12bcbeb261f5c38eb077059e603143be5e352ef35459d0b70c05bd88fa318cfeb5e85965406b; JSESSIONID=iQ5LeuVU_UUGt8E-betAShHvVbImbOhc1NjOJ9du.va77705kriep077; csrf-token-name=csrftoken_mdc; csrf-token-value=166c76308644f4a4f74f72f145cd9b1096328d755a513523e84f6b7aa4ce9711ef9caa6b0093ad8b");
                    get.addHeader("Origin", "http://ka.egisso.ru");
                    get.addHeader("X-Requested-With", "XMLHttpRequest");
                    get.addHeader("Referer", "http://ka.egisso.ru/static-report/web/report-desktop-war-iminfin.html?reportId=aa0ed8b6-e07a-4f2d-8092-73665f66394a&version=29.10.2020 14.25.00.139&device=Desktop");
                    get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36");
                    get.addHeader("Host", "ka.egisso.ru");
                    get.addHeader("X-csrftoken_mdc", "166c73e72eff8a077f367a51607e2c9b203210b3ba458323d9624acc5c2f3492f28624f48ae205de");

                    HttpResponse response = httpClient.execute(get);
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        return null;
                    }

                    ObjectMapper mapper = new ObjectMapper();

                    try (InputStreamReader reader = new InputStreamReader(entity.getContent(), Charsets.UTF_8)) {
                        String result = CharStreams.toString(reader);

                        try {
                            Map<String, Object> retVal = mapper.readValue(result, new TypeReference<Map<String, Object>>() {
                            });

                            if (retryNo > 0) {
                                logger.warn("Попытка #" + (retryNo + 1) + ": Успешно");
                            }

                            return retVal;

                        } catch (JsonParseException ex) {
                            log.addLogRecord(stageTitle + ": загрузка пропущена из за ошибок в JSON: " + ex.getMessage());
                            logger.warn(stageTitle + ": загрузка пропущена из за ошибок в JSON: " + ex.getMessage());
                            log.addLogRecord(result);
                            logger.warn(result);
                            return null;
                        }
                    }
                }

            } catch (IOException ex) {

                logger.warn("Попытка #" + (retryNo + 1) + ": При загрузке из ЕГИССО ошибка связи: " + ex.getMessage());

                retryNo++;
                if (retryNo > 10) {
                    throw ex;
                }
            }
        }
    }
}
