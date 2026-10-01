package com.taller.auth;

import com.taller.security.JwtService;
import com.taller.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthFacade {
    private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
    public AuthFacade(UserRepository users, PasswordEncoder encoder, JwtService jwt) { this.users=users; this.encoder=encoder; this.jwt=jwt; }
    public AuthResponse register(String nombre, String email, String password) { if(users.findByEmailIgnoreCase(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"El correo ya está registrado"); User u=new User();u.setNombre(nombre.trim());u.setEmail(email.toLowerCase());u.setPassword(encoder.encode(password));u.setRol(Role.CLIENTE);users.save(u);return response(u); }
    public AuthResponse login(String email, String password) { User u=users.findByEmailIgnoreCase(email).filter(User::isActivo).filter(x->encoder.matches(password,x.getPassword())).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Correo o contraseña incorrectos"));return response(u); }
    public void requestPasswordRecovery(String email) { users.findByEmailIgnoreCase(email).ifPresent(u->{u.setTokenRecuperacion(UUID.randomUUID().toString());u.setTokenExpira(Instant.now().plus(30,ChronoUnit.MINUTES));users.save(u);}); }
    public void resetPassword(String token, String password) { User u=users.findByTokenRecuperacion(token).filter(x->x.getTokenExpira().isAfter(Instant.now())).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"El enlace es inválido o expiró"));u.setPassword(encoder.encode(password));u.setTokenRecuperacion(null);u.setTokenExpira(null);users.save(u); }
    private AuthResponse response(User user) { return new AuthResponse(jwt.token(user),user.getNombre(),user.getRol()); }
    public record AuthResponse(String token, String nombre, Role rol) {}
}
