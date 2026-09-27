# Taller Mecánico — primera etapa

Aplicación de autenticación para un sistema de órdenes de reparación: registro de clientes, inicio de sesión, recuperación de contraseña, JWT, BCrypt y autorización por roles.

## Arranque

1. Levanta MySQL: `docker compose up -d`
2. API: `mvn spring-boot:run` (puerto 8080)
3. Interfaz: `cd frontend && npm install && npm run dev` (puerto 5173)

Las personas se registran como `CLIENTE`. Al primer inicio se crea `admin@taller.local` con contraseña `Admin123!` (cámbialas con `ADMIN_EMAIL` y `ADMIN_PASSWORD`). El administrador puede crear los roles `ADMINISTRADOR`, `MECANICO`, `SECRETARIA` y `GERENTE` a través de `POST /api/usuarios`. En producción configura `JWT_SECRET`, las credenciales de BD y un proveedor de correo para entregar los enlaces de recuperación.
