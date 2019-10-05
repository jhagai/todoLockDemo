package fr.jhagai.todoLockDemo.web;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String login;
    private String password;
}
