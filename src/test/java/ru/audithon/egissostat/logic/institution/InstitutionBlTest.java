package ru.audithon.egissostat.logic.institution;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import ru.audithon.egissostat.config.AppSecurityConfig;

@RunWith(SpringRunner.class)
@JdbcTest(includeFilters = {
        @ComponentScan.Filter(Repository.class),
        @ComponentScan.Filter(Service.class),
        @ComponentScan.Filter(Configuration.class)
},
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AppSecurityConfig.class),
        })
@AutoConfigureJson
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/db/data/institution.sql")
public class InstitutionBlTest {

//    @Test
//    public void okOgrn() {
//        Institution institution = createWithOgrn("1037727038315");
//        institution.prettify();
//        institutionBl.validate(institution);
//    }
}
