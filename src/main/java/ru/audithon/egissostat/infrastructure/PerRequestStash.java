package ru.audithon.egissostat.infrastructure;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

/*
* Хранилище полезностей (локальный кэш) для отдельного запроса
*/
@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PerRequestStash {

    private Map<String, Object> keyValueStorage = new HashMap<>();

    public Map<String, Object> getKeyValueStorage() {
        return keyValueStorage;
    }
}
