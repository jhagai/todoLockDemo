package fr.jhagai.todoLockDemo;

import fr.jhagai.todoLockDemo.services.AuthenticationService;
import fr.jhagai.todoLockDemo.services.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
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

