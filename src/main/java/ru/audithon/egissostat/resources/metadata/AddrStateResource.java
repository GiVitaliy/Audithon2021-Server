package ru.audithon.egissostat.resources.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.audithon.egissostat.domain.common.LookupObject;
import ru.audithon.egissostat.logic.common.AddrStateDao;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/metadata/addr-state",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class AddrStateResource {
    private AddrStateDao dao;

    @Autowired
    public AddrStateResource(AddrStateDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<LookupObject> all() {
        return dao.all();
    }
}
