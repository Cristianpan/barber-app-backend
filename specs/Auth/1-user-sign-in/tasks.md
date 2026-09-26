# Tasks — 1 · Autenticar usuario por correo y contraseña

## Configuración

- [x] Agregar `io.jsonwebtoken:jjwt-api`, `jjwt-impl` (scope runtime) y `jjwt-jackson` (scope runtime) al `pom.xml`. (D6)
- [x] Agregar `app.jwt.expiration-ms`, `app.jwt.secret` y `app.security.cookie-secure` a `application.properties`. (D5, D4)

## Domain

- [x] Crear `AuthenticatedUser` record en `domain/models` con campos `id` (Long) y `role` (Role). (D10)
- [x] Añadir `INVALID_CREDENTIALS_MESSAGE = "El correo o la contraseña son invalidos"` y `UNAUTHORIZED_MESSAGE = "No autorizado"` en `domain/constants/ErrorMessages`. (D3, D9)

## Application

- [x] Añadir `findByEmail(String email)` a `UserRepository` en `application/repositories`. (D2)
- [x] Crear `SignInRequest` record en `application/dtos/request` con campos `email` (`@NotBlank`, `@Email`) y `password` (`@NotBlank`). (R1)
- [x] Crear `SignInResponse` record en `application/dtos/response` con campos `email`, `firstName`, `lastNames` y `role` (sin campo `token`). (D4)
- [x] Crear `AuthService` en `application/services`: método `signIn(SignInRequest) → User` que busca el usuario por email (minúsculas), verifica la contraseña con `PasswordEncoder` y retorna `User`. Lanza `UnauthorizedException` si las credenciales son inválidas. (R1, R2, D2, D3)

## Infrastructure — repositorio

- [x] Añadir `findByEmail(String email)` a `UserJpaRepository` en `infrastructure/repositories`. (D2)
- [x] Implementar `findByEmail` en `UserRepositoryImpl`: normaliza el correo a minúsculas antes de consultar, mapea el resultado a `User` model. (D2)

## Infrastructure — seguridad

- [x] Crear `JwtService` en `infrastructure/config/security`: método `createToken(User user) → String` (genera JWT firmado con claims `userId`, `email`, `role` y expiración) y método `validateToken(String token) → Optional<Claims>` (valida firma y expiración, sin BD). (D5, D7)
- [x] Crear `CookieAuthFilter` (`OncePerRequestFilter`) en `infrastructure/config/security`: lee la cookie `token`, llama a `JwtService.validateToken`; si claims válidos, extrae `userId`, `email` y `role` del JWT y popula el `SecurityContext`; si no, deja pasar sin contexto. Sin consulta a BD. (D8)
- [x] Crear `SecurityConfig` en `infrastructure/config/security` con `SecurityFilterChain`: stateless, CSRF off, `/auth/**` público, resto autenticado; registrar `CookieAuthFilter`; configurar `AuthenticationEntryPoint` que responde `{message: UNAUTHORIZED_MESSAGE, body: null}` con status 401. (D9)
- [x] Crear `AuthenticatedUserProvider` en `infrastructure/config/security`: método `getAuthenticatedUser()` que lee el `SecurityContext` y devuelve `AuthenticatedUser`; lanza `UnauthorizedException` si no hay autenticación. (D10, D11)

## Infrastructure — controller

- [x] Crear `AuthController` en `infrastructure/controllers` con `POST /auth/sign-in`: delega en `AuthService.signIn` para validar credenciales, llama a `JwtService.createToken` para generar el JWT, construye la cookie `HttpOnly; SameSite=Lax` con el flag `Secure` leído de `app.security.cookie-secure`, la añade a `HttpServletResponse` y retorna `SignInResponse` con status 200. Añadir `@Tag(name = "Auth")`. (R1, D1, D4, D7)
- [x] Documentar `POST /auth/sign-in` en OpenAPI dentro del grupo `authApi`: `@Operation`, `@ApiResponse` 200 con schema `SignInResponse` (sin campo token) y header `Set-Cookie`, 401 con mensaje de credenciales inválidas, 400 con mensaje genérico, 500 con mensaje genérico. (D1)

## Infrastructure — OpenAPI security

- [x] Eliminar `OpenApiSecurityConfig.java` de `infrastructure/config/security` y añadir `@SecurityScheme(name = "cookieAuth", type = APIKEY, in = COOKIE, paramName = "token")` como anotación a nivel de clase en `OpenApiConfig.java` (`infrastructure/config`), conservando todos los beans `GroupedOpenApi` existentes intactos. Verificar que `/swagger-ui.html` sigue mostrando todos los grupos. (D12, D14)
- [x] Añadir `@SecurityRequirement(name = "cookieAuth")` a cada endpoint protegido del proyecto (excluir `POST /auth/sign-in` que es público). (D12)
- [x] Refactorizar `SecurityConfig`: extraer el array/lista `PUBLIC_PATTERNS = {"/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"}` y usarlo en el `SecurityFilterChain` en lugar de URLs hardcodeadas dispersas. (D13)

## Tests

- [x] `AuthServiceTest` — `@Nested class SignInTests`: `shouldReturnUserData` (happy path, verifica campos de `User` retornado), `shouldRejectInvalidCredentials_whenEmailNotFound` y `shouldRejectInvalidCredentials_whenPasswordMismatch` (ambos 401, mismo mensaje). (R1, R2)
- [x] `AuthControllerTest` — `@Nested class SignInTests`: `shouldSignInSuccessfully` (200, verifica body `SignInResponse` y presencia de cookie `token` en la respuesta), `shouldRejectInvalidCredentials` (401, verifica `{message}`), `shouldRejectInvalidRequest` (400, request sin campos). (R1, R2, R3, D4)
- [x] `AuthControllerTest` — verificar que un request a un endpoint protegido sin cookie `token` retorna 401 con `{message: UNAUTHORIZED_MESSAGE}`. (R4)
