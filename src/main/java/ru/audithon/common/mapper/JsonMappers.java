package ru.audithon.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

// содержит функции, преобразующие сложные типы данных в JSON представление. используются, например, при сериализации в БД.
//как один из основных вариантов использования, преобразует массив целочисленных значений в строку, чтобы потом запихать её
//в одно строковое поле в БД
public class JsonMappers {
    private static final ObjectMapper jsonMapper;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.registerModule(new JavaTimeModule());
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    public static <TObject, TArrItem> BiConsumer<TObject, String> of(BiConsumer<TObject, List<TArrItem>> arraySetter,
                                                                     Class<TArrItem> itemClass) {
        return (obj, val) -> {
            if (val != null) {
                try {
                    JavaType collectionType = jsonMapper.getTypeFactory().constructCollectionType(List.class, itemClass);
                    arraySetter.accept(obj, jsonMapper.readValue(val, collectionType));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                arraySetter.accept(obj, new ArrayList<>());
            }
        };
    }

    public static <TObject, TItem> BiConsumer<TObject, String> ofValue(BiConsumer<TObject, TItem> valueSetter,
                                                                  Class<TItem> itemClass) {
        return (obj, val) -> {
            if (val != null) {
                try {
                    valueSetter.accept(obj, jsonMapper.readValue(val, itemClass));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                valueSetter.accept(obj, null);
            }
        };
    }

    public static <TObject> BiConsumer<TObject, String> ofSet(BiConsumer<TObject, Set<Integer>> setSetter) {
        return (obj, val) -> {
            if (val != null) {
                try {
                    setSetter.accept(obj, jsonMapper.readValue(val, new TypeReference<HashSet<Integer>>() {
                    }));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                setSetter.accept(obj, new HashSet<>());
            }
        };
    }

    public static <TObject, TArrItem> Function<TObject, String> of(Function<TObject, List<TArrItem>> arrayGetter) {
        return obj -> {
            try {
                List<TArrItem> val = arrayGetter.apply(obj);
                if (val == null || val.size() == 0) {
                    return null;
                } else {
                    return jsonMapper.writeValueAsString(val);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    public static <TObject, TItem> Function<TObject, String> ofValue(Function<TObject, TItem> valueGetter) {
        return obj -> {
            try {
                TItem val = valueGetter.apply(obj);
                if (val == null) {
                    return null;
                } else {
                    return jsonMapper.writeValueAsString(val);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    public static <TObject> Function<TObject, String> ofSet(Function<TObject, Set<Integer>> setGetter) {
        return obj -> {
            try {
                Set<Integer> val = setGetter.apply(obj);
                if (val == null || val.size() == 0) {
                    return null;
                } else {
                    return jsonMapper.writeValueAsString(val);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    public static <T> String writeJson(T val) {
        try {
            if (val == null) {
                return null;
            } else {
                return jsonMapper.writeValueAsString(val);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <R> R readObject(String val, Class<R> objectClass) {
        if (val != null) {
            try {
                return jsonMapper.readValue(val, objectClass);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return null;
        }
    }

    public static <R> R readObject(String val, TypeReference<R> typeReference) {
        if (val != null) {
            try {
                return jsonMapper.readValue(val, typeReference);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return null;
        }
    }

    public static <R> List<R> readCollection(String val, Class<R> itemClass) {
        if (val != null) {
            try {
                JavaType collectionType = jsonMapper.getTypeFactory().constructCollectionType(List.class, itemClass);
                return jsonMapper.readValue(val, collectionType);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return null;
        }
    }
}
