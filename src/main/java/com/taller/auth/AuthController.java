package com.taller.auth;
import com.taller.auth.AuthFacade.AuthResponse; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final AuthFacade facade; public AuthController(AuthFacade facade){this.facade=facade;}
 record Registro(@NotBlank String nombre,@Email @NotBlank String email,@Size(min=8,message="La contraseña debe tener al menos 8 caracteres") String password){} record Login(@Email @NotBlank String email,@NotBlank String password){} record Recuperar(@Email @NotBlank String email){} record Restablecer(@NotBlank String token,@Size(min=8) String password){}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) AuthResponse registrar(@Valid @RequestBody Registro r){ return facade.register(r.nombre(),r.email(),r.password()); }
 @PostMapping("/login") AuthResponse login(@Valid @RequestBody Login l){return facade.login(l.email(),l.password());}
 @PostMapping("/forgot-password") Map<String,String> olvidar(@Valid @RequestBody Recuperar r){facade.requestPasswordRecovery(r.email());return Map.of("message","Si el correo existe, recibirás instrucciones para recuperar tu cuenta.");}
 @PostMapping("/reset-password") Map<String,String> reset(@Valid @RequestBody Restablecer r){facade.resetPassword(r.token(),r.password());return Map.of("message","Contraseña actualizada correctamente");}
}
