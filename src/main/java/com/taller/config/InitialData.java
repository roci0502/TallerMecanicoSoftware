package com.taller.config;
import com.taller.user.*; import com.taller.user.Role; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.*; import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration public class InitialData {
 @Bean CommandLineRunner adminInicial(UserRepository users, PasswordEncoder encoder, @Value("${ADMIN_EMAIL:admin@taller.local}") String email, @Value("${ADMIN_PASSWORD:Admin123!}") String password){return args->{if(users.findByEmailIgnoreCase(email).isEmpty()){User u=new User();u.setNombre("Administrador");u.setEmail(email);u.setPassword(encoder.encode(password));u.setRol(Role.ADMINISTRADOR);users.save(u);}};}
}
