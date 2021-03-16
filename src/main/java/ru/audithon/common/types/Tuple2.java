package ru.audithon.common.types;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tuple2<A, B> {
    private A a;
    private B b;
}
