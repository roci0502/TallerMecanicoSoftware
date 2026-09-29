# Fase 02 — Caso 01: Registro y consulta de clientes

## Estado documentado

Esta documentación refleja el código actual del proyecto de Gestión de Órdenes de Reparación para Taller Mecánico. Describe el módulo de clientes, sus correcciones posteriores y la integración que permanece con la fase de autenticación. No sustituye la documentación de futuras órdenes de reparación.

Diagrama actualizado: [Diagrama_componentes_fase02.md](Diagrama_componentes_fase02.md).

## Alcance y resultado actual

| Aspecto | Estado actual |
|---|---|
| Alta de cliente | Terminada. Un Administrador, Recepcionista o Secretaria puede enviar el formulario con datos y fotografía. |
| Consulta de clientes | Terminada en código. Administrador y Recepcionista disponen de una tabla con los datos guardados y su fotografía. |
| Fotografía | Terminada. Se guarda como BLOB largo en MySQL, se entrega por REST y se presenta como miniatura; existe imagen predeterminada ante ausencia o error de carga. |
| Validación y duplicados | Terminada. Hay validación en navegador/Fachada, Bean Validation, reglas de negocio e índices únicos de correo y teléfono personal. |
| Manejo de errores | Terminado. El backend devuelve JSON con la propiedad `message`; el frontend lo lee de forma segura y lo muestra como notificación. |
| Usuario CLIENTE | Terminado para esta fase. Tras iniciar sesión ve saludo, rol y botón Salir; no se le muestran Registro ni Clientes registrados. |
| Seguridad | Terminada para las rutas implementadas. JWT, contraseña BCrypt y validación de rol en el controlador protegen el alta y la consulta. |

## Fases realizadas

| Fase | Implementación realizada | Resultado |
|---|---|---|
| 1. Modelo de clientes | Se creó la entidad JPA `Client` y el esquema `clientes`, con identidad, contacto, domicilio y metadatos de imagen. | Terminada. |
| 2. Imagen persistente | `fotografia` se definió con `@Lob` y `columnDefinition = "LONGBLOB"`; la columna existente de MySQL se actualizó a `LONGBLOB`. | Terminada. |
| 3. Persistencia | Se implementó `ClientRepository` sobre JPA y búsquedas por correo y teléfono personal. | Terminada. |
| 4. Caso de uso | `ClientFacade` de Spring concentra validación de edad, imagen, duplicados, guardado y conversión de respuesta. | Terminada. |
| 5. Servicios REST | Se agregaron `POST /api/clientes`, `GET /api/clientes` y `GET /api/clientes/{id}/fotografia`. | Terminada. |
| 6. Autorización | Se valida el JWT recibido, se recupera el rol activo y se limita alta/consulta por rol. | Terminada. |
| 7. Manejo global de fallos | `GlobalExceptionHandler` normaliza errores de validación, duplicidad, carga y errores generales en JSON. | Terminada. |
| 8. Facade y Repository Vue | `ClientFacade.js` y `ClientRepository.js` encapsulan validación de imagen y llamadas REST. | Terminada. |
| 9. Interfaz de alta | Formulario de cliente, selección de imagen, indicador Guardando, limpieza posterior al éxito y notificaciones toast. | Terminada. |
| 10. Interfaz de consulta | Navegación Registrar/Clientes, carga de lista, tabla horizontalmente desplazable y miniaturas. | Terminada. |
| 11. Corrección de imágenes | El repositorio Vue descarga el binario como `Blob`, genera una URL local y usa un SVG por defecto si no hay imagen o falla. | Terminada. |
| 12. Integración con login | El login usa Facade/Repository; el rol CLIENTE recibe una pantalla restringida y los demás roles conservan su interfaz de trabajo. | Terminada. |

## Datos administrados

| Grupo | Campo / almacenamiento | Obligatorio |
|---|---|---|
| Identidad | nombreCompleto, contactoAlternativo, edad, fechaNacimiento | Sí |
| Teléfonos | telefonoPersonal, telefonoTrabajo | Sí |
| Correos | email | Sí |
| Correos | emailTrabajo | No |
| Domicilio | calle, colonia, municipio, estado, codigoPostal | Sí |
| Fotografía | fotografia (`LONGBLOB`), tipoFotografia, nombreFotografia | Sí en el alta actual |

## Reglas funcionales y de seguridad

| Regla | Aplicación actual |
|---|---|
| Alta de clientes | Solo `ADMINISTRADOR`, `RECEPCIONISTA` o `SECRETARIA`. La validación efectiva se ejecuta en `ClientController.roleOf`. |
| Consulta de clientes | Solo `ADMINISTRADOR` o `RECEPCIONISTA`, mediante `authorizeReader`. |
| Vista de CLIENTE | No contiene acciones Registrar ni Clientes registrados; solo saludo, rol y Salir. |
| Formato de texto | Nombre, contacto, colonia, municipio y estado aceptan letras, acentos y espacios según patrones del DTO. |
| Edad | Entre 0 y 130 y debe coincidir con la diferencia entre fecha de nacimiento y fecha actual. |
| Teléfonos | De 7 a 20 caracteres con números y símbolos telefónicos permitidos. |
| Código postal | Cinco dígitos. |
| Imagen | PNG, JPEG/JPG, WEBP o GIF; estrictamente menor que 7 MB, tanto en frontend como backend. |
| Duplicados | Correo y teléfono personal se revisan antes de guardar y también tienen restricciones únicas en la tabla. |
| Contraseñas | Se almacenan cifradas con BCrypt; la sesión emplea JWT. |
| Errores REST | Se responde JSON `{ "message": "..." }`; los repositorios Vue evitan intentar parsear JSON vacío. |

## Arquitectura Facade y Repository

| Capa | Componente | Responsabilidad |
|---|---|---|
| Vue | `ClientFacade.js` | Valida la fotografía y delega las operaciones de cliente. |
| Vue | `ClientRepository.js` | Construye `FormData`, usa `fetch` con JWT, convierte errores y descarga blobs de foto. |
| Spring Boot | `ClientController.java` | Expone las rutas HTTP, toma cabeceras y aplica autorización por rol. |
| Spring Boot | `ClientFacade.java` | Implementa las reglas del caso de uso y coordina el repositorio JPA. |
| Spring Boot | `ClientRepository.java` | Acceso JPA a `clientes`. |
| Vue / Spring Boot | `AuthFacade` y `AuthRepository` | Mantienen la fase de login con la misma separación Facade/Repository. |

La primera fase ya tenía `UserRepository` como repositorio JPA. En la evolución del proyecto se incorporaron `AuthFacade.java`, `AuthFacade.js` y `AuthRepository.js`, por lo que el login actual también sigue el patrón Facade + Repository.

## Código generado y comportamiento por archivo

### Backend: cliente y REST

| Archivo | Elemento | Descripción |
|---|---|---|
| `Client.java` | Entidad `Client` | Mapea `clientes`; declara restricciones únicas de correo/teléfono, campos del cliente, imagen `byte[]` como `LONGBLOB`, MIME y nombre del archivo. |
| `Client.java` | Getters y setters | Proporcionan acceso JPA a cada campo: identidad, contacto, imagen y domicilio. |
| `ClientRegistrationRequest.java` | Record `ClientRegistrationRequest(...)` | Representa la parte JSON `datos` del multipart y contiene las anotaciones `@NotBlank`, `@Pattern`, `@Email`, `@Min`, `@Max`, `@Past` y `@Size`. |
| `ClientResponse.java` | Record `ClientResponse(...)` | Define la salida del alta/listado; no expone el BLOB, expone `nombreFotografia` y `fotografiaUrl`. |
| `ClientRepository.java` | `findByEmailIgnoreCase(email)` | Busca un cliente por correo sin distinción de mayúsculas. |
| `ClientRepository.java` | `findByTelefonoPersonal(telefonoPersonal)` | Busca un cliente por teléfono personal. |
| `ClientRepository.java` | Métodos heredados de `JpaRepository` | Aporta `save`, `findAll` y `findById` usados por la fachada. |
| `ClientFacade.java` | Constructor | Recibe e inicializa la dependencia `ClientRepository`. |
| `ClientFacade.java` | `register(data, fotografia)` | Ejecuta reglas, detecta duplicados, limpia textos, obtiene bytes/MIME/nombre de la imagen, guarda y devuelve respuesta. |
| `ClientFacade.java` | `list()` | Recupera todos los clientes, los transforma a `ClientResponse` y devuelve la lista. |
| `ClientFacade.java` | `image(id)` | Busca la entidad requerida para entregar su fotografía o genera 404 si no existe. |
| `ClientFacade.java` | `validateAge(edad, birthDate)` | Compara edad declarada con la calculada desde la fecha de nacimiento. |
| `ClientFacade.java` | `validateImage(image)` | Rechaza archivo vacío, mayor o igual a 7 MB o MIME fuera de JPEG/PNG/WEBP/GIF. |
| `ClientFacade.java` | `blankToNull(value)` | Convierte correo laboral vacío a `null`; en otro caso lo recorta y normaliza. |
| `ClientFacade.java` | `response(client)` | Mapea entidad a `ClientResponse` e incluye la URL de foto REST. |
| `ClientController.java` | Constructor | Inyecta fachada de cliente, repositorio de usuarios y servicio JWT. |
| `ClientController.java` | `register(datos, fotografia, authorization)` | Atiende `POST /api/clientes`; exige JWT válido y rol de alta antes de delegar en `facade.register`. Devuelve 201. |
| `ClientController.java` | `list(authorization)` | Atiende `GET /api/clientes`; exige rol de lectura y devuelve la lista de la fachada. |
| `ClientController.java` | `photo(id, authorization)` | Atiende `GET /api/clientes/{id}/fotografia`; devuelve los bytes y el MIME guardado. Si no existen bytes, entrega SVG predeterminado. |
| `ClientController.java` | `roleOf(authorization)` | Extrae el Bearer token, obtiene correo desde JWT, verifica usuario activo y devuelve su rol; rechaza sesión ausente, inválida o expirada. |
| `ClientController.java` | `authorizeReader(authorization)` | Permite únicamente Administrador o Recepcionista para listado. |

### Backend: soporte de autenticación, seguridad y errores

| Archivo | Elemento | Descripción |
|---|---|---|
| `GlobalExceptionHandler.java` | `responseStatus(exception)` | Convierte `ResponseStatusException` a JSON con `message` y conserva su código HTTP. |
| `GlobalExceptionHandler.java` | `validation(exception)` | Devuelve el primer mensaje de validación Bean Validation con HTTP 400. |
| `GlobalExceptionHandler.java` | `uploadSize()` | Responde 413 con el límite de fotografía. |
| `GlobalExceptionHandler.java` | `integrity()` | Responde 409 cuando la base reporta integridad o duplicación. |
| `GlobalExceptionHandler.java` | `generic()` | Responde 500 JSON para fallos no controlados. |
| `SecurityConfig.java` | `passwordEncoder()` | Registra `BCryptPasswordEncoder` para guardar/verificar contraseñas. |
| `SecurityConfig.java` | `security(http)` | Configura CORS para Vue local, API sin sesión, filtro JWT y rutas protegidas. El POST multipart y URL de foto se permiten en el filtro, pero el controlador valida identidad y rol. |
| `JwtService.java` | Constructor `JwtService(secret)` | Convierte el secreto configurado en una clave HMAC para firmar y verificar tokens. |
| `JwtService.java` | `token(user)` | Emite JWT con correo como sujeto, rol, fecha de emisión y vigencia de ocho horas. |
| `JwtService.java` | `email(token)` | Verifica el JWT y obtiene el correo del sujeto. |
| `JwtFilter.java` | Constructor | Recibe el servicio JWT y el repositorio de usuarios. |
| `JwtFilter.java` | `doFilterInternal(req, res, chain)` | Lee el Bearer token, valida al usuario activo y registra la autoridad ROLE_rol en el contexto de Spring Security; continúa la cadena aun si el token es inválido. |
| `User.java` | Entidad y getters/setters | Mapea `usuarios` con nombre, correo único, contraseña cifrada, rol, estado activo y token/fecha de recuperación. |
| `UserRepository.java` | `findByEmailIgnoreCase(email)` | Busca usuarios para autenticación, autorización y prevención de correo duplicado. |
| `UserRepository.java` | `findByTokenRecuperacion(token)` | Busca al usuario que solicita restablecer contraseña. |
| `Role.java` | Enum `Role` | Declara ADMINISTRADOR, CLIENTE, MECANICO, SECRETARIA, RECEPCIONISTA y GERENTE. |
| `AuthFacade.java` | Constructor | Recibe `UserRepository`, codificador BCrypt y JWT. |
| `AuthFacade.java` | `register(nombre, email, password)` | Evita correo duplicado, crea usuario CLIENTE, cifra contraseña, guarda y emite respuesta con JWT. |
| `AuthFacade.java` | `login(email, password)` | Valida usuario activo y coincidencia BCrypt; emite JWT o 401. |
| `AuthFacade.java` | `requestPasswordRecovery(email)` | Si existe el usuario, guarda token UUID y expiración de 30 minutos. |
| `AuthFacade.java` | `resetPassword(token, password)` | Verifica token/expiración, cifra nueva contraseña, limpia token y guarda. |
| `AuthFacade.java` | `response(user)` | Construye `AuthResponse` con JWT, nombre y rol. |
| `AuthController.java` | Records `Registro`, `Login`, `Recuperar`, `Restablecer` | Definen cuerpos de cada endpoint y sus validaciones. |
| `AuthController.java` | `registrar(r)`, `login(l)`, `olvidar(r)`, `reset(r)` | Exponen, respectivamente, registro, inicio de sesión, solicitud y restablecimiento de contraseña. Delegan en `AuthFacade`. |
| `UserController.java` | Constructor | Recibe el repositorio de usuarios y el codificador de contraseña. |
| `UserController.java` | Records `Perfil` y `Alta` | Representan la respuesta de perfil y el cuerpo para alta administrativa de usuarios. |
| `UserController.java` | `yo(authentication)` | Devuelve el perfil del usuario autenticado en GET `/api/usuarios/me`. |
| `UserController.java` | `listar()` | Devuelve perfiles de usuarios en GET `/api/usuarios`; la configuración lo reserva a Administrador. |
| `UserController.java` | `alta(r)` | Valida rol/correo, cifra la contraseña y crea un usuario en POST `/api/usuarios`. |
| `UserController.java` | `perfil(user)` | Convierte la entidad `User` en el record no sensible `Perfil`. |
| `InitialData.java` | `adminInicial(users, encoder, email, password)` | Registra el CommandLineRunner que crea un Administrador inicial solo si el correo configurado aún no existe. |
| `application.yml` | datasource, JPA, multipart, puerto y JWT | Configura MySQL, `ddl-auto: update`, límites multipart de 7 MB, puerto 8081 y valores reemplazables por variables de entorno. |

### Frontend: fachada, repositorio e interfaz

| Archivo | Elemento | Descripción |
|---|---|---|
| `frontend/src/facades/ClientFacade.js` | `validatePhoto(photo)` | Valida foto obligatoria, peso menor de 7 MB y MIME permitido antes del REST. |
| `frontend/src/facades/ClientFacade.js` | `register(client, photo, token)` | Valida y delega el alta al repositorio. |
| `frontend/src/facades/ClientFacade.js` | `list(token)` | Delega la consulta de clientes. |
| `frontend/src/facades/ClientFacade.js` | `photo(path, token)` | Delega la descarga de una fotografía. |
| `frontend/src/repositories/ClientRepository.js` | `parse(response)` | Lee el cuerpo como texto, intenta JSON solo si existe y lanza un mensaje útil ante HTTP no exitoso. |
| `frontend/src/repositories/ClientRepository.js` | `register(client, photo, token)` | Crea `FormData`, adjunta JSON como `datos` y archivo como `fotografia`, y llama POST protegido por Bearer. |
| `frontend/src/repositories/ClientRepository.js` | `list(token)` | Llama GET de clientes con JWT y procesa su JSON. |
| `frontend/src/repositories/ClientRepository.js` | `photo(path, token)` | Solicita bytes de foto, genera URL con `URL.createObjectURL` y permite mostrar el blob en una etiqueta `img`. |
| `frontend/src/facades/AuthFacade.js` | `login`, `register`, `requestRecovery` | Delega los tres flujos de autenticación al repositorio. |
| `frontend/src/facades/AuthFacade.js` | `saveSession(data)` | Guarda solamente nombre, rol y JWT en `localStorage` y devuelve la sesión. |
| `frontend/src/facades/AuthFacade.js` | `clearSession()` | Elimina la sesión local. |
| `frontend/src/repositories/AuthRepository.js` | `post(path, body)` | Centraliza POST JSON, lectura segura del cuerpo y propagación de `message`. |
| `frontend/src/repositories/AuthRepository.js` | `login`, `register`, `requestRecovery` | Preparan las rutas y cuerpos de los endpoints del login. |
| `frontend/src/main.js` | `emptyClient()` | Devuelve un modelo limpio para el formulario de cliente. |
| `frontend/src/main.js` | `defaultPhoto` | Define SVG de perfil usado al no haber foto o si no se puede cargar. |
| `frontend/src/main.js` | `puedeAlta` | Indica si el rol puede ver/usar el formulario. |
| `frontend/src/main.js` | `puedeLista` | Indica si el rol puede consultar la tabla. |
| `frontend/src/main.js` | `notificar(texto, tipo)` | Muestra toast temporal y reinicia su temporizador. |
| `frontend/src/main.js` | `seleccionarFoto(event)` | Conserva el archivo seleccionado y su nombre. |
| `frontend/src/main.js` | `limpiarFormulario()` | Restablece campos, archivo, nombre y control de entrada de foto. |
| `frontend/src/main.js` | `iniciarSesion()` | Inicia sesión por fachada, guarda sesión, muestra toast y siempre libera el estado de carga. |
| `frontend/src/main.js` | `crearCuenta()` | Verifica confirmación, registra usuario, guarda sesión y libera el estado de carga. |
| `frontend/src/main.js` | `recuperarCuenta()` | Solicita recuperación y muestra su mensaje. |
| `frontend/src/main.js` | `registrarCliente()` | Activa carga, registra, limpia el formulario solo tras éxito, muestra toast de éxito/error y siempre restablece el botón en `finally`. |
| `frontend/src/main.js` | `cargarClientes()` | Cambia a lista, consulta clientes, descarga en paralelo sus fotos y coloca la predeterminada al fallar. |
| `frontend/src/main.js` | `fotoError(event)` | Sustituye una imagen rota por el SVG predeterminado. |
| `frontend/src/main.js` | `salir()` | Elimina sesión por fachada y vuelve al estado de acceso. |
| `frontend/src/main.js` | Template Vue | Conserva el panel visual del taller, muestra saludo para CLIENTE, formulario para roles de alta y tabla para roles de lectura. |
| `frontend/src/style.css` | Estilos | Define el tema azul, distribución de paneles, formulario, toast, tabla con desplazamiento horizontal, miniaturas y adaptación móvil. |

## Endpoints implementados

| Método y ruta | Uso | Autorización efectiva |
|---|---|---|
| POST `/api/clientes` | Guarda datos y fotografía multipart. | Administrador, Recepcionista o Secretaria. |
| GET `/api/clientes` | Devuelve `ClientResponse[]`. | Administrador o Recepcionista. |
| GET `/api/clientes/{id}/fotografia` | Devuelve imagen binaria/MIME o SVG predeterminado. | La URL es permitida en filtro para permitir uso de imagen; la petición de lista sí está restringida. |
| POST `/api/auth/register` | Crea usuario CLIENTE y devuelve JWT. | Público. |
| POST `/api/auth/login` | Autentica y devuelve JWT. | Público. |
| POST `/api/auth/forgot-password` | Genera token temporal si el correo existe. | Público. |
| POST `/api/auth/reset-password` | Restablece contraseña con token válido. | Público. |

## Estado pendiente

| Pendiente | Alcance futuro |
|---|---|
| Pruebas automatizadas | Crear pruebas unitarias para fachada y pruebas de integración REST/JPA. |
| Multiempresa/multitaller | Añadir entidad/relación Taller; los duplicados deberán evaluarse dentro del taller correspondiente. |
| Gestión completa | Editar, desactivar, eliminar y búsqueda de clientes no forman parte de este caso. |
| Órdenes de reparación | Es la siguiente etapa funcional; aún no existe relación cliente–vehículo–orden. |
| Recuperación con entrega real | El token se genera y almacena; falta la integración de correo o canal de entrega al usuario. |
| Producción | Sustituir secretos por variables seguras, restringir CORS y valorar almacenamiento externo de imágenes. |
