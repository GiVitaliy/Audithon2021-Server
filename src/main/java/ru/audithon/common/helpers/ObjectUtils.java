package ru.audithon.common.helpers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ObjectUtils {
    public static boolean containsNullArgument(Object... objects) {
        if (objects == null) {
            return false;
        }

        for (int i = 0; i < objects.length; ++i) {
            if (objects[i] == null) {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean equalsSome(T val, T... objects) {
        for (T object : objects) {
            if (Objects.equals(val, object)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T isNull(T val, T nullVal) {
        return val == null ? nullVal : val;
    }
    public static <T> T coalesce(T a, T b) {
        return a == null ? b : a;
    }
    public static <T> T coalesce(T a, T b, T c) {
        return a != null ? a : (b != null ? b : c);
    }
    public static <T> T coalesce(T a, T b, T c, T d) {
        return a != null ? a : (b != null ? b : (c != null ? c : d)) ;
    }

    private static final Set<Class<?>> primitiveWrappers = new HashSet<Class<?>>() {{
        add(Boolean.class);
        add(Character.class);
        add(Byte.class);
        add(Short.class);
        add(Integer.class);
        add(Long.class);
        add(Float.class);
        add(Double.class);
        add(Void.class);
    }};

    public static boolean typeIsPrimitiveOrPrimitiveWrapper(Class<?> type) {
        return type.isPrimitive() || primitiveWrappers.contains(type);
    }
}
