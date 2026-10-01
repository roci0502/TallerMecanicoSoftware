package com.taller.client;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.taller.user.Role;
import com.taller.user.UserRepository;
import com.taller.security.JwtService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClientController {
    private final ClientFacade facade; private final UserRepository users; private final JwtService jwt;
    public ClientController(ClientFacade facade, UserRepository users, JwtService jwt) { this.facade = facade; this.users = users; this.jwt = jwt; }
    @PostMapping(consumes = "multipart/form-data") @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse register(@Valid @RequestPart("datos") ClientRegistrationRequest datos, @RequestPart("fotografia") MultipartFile fotografia, @RequestHeader(value="Authorization", required=false) String authorization) {
        Role role = roleOf(authorization);
        boolean authorized = role == Role.ADMINISTRADOR || role == Role.RECEPCIONISTA || role == Role.SECRETARIA;
        if (!authorized) throw new AccessDeniedException("Solo Administrador, Recepcionista o Secretaria pueden registrar clientes");
        return facade.register(datos, fotografia);
    }
    @GetMapping
    public List<ClientResponse> list(@RequestHeader(value="Authorization", required=false) String authorization) { authorizeReader(authorization); return facade.list(); }
    @PutMapping(value="/{id}", consumes = "multipart/form-data")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestPart("datos") ClientRegistrationRequest datos, @RequestPart(value="fotografia", required=false) MultipartFile fotografia, @RequestHeader(value="Authorization", required=false) String authorization) { authorizeReader(authorization); return facade.update(id, datos, fotografia); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestHeader(value="Authorization", required=false) String authorization) { authorizeReader(authorization); facade.delete(id); }
    @GetMapping("/{id}/fotografia")
    public ResponseEntity<byte[]> photo(@PathVariable Long id, @RequestHeader(value="Authorization", required=false) String authorization) { Client client=facade.image(id); if(client.getFotografia()==null || client.getFotografia().length==0) return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body("<svg xmlns='http://www.w3.org/2000/svg' width='160' height='160'><rect width='100%' height='100%' fill='#e8f2fb'/><circle cx='80' cy='58' r='28' fill='#7fa5c8'/><path d='M25 145c8-35 36-48 55-48s47 13 55 48' fill='#7fa5c8'/></svg>".getBytes()); return ResponseEntity.ok().contentType(MediaType.parseMediaType(client.getTipoFotografia())).body(client.getFotografia()); }
    private Role roleOf(String authorization) { if(authorization==null || !authorization.startsWith("Bearer ")) throw new AccessDeniedException("Debes iniciar sesión"); try { String email=jwt.email(authorization.substring(7)); return users.findByEmailIgnoreCase(email).filter(user->user.isActivo()).map(user->user.getRol()).orElseThrow(()->new AccessDeniedException("Usuario no autorizado")); } catch(Exception exception) { throw new AccessDeniedException("Sesión inválida o expirada"); } }
    private void authorizeReader(String authorization) { Role role=roleOf(authorization); if(role!=Role.ADMINISTRADOR && role!=Role.RECEPCIONISTA) throw new AccessDeniedException("Solo Administrador o Recepcionista pueden consultar clientes"); }
}
