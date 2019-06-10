package fr.jhagai.todoLockDemo;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String login;
    private String password;
}
