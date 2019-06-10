package fr.jhagai.todoLockDemo.services;

import fr.jhagai.todoLockDemo.AuthenticationRequest;
import fr.jhagai.todoLockDemo.dao.UserRepository;
import fr.jhagai.todoLockDemo.security.TodoPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    public AuthenticationService() {
    }

    @Transactional(readOnly = true)
    public Optional<fr.jhagai.todoLockDemo.entities.User> authenticate(AuthenticationRequest authenticationRequest) {

        final String login = authenticationRequest.getLogin();
        final String password = authenticationRequest.getPassword();
        return this.userRepository.findByLoginAndPassword(login, password);
    }

    public Authentication getAuthentication(Jws<Claims> token) {

        final Claims body = token.getBody();
        final Long userId = Long.valueOf((String) body.get("userId"));
        final String login = (String) body.get("login");

        final TodoPrincipal todoPrincipal = new TodoPrincipal();
        todoPrincipal.setUserId(userId);
        todoPrincipal.setName(login);

        return new UsernamePasswordAuthenticationToken(todoPrincipal, "PROTECTED",
                AuthorityUtils.commaSeparatedStringToAuthorityList(body.get("roles", String.class)));
    }
}
