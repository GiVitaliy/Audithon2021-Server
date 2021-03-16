package ru.audithon.common.types;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tuple3<A, B, C> {
    private A a;
    private B b;
    private C c;
}
