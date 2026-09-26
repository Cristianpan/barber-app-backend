# Convenciones de codificación — barber-app-backend

Paquete base: `com.la_navaja.backend`. Capas/diagrama: `docs/architecture.md`.

Hexagonal simplificada, tres capas. Dependencia: `infrastructure -> application -> domain`. `domain` no depende de nada.

## Dónde va cada cosa

| Tipo de código | Carpeta |
|---|---|
| Entidad de dominio | `domain/models` |
| Enum de dominio (rol, status, …) | `domain/models` |
| Excepción de dominio | `domain/exceptions` |
| Constante / mensaje de excepción | `domain/constants` |
| Caso de uso | `application/services` |
| Interfaz de repositorio (solo domain objects) | `application/repositories` |
| Puerto a integración externa (correo, pagos, …) | `application/ports` |
| Validador de reglas de negocio | `application/validators` |
| Utilidad | `application/utils` |
| DTO de entrada / salida | `application/dtos/request` / `application/dtos/response` |
| Entity JPA (`@Entity`) | `infrastructure/schemas` |
| Mapper schema <-> model | `infrastructure/mappers` |
| Config (app, JWT, security, …) | `infrastructure/config` |
| REST controller | `infrastructure/controllers` |
| `@RestControllerAdvice` global | `infrastructure/controllers/advice` |
| Implementación de repositorio (`JpaRepository`) | `infrastructure/repositories` |
| Adaptador de integración externa (implementa un puerto) | `infrastructure/adapters/<integración>` (ej. `adapters/mail`) |

## Código

- **Models y DTOs**: `record` por defecto; `class` solo si necesita mutabilidad o comportamiento. Lombok solo para boilerplate real (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`; `@Builder` también en `record`). Sin JPA en `domain/models`. Para crear objetos a insertar/actualizar, preferir builder.
- **Repositorios (`application`)**: solo conocen `domain/models`, nunca schemas.
- **Mappers**: `schema <-> model`, solo en `infrastructure/mappers`. `application` y `domain` nunca importan schemas.
- **Ports/adapters**: integración no-persistencia → el service depende de un puerto (solo tipos de `domain`); la librería externa vive en el adaptador, que traduce sus errores a excepciones de `domain/exceptions` (tipos de la librería no salen del adaptador). Los repositorios no cambian.
- **DTOs**: siempre separados en `request` y `response`.
- **Services**: clases directas, sin interfaz. Uno por controller (ver Naming).
- **Usuario autenticado**: solo los controllers leen el `SecurityContext` vía `AuthenticatedUserProvider.getAuthenticatedUser()` (`infrastructure/config/security`). El service lo recibe como parámetro `AuthenticatedUser` (`domain/models`, usa `.id()`, `.role()`); `application` no importa Spring Security.
- **Validación de forma**: constraints (`@NotBlank`, `@Email`, …) en el DTO request; `@Valid` en el parámetro del controller (lo único que la dispara). El advice global captura el fallo.
- **Validators**: solo reglas de negocio que las anotaciones no expresan; al fallar lanzan excepción genérica por status.

## Naming

- Repository, tres nombres (uno por capa):
  - `application/repositories/<Model>Repository` — interfaz, solo domain models (`UserRepository`).
  - `infrastructure/repositories/<Entity>JpaRepository` — interfaz Spring Data, tipada al schema (`UserJpaRepository`).
  - `infrastructure/repositories/<Model>RepositoryImpl` — implementación: usa la `JpaRepository` y el mapper (`UserRepositoryImpl`).
- Controller: por funcionalidad, no por entity. Login + registro → `AuthController`.
- Service: uno por controller. `AuthController` → `AuthService`.

## Excepciones

`domain/exceptions` son **genéricas y globales**, nombradas por **status HTTP**, no por motivo de negocio:

| Excepción | Status |
|---|---|
| `BadRequestException` | 400 (regla de negocio con dato inválido) |
| `UnauthorizedException` | 401 |
| `ForbiddenException` | 403 |
| `ResourceNotFoundException` | 404 |
| `ResourceAlreadyExistsException` | 409 |

Lista base, no cerrada: elegir el status más apropiado (no todo es 400). Si ninguno encaja, crear otra genérica por status (ej. `UnprocessableEntityException` 422). Nunca una por entidad/causa (`UserNotFoundException` ❌). Errores de forma del request no usan estas: los resuelve el advice con `400` genérico.

El **motivo** va en el **mensaje**: constante en `domain/constants` (`ErrorMessages`), texto en español, nunca hardcodeado en el `throw` ni en la clase.

```java
// domain/constants/ErrorMessages.java
public static final String USER_NOT_FOUND_MESSAGE = "El usuario no ha sido encontrado";

// ✅
throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND_MESSAGE);
// ❌
throw new UserNotFoundException();
throw new ResourceNotFoundException("no existe");
```

Si el mensaje no debe revelar la causa (ej. login: no decir si falló correo o contraseña), usar constante genérica (`INVALID_CREDENTIALS_MESSAGE`). Si no es obvio, documentar como `D<n>` en `design.md`.

## Manejo global de errores

Un único `@RestControllerAdvice` (`infrastructure/controllers/advice`); sin handlers por controller. Formato único:

```json
{ "message": "El usuario no ha sido encontrado", "body": null }
```

- `message`: siempre. `body`: opcional, por defecto omitido; nunca detalle de validación, stacktrace ni datos internos.
- **Excepciones de dominio**: su status; `message` = constante del motivo.
- **Forma del request** (`MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `ConstraintViolationException`): `400` con `INVALID_REQUEST_MESSAGE = "Solicitud inválida"`, sin decir campo ni regla. Detalle solo en logs `WARN`.
- **No controladas (500)**: `INTERNAL_ERROR_MESSAGE = "Error interno"`; stacktrace solo en logs.

Prohibidas constantes por campo/regla de forma (`MISSING_EMAIL`, `INVALID_EMAIL_FORMAT`, …).

## OpenAPI

Todo endpoint lleva `@Operation` + `@ApiResponses` (`springdoc`):

- **Éxito**: `@ApiResponse` con status real (`200`, `201`, `204`, …) y `schema` del DTO response.
- **Error**: un `@ApiResponse` por cada status que el flujo puede emitir vía advice (`400`, `404`, `409`, `500`, …). `description` = mensaje real de la constante (o el genérico del advice); schema = `{message, body}`. No documentar status que el endpoint no puede producir.
- **Agrupación por módulo (obligatoria)**: un bean `GroupedOpenApi` por prefijo de ruta en `infrastructure/config` y `@Tag` en cada controller. Módulo nuevo ⇒ grupo nuevo.

```java
@Bean
GroupedOpenApi authApi() {
    return GroupedOpenApi.builder().group("auth").pathsToMatch("/auth/**").build();
}

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
class AuthController { ... }
```

El `reviewer` rechaza endpoints sin status de éxito/error documentados o fuera del grupo de su módulo.

## Relaciones JPA: sin query extra por ID

Si en un schema (`@ManyToOne`, `@OneToOne`) solo se tiene el ID de la entity relacionada, **no** usar `findById` solo para setear la referencia. Usar `EntityManager#getReference(TargetSchema.class, id)` (proxy, sin query).

```java
// mal
ClientSchema client = clientJpaRepository.findById(clientId).orElseThrow();
// bien
ClientSchema clientRef = entityManager.getReference(ClientSchema.class, clientId);
appointment.setClient(clientRef);
```

Va en `infrastructure/repositories` (el mapper no tiene `EntityManager`); nunca en `domain`/`application`.

## Testing

**Los tests nacen de los requirements (`R<n>`), no de las clases.** Validan el requisito, no la implementación. Un test puede cubrir varios `R<n>`.

### Qué se prueba

Cada `R<n>` con lógica propia tiene al menos un test; un `R<n>` puramente declarativo (constraint JPA, enum) no lleva test. Un test que no sale de un `R<n>` no se escribe.

Cada service tiene su test de service; su controller, su test de controller. Sin solape:

| Clase | Herramienta | Valida |
|---|---|---|
| `XxxServiceTest` | Mockito puro, sin Spring | Reglas de negocio del `R<n>`: resultado, excepciones (tipo genérico + mensaje de constante), efectos sobre repos/puertos |
| `XxxControllerTest` | `@WebMvcTest`, service `@MockitoBean`, importando security/advice | Contrato HTTP: status, forma de respuesta, `{message, body}`, request inválido → `400` genérico, auth/roles, mapeo de excepciones por el advice |

La rama de negocio se prueba solo en el service test. El controller test no repite lógica: un test por cada status distinto que llega al cliente.

### Qué NO se prueba

Utils, validators, constraints individuales de Hibernate Validator (solo el contrato "request inválido → `400`" una vez por endpoint), mappers, adapters, repositories (SQL), config, models, terceros, conexión a BD. Se cubren indirectamente. Solo mocks: sin BD real ni contenedores (Testcontainers/`@SpringBootTest`), sin excepción.

Tampoco: decisiones `D<n>`, aserciones sobre logs o "el secreto no se filtra", flujos de otras features, validación defensiva que ningún `R<n>` exige.

### Mocks

Mockear lo que sale del proceso: repositorios, puertos/integraciones externas y `Clock`. Lo interno sin dependencias externas (validators, utils, mappers) se usa **real** en el service test.

### Tests significativos

- **Happy path: uno** por `R<n>`/flujo, con asserts completos: resultado **y** efecto en el borde (`ArgumentCaptor` del objeto guardado, `verify` del puerto con argumentos correctos).
- **No-happy: uno por resultado distinto** (otro status, mensaje o efecto). Varias causas con mismo resultado → un solo test (o `@ParameterizedTest` corto).
- Sin casos extremos (longitudes, unicode, combinatorias) salvo que un `R<n>` lo exija.
- Asserts sobre resultado observable: evitar `verifyNoMoreInteractions`, orden de llamadas, estructura interna.
- Un solo `shouldRejectInvalidRequest` por operación para toda validación de forma, salvo regla de negocio con resultado distinto.

### Nombres y agrupación

- Inglés, `camelCase`, prefijo `should`, describe comportamiento: `shouldSetPasswordWithValidToken`, `shouldRejectExistingEmail`. Con condición: `shouldRejectRegistration_whenEmailAlreadyExists`. Sin `@DisplayName`.
- `@Nested` por flujo (endpoint en controller test, método/caso de uso en service test), nombrado `<Operación>Tests`. Mocks (`@Mock`, `@MockitoBean`, `@InjectMocks`) en la clase exterior.

```java
class AuthControllerTest {

    @Nested
    class LoginTests {
        @Test void shouldLoginSuccessfully() { ... }
        @Test void shouldRejectInvalidCredentials() { ... }   // 401
        @Test void shouldRejectInvalidRequest() { ... }       // 400 genérico
    }

    @Nested
    class RegisterTests {
        @Test void shouldRegisterUserSuccessfully() { ... }
        @Test void shouldRejectExistingEmail() { ... }        // 409
        @Test void shouldRejectInvalidRequest() { ... }
    }
}
```

Sin número fijo de tests: importa que los `R<n>` queden cubiertos. Trade-off aceptado: menos precisión sobre qué falló, a cambio de tests que no se rompen al refactorizar.
