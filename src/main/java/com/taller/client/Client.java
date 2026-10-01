package com.taller.client;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "clientes", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cliente_email", columnNames = "email"),
    @UniqueConstraint(name = "uk_cliente_telefono_personal", columnNames = "telefonoPersonal")
})
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String nombreCompleto;
    @Column(nullable = false, length = 150) private String contactoAlternativo;
    @Column(nullable = false) private Integer edad;
    @Column(nullable = false) private LocalDate fechaNacimiento;
    @Column(nullable = false, length = 20) private String telefonoPersonal;
    @Column(nullable = false, length = 20) private String telefonoTrabajo;
    @Column(nullable = false, length = 150) private String email;
    @Column(length = 150) private String emailTrabajo;
    @Lob @Basic(fetch = FetchType.LAZY) @Column(nullable = false, columnDefinition = "LONGBLOB") private byte[] fotografia;
    @Column(nullable = false, length = 50) private String tipoFotografia;
    @Column(nullable = false, length = 150) private String nombreFotografia;
    @Column(nullable = false, length = 150) private String calle;
    @Column(nullable = false, length = 100) private String colonia;
    @Column(nullable = false, length = 100) private String municipio;
    @Column(nullable = false, length = 100) private String estado;
    @Column(nullable = false, length = 5) private String codigoPostal;
    public Long getId(){return id;} public String getNombreCompleto(){return nombreCompleto;} public void setNombreCompleto(String v){nombreCompleto=v;} public String getContactoAlternativo(){return contactoAlternativo;} public void setContactoAlternativo(String v){contactoAlternativo=v;} public Integer getEdad(){return edad;} public void setEdad(Integer v){edad=v;} public LocalDate getFechaNacimiento(){return fechaNacimiento;} public void setFechaNacimiento(LocalDate v){fechaNacimiento=v;} public String getTelefonoPersonal(){return telefonoPersonal;} public void setTelefonoPersonal(String v){telefonoPersonal=v;} public String getTelefonoTrabajo(){return telefonoTrabajo;} public void setTelefonoTrabajo(String v){telefonoTrabajo=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getEmailTrabajo(){return emailTrabajo;} public void setEmailTrabajo(String v){emailTrabajo=v;} public byte[] getFotografia(){return fotografia;} public void setFotografia(byte[] v){fotografia=v;} public String getTipoFotografia(){return tipoFotografia;} public void setTipoFotografia(String v){tipoFotografia=v;} public String getNombreFotografia(){return nombreFotografia;} public void setNombreFotografia(String v){nombreFotografia=v;} public String getCalle(){return calle;} public void setCalle(String v){calle=v;} public String getColonia(){return colonia;} public void setColonia(String v){colonia=v;} public String getMunicipio(){return municipio;} public void setMunicipio(String v){municipio=v;} public String getEstado(){return estado;} public void setEstado(String v){estado=v;} public String getCodigoPostal(){return codigoPostal;} public void setCodigoPostal(String v){codigoPostal=v;}
}
