# Planeación y documentación — Fase inicial: Login

## 1. Propósito y alcance

La primera fase implementa la base de identidad, autenticación y autorización de la aplicación **Gestión de Órdenes de Reparación para Taller Mecánico**. Su objetivo es que una persona pueda crear una cuenta de cliente, iniciar sesión y recibir un token de acceso; además, deja preparada la administración de los roles requeridos.

Esta fase **no** incluye todavía órdenes de reparación, vehículos, diagnósticos, refacciones, pagos, calendario ni notificaciones. Tampoco constituye aún un despliegue productivo completo.

| Capa | Tecnología implementada | Propósito |
|---|---|---|
| Backend | Java 21, Spring Boot 3.5.8, Spring Web, Spring Security, Spring Data JPA | API REST, reglas de autenticación y persistencia |
| Persistencia | MySQL, Hibernate/JPA | Tabla de usuarios y tokens de recuperación |
| Seguridad | BCrypt, JWT (JJWT 0.12.6) | No almacenar contraseñas en texto plano y proteger rutas |
| Frontend | Vue 3, Vite 6, JavaScript y CSS | Pantallas de acceso y comunicación con la API |
| Desarrollo local | Docker Compose | Definición de servicio MySQL local |

## 2. Resultado de las etapas desarrolladas

| Etapa | Desarrollo realizado | Resultado objetivo | Estado actual |
|---|---|---|---|
| 1. Estructura del backend | Se creó el proyecto Maven, la clase principal y sus dependencias. | API Spring Boot ejecutable. | Implementado; requiere descargar dependencias y arrancar la API. |
| 2. Base de datos | Se definió MySQL y JPA con creación/actualización de esquema. | Persistir usuarios en `taller_mecanico`. | Configurado. El usuario MySQL necesita privilegios sobre la base para persistir. |
| 3. Modelo de identidad | Se creó entidad `User`, repositorio y catálogo de roles. | Representar usuarios, estado y recuperación. | Implementado. |
| 4. Registro e inicio de sesión | Se crearon endpoints de registro y login, validación y respuesta con JWT. | Crear cliente y autenticarlo posteriormente. | Implementado en código; depende de API y permisos MySQL operativos. |
| 5. Autorización | Se configuró filtro JWT y reglas por rol. | Limitar recursos según usuario autenticado y rol. | Implementado. |
| 6. Recuperación de contraseña | Se crearon endpoints para generar token temporal y restablecer contraseña. | Recuperar acceso de forma segura. | Parcial: token y restablecimiento existen; falta envío de correo/enlace. |
| 7. Administración inicial | Se agregó administrador inicial y endpoint protegido para altas de personal. | Disponer de roles internos. | Implementado en API; no hay pantalla administrativa aún. |
| 8. Interfaz de acceso | Se creó pantalla Vue responsiva en tonos azules. | Login, registro y solicitud de recuperación amigables. | Implementado y compilado con Vite. |

## 3. Módulos desarrollados

### 3.1 Módulo de usuarios y roles

| Elemento | Descripción | Estado |
|---|---|---|
| Registro de clientes | `POST /api/auth/register` valida nombre, correo y contraseña; siempre asigna `CLIENTE`. | Terminado en API. |
| Alta interna de usuarios | `POST /api/usuarios` permite a un administrador crear usuarios con rol seleccionado. | Terminado en API; sin pantalla web. |
| Consulta del propio perfil | `GET /api/usuarios/me` devuelve el perfil del usuario autenticado. | Terminado en API. |
| Listado de usuarios | `GET /api/usuarios` devuelve usuarios sin contraseñas. | Terminado en API; solo administrador. |
| Administrador inicial | En el arranque se crea un administrador si su correo no existe. | Terminado; cambiar credenciales antes de producción. |

#### Roles disponibles

| Rol | Uso actual en la fase |
|---|---|
| `ADMINISTRADOR` | Puede listar y dar de alta usuarios en `/api/usuarios`. |
| `CLIENTE` | Rol asignado por el registro público. |
| `MECANICO` | Definido para fases posteriores de órdenes y diagnósticos. |
| `SECRETARIA` | Definido para fases posteriores de atención y captura. |
| `GERENTE` | Definido para fases posteriores de reportes y supervisión. |

> Los roles de mecánico, secretaria y gerente existen en el catálogo y pueden ser creados por el administrador. Sus permisos específicos sobre órdenes todavía no se han desarrollado porque esos módulos pertenecen a fases posteriores.

### 3.2 Módulo de autenticación

| Acción | Endpoint | Datos de entrada | Respuesta / comportamiento |
|---|---|---|---|
| Registrar cliente | `POST /api/auth/register` | `nombre`, `email`, `password` | Crea usuario `CLIENTE`, cifra contraseña y devuelve JWT, nombre y rol. |
| Iniciar sesión | `POST /api/auth/login` | `email`, `password` | Comprueba contraseña y estado activo; devuelve JWT, nombre y rol. |
| Solicitar recuperación | `POST /api/auth/forgot-password` | `email` | Crea token UUID válido por 30 minutos si el correo existe; devuelve mensaje genérico. |
| Restablecer contraseña | `POST /api/auth/reset-password` | `token`, `password` | Verifica vigencia, cifra la nueva contraseña e invalida el token. |
| Cerrar sesión | Acción local de Vue | — | Elimina `usuario` de `localStorage`. No existe invalidación de JWT del lado del servidor. |

### 3.3 Controles de seguridad implementados

| Control | Implementación concreta | Archivo |
|---|---|---|
| Cifrado de contraseñas | `BCryptPasswordEncoder`; se almacena el hash, no la contraseña original. | `src/main/java/com/taller/security/SecurityConfig.java` |
| Validación de datos | `@NotBlank`, `@Email`, `@Size(min=8)` en DTOs de las solicitudes. | `src/main/java/com/taller/auth/AuthController.java` |
| Sesión sin estado | JWT firmado con clave HMAC y expiración de 8 horas. | `src/main/java/com/taller/security/JwtService.java` |
| Lectura de token | Filtro que toma `Authorization: Bearer <token>`, valida y carga el rol autenticado. | `src/main/java/com/taller/security/JwtFilter.java` |
| Restricción de rutas | `/api/auth/**` público; perfil autenticado; `/api/usuarios/**` para administrador. | `src/main/java/com/taller/security/SecurityConfig.java` |
| Evitar enumeración de correos | Recuperación responde el mismo mensaje aunque el correo no exista. | `src/main/java/com/taller/auth/AuthController.java` |
| Recuperación temporal | UUID de recuperación, expiración de 30 minutos e invalidación tras usarse. | `src/main/java/com/taller/auth/AuthController.java` |

## 4. Datos y entidad persistida

### Entidad `usuarios`

La entidad Java `User` se mapea a la tabla MySQL `usuarios`. Hibernate crea o actualiza el esquema al iniciar, debido a `spring.jpa.hibernate.ddl-auto=update`.

| Campo Java / columna | Tipo | Regla | Descripción |
|---|---|---|---|
| `id` | `Long` / identidad | Llave primaria autogenerada | Identificador del usuario. |
| `nombre` | `String(100)` | Obligatorio | Nombre completo mostrado en la interfaz. |
| `email` | `String(150)` | Obligatorio y único | Identificador de inicio de sesión; se guarda en minúsculas al registrarse. |
| `password` | `String` | Obligatorio | Hash BCrypt de la contraseña. Nunca debe devolverse en respuestas. |
| `rol` | Enum String(20) | Obligatorio | Uno de los cinco roles definidos. |
| `activo` | boolean | Obligatorio; valor inicial `true` | Permite bloquear el acceso sin borrar la cuenta. |
| `token_recuperacion` | `String(100)` | Opcional | Token UUID para restablecer contraseña. |
| `tokenExpira` | `Instant` | Opcional | Fecha/hora de expiración del token de recuperación. |

## 5. Pantallas y comportamiento del frontend

| Vista | Elementos | Comportamiento |
|---|---|---|
| Inicio de sesión | Correo, contraseña, botón **Ingresar**, enlaces de registro y recuperación. | Envía `login`; al recibir JWT guarda `{nombre, rol, token}` en `localStorage`. |
| Crear cuenta | Nombre, correo, contraseña y confirmación. | Verifica que ambas contraseñas coincidan y envía `register`. |
| Recuperar acceso | Campo de correo y botón **Continuar**. | Envía `forgot-password` y muestra el mensaje devuelto. |
| Sesión activa | Saludo con nombre, rol y botón **Cerrar sesión**. | Se muestra si hay un objeto `usuario` en `localStorage`. |

La aplicación frontend consulta por defecto `http://localhost:8081/api`. Este valor puede sustituirse al construir mediante la variable `VITE_API_URL`.

## 6. Inventario de archivos generados

### Backend

| Archivo | Responsabilidad |
|---|---|
| `pom.xml` | Dependencias, Java 21, empaquetado Spring Boot. |
| `src/main/java/com/taller/TallerApplication.java` | Punto de entrada de Spring Boot. |
| `src/main/java/com/taller/user/User.java` | Entidad JPA de usuarios. |
| `src/main/java/com/taller/user/Role.java` | Enumeración de roles. |
| `src/main/java/com/taller/user/UserRepository.java` | Acceso JPA por correo y token. |
| `src/main/java/com/taller/user/UserController.java` | Perfil, listado y alta administrativa. |
| `src/main/java/com/taller/auth/AuthController.java` | Registro, login y recuperación/restablecimiento. |
| `src/main/java/com/taller/security/JwtService.java` | Emisión y lectura de JWT. |
| `src/main/java/com/taller/security/JwtFilter.java` | Autenticación por encabezado Bearer. |
| `src/main/java/com/taller/security/SecurityConfig.java` | BCrypt, CORS, rutas públicas/protegidas y filtro. |
| `src/main/java/com/taller/config/InitialData.java` | Creación condicionada del administrador inicial. |
| `src/main/resources/application.yml` | Configuración de aplicación, puerto, MySQL y JWT. |

### Frontend, infraestructura y apoyo

| Archivo | Responsabilidad |
|---|---|
| `frontend/package.json` | Dependencias y comandos `npm run dev` / `npm run build`. |
| `frontend/index.html` | Punto de montaje de Vue. |
| `frontend/src/main.js` | Estado, llamadas REST, validaciones de pantalla y plantilla visual. |
| `frontend/src/style.css` | Diseño responsivo y tema visual azul. |
| `docker-compose.yml` | Servicio local MySQL 8.4, volumen y puerto 3306. |
| `.env.example` | Variables de entorno de ejemplo; no se carga automáticamente sin una herramienta que lo haga. |
| `.gitignore` | Excluye dependencias, compilados, secretos locales y carpetas temporales. |
| `README.md` | Guía breve original del proyecto. Debe actualizarse: indica puerto 8080, pero `application.yml` usa 8081. |

## 7. Credenciales y configuración de conexión

Las credenciales de desarrollo están escritas como valores predeterminados en los archivos siguientes. Para producción no deben permanecer en el repositorio ni en texto plano.

| Archivo | Variables / datos que contiene | Uso |
|---|---|---|
| `src/main/resources/application.yml` | `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `PORT`, `FRONTEND_URL` | Configuración efectiva del backend con valores predeterminados si no existen variables de entorno. |
| `.env.example` | Ejemplo de `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Plantilla para variables de entorno; no es un archivo secreto ni lo consume Spring automáticamente por sí solo. |
| `docker-compose.yml` | `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` | Crea el contenedor MySQL local y sus credenciales iniciales. |
| `src/main/java/com/taller/config/InitialData.java` | Valores por defecto de `ADMIN_EMAIL` y `ADMIN_PASSWORD` | Crea el administrador inicial si no se suministran variables de entorno. |

**Ubicación principal de conexión de la API:** `src/main/resources/application.yml`.

### Medidas obligatorias antes de publicar

1. Establecer `DB_URL`, `DB_USER` y `DB_PASSWORD` como secretos del proveedor, no como valores en el código.
2. Definir un `JWT_SECRET` único, aleatorio y de más de 32 caracteres.
3. Definir `ADMIN_EMAIL` y `ADMIN_PASSWORD` seguros antes del primer arranque.
4. Eliminar o cambiar las credenciales de desarrollo predeterminadas.
5. Conceder al usuario de aplicación permisos solo sobre `taller_mecanico`, no permisos globales de MySQL.
6. Incorporar servicio SMTP/transaccional antes de anunciar la recuperación de contraseña como funcional por correo.

## 8. Estado objetivo de finalización de la fase

| Módulo | Estado | Evidencia / límite actual |
|---|---|---|
| Modelo, roles y endpoints REST | Terminado en código | Clases de entidad, repositorio, controladores y seguridad presentes. |
| Interfaz login/registro | Terminado en código y compilación | Vue/Vite compila; se visualizó en `localhost:5173`. |
| Registro persistente MySQL | Pendiente de validación final | Se requiere que la API esté activa y que el usuario MySQL tenga permisos sobre la base. |
| Login posterior al registro | Pendiente de prueba integral | El endpoint existe y valida BCrypt; depende de la persistencia anterior. |
| Recuperación por token | Terminado en API | `forgot-password` genera token y `reset-password` lo consume. |
| Recuperación por correo real | Pendiente | No existe dependencia, configuración SMTP ni envío de enlace. |
| Administración de roles desde UI | Pendiente | Solo hay endpoint API protegido para administrador. |

## 9. Ejecución local para validar la fase

1. Instalar Java 21, Maven, Node.js (18.14 o superior), npm, Docker y Docker Compose.
2. Desde la raíz del proyecto, crear/iniciar MySQL con `docker compose up -d`.
3. Confirmar que el usuario de aplicación tiene privilegios para crear/actualizar tablas en la base `taller_mecanico`.
4. En una terminal, ejecutar `mvn spring-boot:run`. La configuración actual publica la API en el puerto **8081**.
5. En otra terminal, entrar en `frontend`, ejecutar `npm install` y después `npm run dev`.
6. Abrir `http://localhost:5173`.
7. Crear una cuenta con correo no registrado y contraseña de al menos 8 caracteres.
8. Cerrar sesión y entrar con el mismo correo y contraseña para verificar persistencia.

## 10. Publicación recomendada

### Arquitectura propuesta

| Componente | Software/servicio recomendado | Motivo |
|---|---|---|
| Repositorio y control de versiones | GitHub | Despliegue automático por cada cambio validado. |
| Frontend Vue/Vite | Netlify | Sirve el contenido estático generado en `frontend/dist`. |
| Backend Spring Boot | Railway | Puede desplegar el servicio Java y convivir con una base MySQL privada. |
| Base de datos | Railway MySQL | Ofrece variables `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD` y `MYSQLDATABASE` para servicios del mismo proyecto. |

Netlify detecta proyectos Vite y propone `npm run build` como comando de construcción y `dist` como directorio de publicación. Railway permite agregar un MySQL y conectarlo a otro servicio usando variables internas. Fuentes: [Netlify: Vite](https://docs.netlify.com/build/frameworks/framework-setup-guides/vite/), [Railway: MySQL](https://docs.railway.com/databases/mysql).

### 10.1 Publicar backend y MySQL con Railway

1. Crear una cuenta en Railway y un proyecto nuevo.
2. En el proyecto, elegir **New** y agregar una base de datos **MySQL**.
3. Importar el repositorio GitHub que contiene este proyecto y crear un servicio para el backend.
4. Configurar el directorio raíz del servicio como la raíz del repositorio (donde está `pom.xml`).
5. Configurar el comando de construcción: `mvn -DskipTests package`.
6. Configurar el comando de inicio: `java -jar target/taller-mecanico-api-1.0.0.jar`.
7. Crear las variables del backend: `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` y `FRONTEND_URL`.
8. Formar `DB_URL` con las variables privadas de MySQL: `jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=true&serverTimezone=UTC`.
9. Configurar `PORT` con el valor que asigne Railway; Spring ya lee `${PORT:8081}`.
10. Desplegar, obtener la URL pública HTTPS del backend y probar `POST /api/auth/register`.

### 10.2 Publicar frontend con Netlify

1. En Netlify, elegir **Add new project** e importar el mismo repositorio GitHub.
2. Definir **Base directory**: `frontend`.
3. Definir **Build command**: `npm run build`.
4. Definir **Publish directory**: `frontend/dist` si Netlify interpreta el directorio desde la raíz del repositorio; si lo interpreta desde el directorio base, usar `dist`.
5. Crear la variable de entorno `VITE_API_URL` con la URL HTTPS publicada del backend seguida de `/api`; por ejemplo, `https://api-taller.ejemplo.com/api`.
6. Publicar el sitio y copiar su URL HTTPS final.
7. En el backend, ajustar la lista CORS de `SecurityConfig.java`, reemplazando `http://localhost:5173` por la URL HTTPS de Netlify. Este cambio es necesario para que el navegador permita las llamadas desde producción.
8. Actualizar `FRONTEND_URL` en Railway con la misma URL de Netlify y volver a desplegar el backend.
9. Verificar registro, cierre de sesión e inicio de sesión con una cuenta recién creada.

> Para una publicación segura no expongas MySQL al internet público si backend y base están en el mismo proyecto Railway. La conexión privada es preferible; habilita acceso público solo si se requiere administrar la base desde una herramienta externa y protegela con controles adicionales.

## 11. Trabajo propuesto para la siguiente fase

| Prioridad | Trabajo |
|---|---|
| Alta | Concluir prueba integral de registro/login con MySQL y credenciales no predeterminadas. |
| Alta | Integrar envío SMTP o proveedor transaccional y pantalla de restablecimiento mediante enlace. |
| Alta | Crear panel administrativo para usuarios, roles, activación y desactivación. |
| Media | Definir permisos concretos por rol para los módulos de órdenes. |
| Media | Implementar entidades `Vehiculo`, `OrdenReparacion`, diagnóstico, servicios y estados. |
| Media | Agregar pruebas unitarias e integración para autenticación, CORS y autorización. |
