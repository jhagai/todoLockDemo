package fr.jhagai.todoLockDemo.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class LocalDateTimeDeserializer extends JsonDeserializer {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return LocalDateTime.ofEpochSecond(p.getValueAsLong(), 0, OffsetDateTime.now().getOffset());
    }
}
