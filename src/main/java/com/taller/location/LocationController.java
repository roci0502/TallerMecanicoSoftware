package com.taller.location;

import com.taller.security.JwtService;
import com.taller.user.Role;
import com.taller.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
public class LocationController {
    private final LocationFacade facade; private final UserRepository users; private final JwtService jwt;
    public LocationController(LocationFacade facade, UserRepository users, JwtService jwt) { this.facade = facade; this.users = users; this.jwt = jwt; }
    @GetMapping("/estados") public List<LocationFacade.StateOption> states(@RequestHeader(value="Authorization", required=false) String authorization) { authorizeLocation(authorization); return facade.states(); }
    @GetMapping("/estados/{stateCode}/municipios") public List<LocationFacade.MunicipalityOption> municipalities(@PathVariable String stateCode, @RequestHeader(value="Authorization", required=false) String authorization) { authorizeLocation(authorization); return facade.municipalities(stateCode); }
    @GetMapping("/estados/{stateCode}/municipios/{municipalityCode}/colonias") public List<LocationFacade.NeighborhoodOption> neighborhoods(@PathVariable String stateCode, @PathVariable String municipalityCode, @RequestHeader(value="Authorization", required=false) String authorization) { authorizeLocation(authorization); return facade.neighborhoods(stateCode, municipalityCode); }
    private void authorizeLocation(String authorization) { Role role = roleOf(authorization); if (role != Role.ADMINISTRADOR && role != Role.RECEPCIONISTA && role != Role.SECRETARIA) throw new AccessDeniedException("No tienes permiso para consultar ubicaciones"); }
    private Role roleOf(String authorization) { if(authorization==null || !authorization.startsWith("Bearer ")) throw new AccessDeniedException("Debes iniciar sesión"); try { String email=jwt.email(authorization.substring(7)); return users.findByEmailIgnoreCase(email).filter(user->user.isActivo()).map(user->user.getRol()).orElseThrow(()->new AccessDeniedException("Usuario no autorizado")); } catch(Exception exception) { throw new AccessDeniedException("Sesión inválida o expirada"); } }
}
