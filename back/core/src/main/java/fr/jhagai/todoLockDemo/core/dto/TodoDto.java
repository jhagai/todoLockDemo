package fr.jhagai.todoLockDemo.core.dto;

import lombok.Data;

@Data
public class TodoDto {

    private Long id;
    private String title;
    private String text;
    private Long version;
    private TodoLockDto todoLock;
}
