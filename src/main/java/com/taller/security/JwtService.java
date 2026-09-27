package com.taller.security;
import com.taller.user.User; import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.*; import java.time.temporal.ChronoUnit; import java.util.Date;
@Service public class JwtService {
 private final SecretKey key; public JwtService(@Value("${app.jwt-secret}") String secret){ key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
 public String token(User u){ Instant now=Instant.now(); return Jwts.builder().subject(u.getEmail()).claim("rol",u.getRol().name()).issuedAt(Date.from(now)).expiration(Date.from(now.plus(8,ChronoUnit.HOURS))).signWith(key).compact(); }
 public String email(String token){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
}
