package com.taller.user;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="usuarios") public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=100) private String nombre;
 @Column(nullable=false,unique=true,length=150) private String email;
 @Column(nullable=false) private String password;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Role rol;
 @Column(nullable=false) private boolean activo=true;
 @Column(name="token_recuperacion",length=100) private String tokenRecuperacion;
 private Instant tokenExpira;
 public Long getId(){return id;} public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPassword(){return password;} public void setPassword(String v){password=v;} public Role getRol(){return rol;} public void setRol(Role v){rol=v;} public boolean isActivo(){return activo;} public void setActivo(boolean v){activo=v;} public String getTokenRecuperacion(){return tokenRecuperacion;} public void setTokenRecuperacion(String v){tokenRecuperacion=v;} public Instant getTokenExpira(){return tokenExpira;} public void setTokenExpira(Instant v){tokenExpira=v;}
}
