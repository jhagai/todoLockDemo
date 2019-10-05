package fr.jhagai.todoLockDemo.web;

import fr.jhagai.todoLockDemo.web.services.AuthenticationService;
import fr.jhagai.todoLockDemo.core.services.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "fr.jhagai.todoLockDemo")
public class TodoLockDemoApplication {

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    TodoService todoService;

    public static void main(String[] args) {
        SpringApplication.run(TodoLockDemoApplication.class, args);
    }

    @Bean
    public AuthenticationService getAuthenticationService() {
        return new AuthenticationService();
    }

    @Bean
    public JwtTokenService getJwtTokenService() {
        return new JwtTokenService();
    }
    
}

