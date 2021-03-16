package ru.audithon.common.helpers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigInteger;

public class BigIntegerJsonStringSerializer extends StdSerializer<BigInteger> {
    public BigIntegerJsonStringSerializer() {
        this(null);
    }

    public BigIntegerJsonStringSerializer(Class<BigInteger> t) {
        super(t);
    }

    @Override
    public void serialize(
            BigInteger value, JsonGenerator gen, SerializerProvider arg2)
            throws IOException {
        gen.writeString(value != null ? value.toString() : null);
    }
}