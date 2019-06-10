package fr.jhagai.todoLockDemo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.jhagai.todoLockDemo.services.LocalDateTimeDeserializer;
import fr.jhagai.todoLockDemo.services.LocalDateTimeSerializer;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class LockInfo {
    private Long userId;
    private String username;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;

    private long count;
}
