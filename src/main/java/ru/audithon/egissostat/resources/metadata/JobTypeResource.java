package ru.audithon.egissostat.resources.metadata;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.audithon.egissostat.jobs.JobType;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/metadata/job-type",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class JobTypeResource {
    @GetMapping
    public List<JobType> all() {
        return JobType.getConstantDictionaryContent();
    }
}
