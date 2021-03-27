package ru.audithon.egissostat.resources.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.audithon.egissostat.domain.common.IndicatorType;
import ru.audithon.egissostat.domain.common.LookupObject;
import ru.audithon.egissostat.domain.common.LookupStrKeyObject;
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
@RequestMapping(value = "/metadata/indicator-type-group",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class IndicatorTypeGroupResource {
    private IndicatorTypeDao dao;

    @Autowired
    public IndicatorTypeGroupResource(IndicatorTypeDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<LookupStrKeyObject> all() {
        return dao.all().stream().filter(x -> isNull(x.getFavorite(), false))
            .map(IndicatorType::getGroup).distinct().map(x -> new LookupStrKeyObject(x, x)).collect(Collectors.toList());
    }
}
