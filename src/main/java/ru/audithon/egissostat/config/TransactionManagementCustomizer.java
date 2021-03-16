package ru.audithon.egissostat.config;

import org.springframework.boot.autoconfigure.transaction.PlatformTransactionManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Configuration
public class TransactionManagementCustomizer {
    @Bean
    public PlatformTransactionManagerCustomizer<AbstractPlatformTransactionManager> transactionManagementConfigurer() {
        return (AbstractPlatformTransactionManager transactionManager) ->
                transactionManager.setGlobalRollbackOnParticipationFailure(false);
    }
}
