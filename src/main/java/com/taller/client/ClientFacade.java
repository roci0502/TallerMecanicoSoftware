package com.taller.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.List;

@Service
public class ClientFacade {
    private static final long MAX_IMAGE_BYTES = 7L * 1024 * 1024;
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final ClientRepository clients;
    public ClientFacade(ClientRepository clients) { this.clients = clients; }

    public ClientResponse register(ClientRegistrationRequest data, MultipartFile fotografia) {
        validateAge(data.edad(), data.fechaNacimiento());
        validateImage(fotografia);
        String email = data.email().trim().toLowerCase();
        String phone = data.telefonoPersonal().trim();
        validateDuplicates(data, email, phone, null);
        try {
            Client client = new Client();
            client.setNombreCompleto(data.nombreCompleto().trim()); client.setContactoAlternativo(data.contactoAlternativo().trim()); client.setEdad(data.edad()); client.setFechaNacimiento(data.fechaNacimiento());
            client.setTelefonoPersonal(phone); client.setTelefonoTrabajo(data.telefonoTrabajo().trim()); client.setEmail(email); client.setEmailTrabajo(blankToNull(data.emailTrabajo()));
            client.setCalle(data.calle().trim()); client.setColonia(data.colonia().trim()); client.setMunicipio(data.municipio().trim()); client.setEstado(data.estado().trim()); client.setCodigoPostal(data.codigoPostal().trim());
            client.setFotografia(fotografia.getBytes()); client.setTipoFotografia(fotografia.getContentType()); client.setNombreFotografia(Paths.get(fotografia.getOriginalFilename() == null ? "fotografia" : fotografia.getOriginalFilename()).getFileName().toString());
            return response(clients.save(client));
        } catch (IOException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No fue posible leer la fotografía"); }
    }
    public List<ClientResponse> list() { return clients.findAll().stream().map(this::response).toList(); }
    public Client image(Long id) { return clients.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")); }
    public ClientResponse update(Long id, ClientRegistrationRequest data, MultipartFile fotografia) {
        validateAge(data.edad(), data.fechaNacimiento());
        Client client = image(id);
        String email = data.email().trim().toLowerCase();
        String phone = data.telefonoPersonal().trim();
        validateDuplicates(data, email, phone, id);
        applyData(client, data, email, phone);
        if (fotografia != null && !fotografia.isEmpty()) {
            validateImage(fotografia);
            try {
                client.setFotografia(fotografia.getBytes());
                client.setTipoFotografia(fotografia.getContentType());
                client.setNombreFotografia(Paths.get(fotografia.getOriginalFilename() == null ? "fotografia" : fotografia.getOriginalFilename()).getFileName().toString());
            } catch (IOException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No fue posible leer la fotografía"); }
        }
        return response(clients.save(client));
    }
    public void delete(Long id) { clients.delete(image(id)); }
    private void validateAge(Integer edad, LocalDate birthDate) { if (Period.between(birthDate, LocalDate.now()).getYears() != edad) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La edad no coincide con la fecha de nacimiento"); }
    private void validateImage(MultipartFile image) { if (image == null || image.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fotografía es obligatoria"); if (image.getSize() >= MAX_IMAGE_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La fotografía debe pesar menos de 7 MB"); if (!IMAGE_TYPES.contains(image.getContentType())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fotografía debe ser PNG, JPG, WEBP o GIF"); }
    private void validateDuplicates(ClientRegistrationRequest data, String email, String phone, Long excludedId) {
        String name = data.nombreCompleto().trim();
        String workEmail = blankToNull(data.emailTrabajo());
        String workPhone = data.telefonoTrabajo() == null || data.telefonoTrabajo().isBlank() ? null : data.telefonoTrabajo().trim();
        if (usedName(name, excludedId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este nombre completo ya está registrado");
        if (usedEmail(email, excludedId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este correo ya está registrado");
        if (usedPersonalPhone(phone, excludedId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este teléfono personal ya está registrado");
        if (workEmail != null && usedWorkEmail(workEmail, excludedId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este correo de trabajo ya está registrado");
        if (workPhone != null && usedWorkPhone(workPhone, excludedId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este teléfono de trabajo ya está registrado");
    }
    private boolean usedName(String value, Long id) { return id == null ? clients.findByNombreCompletoIgnoreCase(value).isPresent() : clients.findByNombreCompletoIgnoreCaseAndIdNot(value, id).isPresent(); }
    private boolean usedEmail(String value, Long id) { return id == null ? clients.findByEmailIgnoreCase(value).isPresent() : clients.findByEmailIgnoreCaseAndIdNot(value, id).isPresent(); }
    private boolean usedPersonalPhone(String value, Long id) { return id == null ? clients.findByTelefonoPersonal(value).isPresent() : clients.findByTelefonoPersonalAndIdNot(value, id).isPresent(); }
    private boolean usedWorkEmail(String value, Long id) { return id == null ? clients.findByEmailTrabajoIgnoreCase(value).isPresent() : clients.findByEmailTrabajoIgnoreCaseAndIdNot(value, id).isPresent(); }
    private boolean usedWorkPhone(String value, Long id) { return id == null ? clients.findByTelefonoTrabajo(value).isPresent() : clients.findByTelefonoTrabajoAndIdNot(value, id).isPresent(); }
    private void applyData(Client client, ClientRegistrationRequest data, String email, String phone) { client.setNombreCompleto(data.nombreCompleto().trim()); client.setContactoAlternativo(data.contactoAlternativo().trim()); client.setEdad(data.edad()); client.setFechaNacimiento(data.fechaNacimiento()); client.setTelefonoPersonal(phone); client.setTelefonoTrabajo(data.telefonoTrabajo().trim()); client.setEmail(email); client.setEmailTrabajo(blankToNull(data.emailTrabajo())); client.setCalle(data.calle().trim()); client.setColonia(data.colonia().trim()); client.setMunicipio(data.municipio().trim()); client.setEstado(data.estado().trim()); client.setCodigoPostal(data.codigoPostal().trim()); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim().toLowerCase(); }
    private ClientResponse response(Client c) { return new ClientResponse(c.getId(),c.getNombreCompleto(),c.getContactoAlternativo(),c.getEdad(),c.getFechaNacimiento(),c.getTelefonoPersonal(),c.getTelefonoTrabajo(),c.getEmail(),c.getEmailTrabajo(),c.getCalle(),c.getColonia(),c.getMunicipio(),c.getEstado(),c.getCodigoPostal(),c.getNombreFotografia(),"/api/clientes/"+c.getId()+"/fotografia"); }
}
