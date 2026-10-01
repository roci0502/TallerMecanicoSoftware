package com.taller.client;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ClientRegistrationRequest(
    @NotBlank @Pattern(regexp="^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]{2,150}$", message="El nombre completo solo admite letras y espacios") String nombreCompleto,
    @NotBlank @Pattern(regexp="^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]{2,150}$", message="El contacto alternativo solo admite letras y espacios") String contactoAlternativo,
    @NotNull @Min(0) @Max(130) Integer edad,
    @NotNull @Past LocalDate fechaNacimiento,
    @NotBlank @Pattern(regexp="^[0-9+() -]{7,20}$", message="El teléfono personal no tiene un formato válido") String telefonoPersonal,
    @NotBlank @Pattern(regexp="^[0-9+() -]{7,20}$", message="El teléfono de trabajo no tiene un formato válido") String telefonoTrabajo,
    @NotBlank @Email String email,
    @Email String emailTrabajo,
    @NotBlank @Size(max=150) String calle,
    @NotBlank @Pattern(regexp="^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]{2,100}$", message="La colonia solo admite letras y espacios") String colonia,
    @NotBlank @Pattern(regexp="^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]{2,100}$", message="El municipio solo admite letras y espacios") String municipio,
    @NotBlank @Pattern(regexp="^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]{2,100}$", message="El estado solo admite letras y espacios") String estado,
    @NotBlank @Pattern(regexp="^\\d{5}$", message="El código postal debe tener 5 dígitos") String codigoPostal
) {}
