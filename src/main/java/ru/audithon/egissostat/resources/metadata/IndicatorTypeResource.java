package ru.audithon.egissostat.resources.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.audithon.egissostat.domain.common.IndicatorType;
import ru.audithon.egissostat.logic.common.IndicatorTypeDao;
import ru.audithon.egissostat.resources.ApiResultDto;
import ru.audithon.egissostat.resources.ResourceNotFoundException;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

@RestController
@Validated
@RequestMapping(value = "/metadata/indicator-type",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class IndicatorTypeResource {
    private IndicatorTypeDao dao;

    @Autowired
    public IndicatorTypeResource(IndicatorTypeDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<IndicatorType> all() {
        return dao.all();
    }

    @GetMapping("/favorites")
    public List<IndicatorType> favorites() {
        return dao.all().stream().filter(x -> isNull(x.getFavorite(), false)).collect(Collectors.toList());
    }

    @GetMapping("/{id:-?[\\d]+}")
    public IndicatorType byId(@PathVariable("id") int id) {
        Optional<IndicatorType> value = dao.byId(id);
        if (value.isPresent()) {
            return value.get();
        }

        throw new ResourceNotFoundException();
    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody IndicatorType value) {
        IndicatorType newValue = dao.insert(value);
        if (newValue == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(newValue.getId()).toUri();

        return ResponseEntity.created(location).body(new ApiResultDto(new ArrayList<>(), null));
    }

    @PostMapping("/update/{id:-?[\\d]+}")
    public ResponseEntity update(@PathVariable("id") int id, @Valid @RequestBody IndicatorType value) {
        int cnt = dao.update(value, id);
        if (cnt == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().body(new ApiResultDto(new ArrayList<>(), null));
    }

    @PostMapping("/delete/{id:-?[\\d]+}")
    public ResponseEntity delete(@PathVariable("id") int id) {

        int cnt = dao.delete(id);
        if (cnt == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().body(new ApiResultDto(new ArrayList<>(), null));
    }
}
