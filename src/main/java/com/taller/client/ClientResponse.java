package com.taller.client;

import java.time.LocalDate;
public record ClientResponse(Long id, String nombreCompleto, String contactoAlternativo, Integer edad, LocalDate fechaNacimiento, String telefonoPersonal, String telefonoTrabajo, String email, String emailTrabajo, String calle, String colonia, String municipio, String estado, String codigoPostal, String nombreFotografia, String fotografiaUrl) {}
