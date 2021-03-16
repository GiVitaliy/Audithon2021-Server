package ru.audithon.egissostat.infrastructure.mass.rest;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.audithon.egissostat.infrastructure.mass.dao.JobDao;
import ru.audithon.egissostat.infrastructure.mass.domain.Job;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.infrastructure.mass.helpers.JobParametersHelper;
import ru.audithon.egissostat.jobs.JobParameters;
import ru.audithon.egissostat.resources.ApiResultDto;
import ru.audithon.egissostat.resources.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/jobs/data",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class JobDataEndpointCore {
    private final JobDao jobDao;

    @Autowired
    public JobDataEndpointCore(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    @GetMapping()
    public List<Job> find(
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDateTo,
        @RequestParam(value = "state", required = false) Integer stateId) {

        // Пользователи с ролью "администратор" видят все job'ы
        return jobDao.byStateAndCreatedDateRange(stateId, createdDateFrom, createdDateTo, null);
    }

    @GetMapping("/{typeId:[\\d]+}/{id:[\\d]+}")
    public Job byId(@PathVariable("typeId") int typeId, @PathVariable("id") int id) {
        Optional<Job> value = jobDao.byId(new JobKey(typeId, id));
        if (value.isPresent()) {
            return value.get();
        }

        throw new ResourceNotFoundException();
    }

    @PostMapping("/latest/{typeId:[\\d]+}")
    @SneakyThrows
    public ResponseEntity latestJobsWithSameParams(@PathVariable("typeId") int typeId,
                                                   @RequestBody() JobParameters parameters) {
        List<Job> jobs = jobDao.byDigestCompleted(JobParametersHelper.buildParamsDigest(parameters));

        return ResponseEntity.ok()
            .body(new ApiResultDto(new ArrayList<>(), jobs));
    }
}
