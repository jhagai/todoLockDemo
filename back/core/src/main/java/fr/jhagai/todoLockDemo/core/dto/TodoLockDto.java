package fr.jhagai.todoLockDemo.core.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.jhagai.todoLockDemo.core.services.LocalDateTimeDeserializer;
import fr.jhagai.todoLockDemo.core.services.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoLockDto {

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;
    private UserDto user;

}
