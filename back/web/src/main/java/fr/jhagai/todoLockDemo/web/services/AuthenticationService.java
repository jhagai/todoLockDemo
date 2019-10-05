package fr.jhagai.todoLockDemo.web.services;

import fr.jhagai.todoLockDemo.core.dao.UserRepository;
import fr.jhagai.todoLockDemo.web.AuthenticationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    public AuthenticationService() {
    }

    @Transactional(readOnly = true)
    public Optional<fr.jhagai.todoLockDemo.core.entities.User> authenticate(AuthenticationRequest authenticationRequest) {

        final String login = authenticationRequest.getLogin();
        final String password = authenticationRequest.getPassword();
        return this.userRepository.findByLoginAndPassword(login, password);
    }
}
