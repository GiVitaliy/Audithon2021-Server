package ru.audithon.egissostat.infrastructure.mass.rest;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.infrastructure.mass.domain.JobProgress;
import ru.audithon.egissostat.jobs.JobParameters;
import ru.audithon.egissostat.infrastructure.mass.service.JobExecutionService;
import ru.audithon.egissostat.resources.ApiResultDto;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(value = "/jobs",
                produces = MediaType.APPLICATION_JSON_VALUE)
public class JobEndpointCore {

    private JobExecutionService executionService;

    @Autowired
    public JobEndpointCore(JobExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping
    public Collection<JobProgress> all() {
        return executionService.getJobProgress().values();
    }

    @GetMapping("/{typeId:[\\d]+}/{id:[\\d]+}")
    public JobProgress byId(@PathVariable("typeId") int typeId, @PathVariable("id") int id) {
        Optional<JobProgress> value = executionService.getJobProgress(new JobKey(typeId, id));
        return value.orElse(JobProgress.completed(new JobKey(typeId, id), null));
    }

    @PostMapping("/{typeId:[\\d]+}")
    @SneakyThrows
    public ResponseEntity createJob(@PathVariable("typeId") int typeId,
                                    @RequestPart("meta") JobParameters parameters,
                                    @RequestPart(value = "file", required = false) MultipartFile file) {
        if (file != null) {
            parameters.setUploadedFileStream(file.getInputStream());
        }

        JobKey jobKey = executionService.startJob(parameters);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{typeId}/{id}")
            .buildAndExpand(jobKey.getTypeId(), jobKey.getId())
            .toUri();

        return ResponseEntity.created(location)
            .body(new ApiResultDto(new ArrayList<>(), jobKey));
    }

    @PostMapping("/delete/{typeId:[\\d]+}/{id:[\\d]+}")
    public ResponseEntity cancelJob(@PathVariable("typeId") int typeId, @PathVariable("id") int id) {
        executionService.softCancel(new JobKey(typeId, id));

        return new ResponseEntity(HttpStatus.OK);
    }
}
