package ru.audithon.egissostat.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.audithon.common.mapper.CrudDao;
import ru.audithon.common.telemetry.TelemetryServiceCore;

import java.util.Arrays;
import java.util.List;

// Управляет кэшем приложения. Периодически проверяет по заложенным алгоритмам кэш на устаревание/переполнение и т.п.
@Service
@EnableAsync
public class AppCacheControl {

    public static final String CACHE_SECURITY_ROLE_FUNCTIONS = "security-role-functions";
    public static final String CACHE_ALL_CITIES = "all-cities";
    public static final String CACHE_STREETS_BY_CITY = "streets-by-city";

    private final SimpleCacheManager cacheManager;
    private final List<CrudDao> allRepositories;

    private final TelemetryServiceCore telemetryServiceCore;

    @Autowired
    public AppCacheControl(List<CrudDao> allRepositories,
                           TelemetryServiceCore telemetryServiceCore) {

        this.telemetryServiceCore = telemetryServiceCore;
        this.allRepositories = allRepositories;

        cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache(CACHE_SECURITY_ROLE_FUNCTIONS),
            new ConcurrentMapCache(CACHE_ALL_CITIES),
            new ConcurrentMapCache(CACHE_STREETS_BY_CITY)));
    }

    public void cleanupRepositoryStashes() {
        if (allRepositories != null) {
            allRepositories.forEach(CrudDao::cleanupStash);
        }
    }

    @Bean
    public CacheManager cacheManager() {
        return cacheManager;
    }

    // Периодическая проверка ролей на невалидность (их вполне могли изменить в другом экземпляре приложения или вообще извне)
    //пока тупо не будем версию проверять. достаточно будет раз секунд в 30 перезагрузить целиком
    @Scheduled(fixedDelay=30000)
    public void timeBasedCheckSecurityRoles() {
        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation("@Scheduled::timeBasedCheckSecurityRoles");
        try {
            cacheManager.getCache(CACHE_SECURITY_ROLE_FUNCTIONS).clear();
        } finally {
            telemetryServiceCore.exitOperation(optoken);
        }
    }

    @Scheduled(fixedDelay=600000)
    public void timeBasedCheckAllCities() {
        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation("@Scheduled::timeBasedCheckAllCities");
        try {
            cacheManager.getCache(CACHE_ALL_CITIES).clear();
        } finally {
            telemetryServiceCore.exitOperation(optoken);
        }
    }

    @Scheduled(fixedDelay=600000)
    public void timeBasedCheckAllStreets() {
        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation("@Scheduled::timeBasedCheckAllStreets");
        try {
            cacheManager.getCache(CACHE_STREETS_BY_CITY).clear();
        } finally {
            telemetryServiceCore.exitOperation(optoken);
        }
    }
}
