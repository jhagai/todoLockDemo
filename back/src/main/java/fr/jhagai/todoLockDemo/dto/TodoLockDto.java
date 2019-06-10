package fr.jhagai.todoLockDemo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.jhagai.todoLockDemo.services.LocalDateTimeDeserializer;
import fr.jhagai.todoLockDemo.services.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoLockDto {

    private Long id;
    private Long version;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;
    private Long count;
    private UserDto user;

}
