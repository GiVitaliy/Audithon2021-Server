package ru.audithon.egissostat.resources.applogic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.audithon.egissostat.domain.common.Indicator;
import ru.audithon.egissostat.domain.common.IndicatorType;
import ru.audithon.egissostat.logic.common.IndicatorDao;
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
@RequestMapping(value = "/indicator",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class IndicatorResource {
    private IndicatorDao dao;

    @Autowired
    public IndicatorResource(IndicatorDao dao) {
        this.dao = dao;
    }

    @GetMapping("/history/{indicatorTypeId:-?[\\d]+}/{stateId:-?[\\d]+}")
    public List<Indicator> history(@PathVariable("indicatorTypeId") Integer indicatorTypeId,
                                   @PathVariable("stateId") Integer stateId) {
        return dao.getHistoryData(indicatorTypeId, stateId);
    }

    @GetMapping("/period/{indicatorTypeId:-?[\\d]+}/{year:-?[\\d]+}/{month:-?[\\d]+}")
    public List<Indicator> period(@PathVariable("indicatorTypeId") Integer indicatorTypeId,
                                   @PathVariable("year") Integer year,
                                  @PathVariable("month") Integer month) {
        return dao.getPeriodData(indicatorTypeId, year, month);
    }
}
