package fr.jhagai.todoLockDemo.web;

import fr.jhagai.todoLockDemo.core.dto.ClaimsDto;
import fr.jhagai.todoLockDemo.core.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class JwtTokenService {

    private String secret = "secret";

    private static final long TOKEN_DURATION = 30;

    public String createToken(User user) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, secret)
                .setClaims(buildUserClaims(user))
                .setExpiration(getTokenExpirationDate())
                .setIssuedAt(new Date())
                .compact();
    }

    public String createToken(Claims claims) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, secret)
                .setClaims(claims)
                .setExpiration(getTokenExpirationDate())
                .setIssuedAt(new Date())
                .compact();
    }

    private Date getTokenExpirationDate() {
        return Date.from(LocalDateTime.now().plusMinutes(TOKEN_DURATION).toInstant(ZoneOffset.UTC));
    }

    private Claims buildUserClaims(User user) {
        Claims claims = Jwts.claims().setId(String.valueOf(user.getId()));
        claims.put("userId", user.getId().toString());
        claims.put("login", user.getLogin());
        return claims;
    }

    public Jws<Claims> validateJwtToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
    }

    public long getUserId(String token) {
        return Long.valueOf((String) Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().get("userId"));
    }

    public ClaimsDto getClaims(String token) {
        final Claims body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        final Long userId = Long.valueOf((String) body.get("userId"));
        final String login = (String) body.get("login");
        return new ClaimsDto(userId, login);
    }
}
