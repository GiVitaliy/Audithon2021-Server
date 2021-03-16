package ru.audithon.egissostat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("application-settings")
@EnableAsync
@EnableScheduling
@Validated
public class ApplicationInstanceConfiguration {

    private Integer mainInstitutionId;

    public Integer getMainInstitutionId() {
        return mainInstitutionId;
    }

    public void setMainInstitutionId(Integer mainInstitutionId) {
        this.mainInstitutionId = mainInstitutionId;
    }

    @Bean
    public MessageSource messageSource () {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "fields");
        return messageSource;
    }
}
