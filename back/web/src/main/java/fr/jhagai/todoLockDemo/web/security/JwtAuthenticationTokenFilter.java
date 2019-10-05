package fr.jhagai.todoLockDemo.web.security;

import fr.jhagai.todoLockDemo.web.services.AuthenticationService;
import fr.jhagai.todoLockDemo.web.AuthController;
import fr.jhagai.todoLockDemo.web.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationTokenFilter extends GenericFilterBean {

    private static final String BEARER = "Bearer";

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AuthenticationService authenticationService;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        Cookie[] cookies = request.getCookies();

        String bearerToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (BEARER.equals(cookie.getName())) {
                    bearerToken = cookie.getValue();
                }
            }
        }

        Authentication authentication = null;

        if (bearerToken != null) {

            try {
                Jws<Claims> claims = jwtTokenService.validateJwtToken(bearerToken);
                authentication = this.getAuthentication(claims);
                final String token = jwtTokenService.createToken(claims.getBody());
                response.addCookie(AuthController.createCookie(token));
            } catch (ExpiredJwtException exception) {
                //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "error.jwt.expired");
                // return;
            } catch (JwtException exception) {
                // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "error.jwt.invalid");
                // return;
            }
        }


        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(servletRequest, servletResponse);
    }

    private Authentication getAuthentication(Jws<Claims> token) {

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
