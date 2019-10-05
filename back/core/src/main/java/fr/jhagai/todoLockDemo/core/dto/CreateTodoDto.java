package fr.jhagai.todoLockDemo.core.dto;

import lombok.Value;

@Value
public class CreateTodoDto {
    private String title;
    private String text;
}
