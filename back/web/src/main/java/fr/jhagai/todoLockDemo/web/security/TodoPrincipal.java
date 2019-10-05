package fr.jhagai.todoLockDemo.web.security;

import lombok.Data;

import java.security.Principal;

@Data
public class TodoPrincipal implements Principal {
    private Long userId;
    private String name;
}
