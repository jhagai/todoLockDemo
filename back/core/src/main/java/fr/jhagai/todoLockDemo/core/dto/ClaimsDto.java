package fr.jhagai.todoLockDemo.core.dto;

import lombok.Value;

@Value
public class ClaimsDto {
    private Long userId;
    private String login;
}
