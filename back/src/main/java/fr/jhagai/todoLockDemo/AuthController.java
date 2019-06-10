package fr.jhagai.todoLockDemo;

import fr.jhagai.todoLockDemo.dto.ClaimsDto;
import fr.jhagai.todoLockDemo.entities.User;
import fr.jhagai.todoLockDemo.security.TodoPrincipal;
import fr.jhagai.todoLockDemo.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @PostMapping(value = {"/login"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        Optional<User> authenticate = authenticationService.authenticate(authenticationRequest);

        final Optional<String> token = authenticate
                .map(user -> jwtTokenService.createToken(user));

        if (token.isPresent()) {
            final String tokenValue = token.get();
            final Cookie bearer = createCookie(tokenValue);
            response.addCookie(bearer);
            final ClaimsDto claimsDto = new ClaimsDto(authenticate.get().getId(), authenticate.get().getLogin());
            return ResponseEntity.ok(claimsDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authentication failed");
    }

    public static Cookie createCookie(String tokenValue) {
        final Cookie bearer = new Cookie("Bearer", tokenValue);
        bearer.setPath("/");
        bearer.setHttpOnly(true);
        return bearer;
    }

    @PostMapping(value = {"/logout"})
    public ResponseEntity<?> logout(HttpServletResponse response) {
        final Cookie bearer = new Cookie("Bearer", "");
        bearer.setMaxAge(0);
        bearer.setPath("/");
        response.addCookie(bearer);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = {"/claims"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClaimsDto> getClaims(HttpServletRequest request) {
        final TodoPrincipal todoPrincipal = TodoController.getTodoPrincipal(request);
        final ClaimsDto claimsDto = new ClaimsDto(todoPrincipal.getUserId(), todoPrincipal.getName());
        return ResponseEntity.ok(claimsDto);
    }
}
