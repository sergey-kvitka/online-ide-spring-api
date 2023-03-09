package com.kvitka.spring_api.security.jwt;

import com.kvitka.spring_api.entities.Role;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.services.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("${jwt.token.expired}")
    private Long validityInMilliseconds;

    private final UserDetailsService userDetailsService;
    private final UserServiceImpl userService;

    @PostConstruct
    protected void init() {
        secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String createToken(String username, List<Role> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", getRoleNames(roles));
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        log.info("Creating token (claims: {})", claims);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        log.info("Getting authentication by token");
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public User getUserByBearerToken(String bearerToken) {
        return userService.findByUsername(getUsername(getToken(bearerToken)));
    }

    public String getUsername(String token) {
        String subject = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        log.info("Getting username by token (username: {})", subject);
        return subject;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.info("Getting bearer token from HTTP request");
        return getToken(bearerToken);
    }

    public String getToken(String bearerToken) {
        boolean isNull = bearerToken == null;
        if (!isNull && bearerToken.startsWith("Bearer_")) {
            log.info("Extracting token from bearer token");
            return bearerToken.substring(7);
        }
        log.warn(isNull ? "No bearer token provided" : "Wrong bearer token format");
        return null;
    }

    public boolean validateToken(String token) {
        try {
            return !(Jwts.parser().setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date()));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public List<String> getRoleNames(List<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }
}
