# Design — 1 · Autenticar usuario por correo y contraseña

## Decisiones

- **D1** `POST /auth/sign-in` en `AuthController` → `AuthService`; público (`permitAll`), stateless, CSRF desactivado. (R1)
- **D2** Añadir `findByEmail(String email)` a `UserRepository` (application) y `UserJpaRepository` (infrastructure); la búsqueda normaliza el correo a minúsculas. (R1, R2)
- **D3** Correo inexistente o contraseña incorrecta → `UnauthorizedException` con `INVALID_CREDENTIALS_MESSAGE = "El correo o la contraseña son invalidos"`. Mismo mensaje en ambos casos para no revelar cuál falló. (R2)
- **D4** Respuesta exitosa: `SignInResponse` con `email`, `firstName`, `lastNames` y `role`. El token NO va en el body; se entrega como cookie `Set-Cookie: token=<jwt>; HttpOnly; SameSite=Lax` en la respuesta HTTP. El flag `Secure` se controla con la property `app.security.cookie-secure` (boolean); `true` en producción, configurable en desarrollo. (R1)
- **D5** Token de sesión: JWT firmado con HS256 usando la librería jjwt (`io.jsonwebtoken:jjwt-api/jjwt-impl/jjwt-jackson`). Claims: `userId` (Long), `email` (String), `role` (String). Expiración configurable en `application.properties` (`app.jwt.expiration-ms`). Clave de firma configurable en `app.jwt.secret`. No se almacena en BD. (R1, R4)
- **D6** Agregar al `pom.xml`: `io.jsonwebtoken:jjwt-api`, `jjwt-impl` (runtime), `jjwt-jackson` (runtime). (D5)
- **D7** `JwtService` en `infrastructure/config/security`: método `createToken(User user) → String` que genera el JWT firmado con los claims `userId`, `email`, `role` y la expiración configurada; método `validateToken(String token) → Optional<Claims>` que valida la firma y la expiración del JWT sin consultar BD. (D5, R4)
- **D8** `CookieAuthFilter` (`OncePerRequestFilter`) en `infrastructure/config/security`: lee la cookie `token` del request, llama a `JwtService.validateToken`; si los claims son válidos, extrae `userId`, `email` y `role` del JWT y popula el `SecurityContext`; si la cookie está ausente o el JWT es inválido/expirado, no popula el contexto y deja que Spring Security rechace la petición con 401. Sin consulta a BD. (R4, D7)
- **D9** `SecurityFilterChain` en `infrastructure/config/security/SecurityConfig`: stateless, CSRF off; `/auth/**` con `permitAll`, cualquier otro path requiere autenticación; registra `CookieAuthFilter` antes de `UsernamePasswordAuthenticationFilter`; usa `AuthenticationEntryPoint` que responde con el formato `{message, body}` y `UNAUTHORIZED_MESSAGE = "No autorizado"`. (R4)
- **D10** `AuthenticatedUser` record en `domain/models` con campos `id` (Long) y `role` (Role); es el tipo que devuelve `AuthenticatedUserProvider` y que los services reciben como parámetro. (R4)
- **D11** `AuthenticatedUserProvider` en `infrastructure/config/security`: lee el `SecurityContext` y devuelve `AuthenticatedUser`; lanza `UnauthorizedException` si no hay autenticación activa. (R4)

- **D12** Configurar Springdoc con `@SecurityScheme(name = "cookieAuth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE, paramName = "token")` en una clase `@Configuration`; añadir `@SecurityRequirement(name = "cookieAuth")` a cada endpoint protegido para que Swagger UI envíe la cookie al probar. (D1, D9)
- **D13** En `SecurityConfig`, declarar un array/lista `PUBLIC_PATTERNS` con los patrones `/auth/**`, `/swagger-ui/**`, `/swagger-ui.html` y `/v3/api-docs/**` (incluye `/v3/api-docs.yaml`); el `SecurityFilterChain` lo consume en lugar de URLs dispersas. (D9)
- **D14** El `@SecurityScheme` de D12 se añade como anotación a nivel de clase en `OpenApiConfig.java` (el archivo ya existente en `infrastructure/config`). El archivo `OpenApiSecurityConfig.java` creado en `infrastructure/config/security` debe eliminarse. `OpenApiConfig.java` conserva todos sus beans `GroupedOpenApi` existentes (`authApi`, `employeesApi`, `servicesApi`, `appointmentsApi`, `storeApi`) intactos para que `/swagger-ui.html` siga mostrando todos los grupos. (D12)

## Alternativas descartadas

- **Sesión con estado (HttpSession)**: descartado; stateless es el estándar para APIs REST y Spring Security lo soporta nativamente.
- **Token opaco en BD**: descartado en favor de JWT; el JWT firmado permite validación stateless sin lookup en BD, reduciendo latencia y acoplamiento a persistencia. La revocación inmediata queda fuera del alcance de esta feature.

## Limitaciones conocidas

- No hay endpoint de sign-out ni invalidación de tokens en esta versión; el token expira según `app.jwt.expiration-ms`.
