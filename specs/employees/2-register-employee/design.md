## Decisiones

- **D1** `POST /employees` en `EmployeeController` (nuevo) → `EmployeeService` (nuevo); ruta restringida a `ADMIN`; responde `201` con `RegisterEmployeeResponse`.
- **D2** `RegisterEmployeeRequest`: `firstName`, `lastNames`, `email`, `phone` (todos `@NotBlank`; `email` además `@Email`), `role` (nullable, enum `Role`). Si `role` es null, el service asigna `EMPLOYEE`.
- **D3** Verificar con `findByEmail` antes de guardar; si existe → `ResourceAlreadyExistsException` (nuevo, 409) con constante `EMAIL_ALREADY_EXISTS_MESSAGE` (nueva en `ErrorMessages`).
- **D4** El empleado se crea con `password = null`; la columna `password` en `UserSchema` debe admitir `NULL`. El password lo establece la feature 4.
- **D5** Nuevo modelo `InvitationToken(id, token, userId, expiresAt, used)` en `domain/models`; `token` = UUID aleatorio; `expiresAt = now + 15 min` usando `Clock` inyectado en `EmployeeService`.
- **D6** `InvitationTokenRepository` en `application/repositories` con `save(InvitationToken)` y `findByToken(String)`; `InvitationTokenSchema` + `InvitationTokenJpaRepository` + `InvitationTokenRepositoryImpl` + `InvitationTokenMapper` en `infrastructure`.
- **D7** `InvitationEmail implements EmailDefinition<InvitationEmailData>` en `infrastructure/adapters/mail`; `InvitationEmailData` lleva `firstName` e `invitationLink` (`{app.frontend-url}/set-password?token={uuid}`); propiedad `app.frontend-url` expuesta vía clase de configuración.
- **D8** `RegisterEmployeeResponse`: `id`, `firstName`, `lastNames`, `email`, `role`, `phone`, `createdAt`.
- **D9** `EmployeeService` anotado con `@Transactional`; si el envío de correo falla, la transacción se revierte y el empleado no queda guardado.
- **D10** Acceso restringido a `ADMIN` en `SecurityConfig` con `.requestMatchers("/employees/**").hasRole("ADMIN")`; no autenticados → 401 via `authenticationEntryPoint` existente; autenticados sin rol `ADMIN` → 403 via `accessDeniedHandler` (nuevo en `SecurityConfig`).

## Alternativas descartadas

- Almacenar el token de invitación en la tabla `User`: descartado, mezcla responsabilidades de autenticación con la de invitación.

## Limitaciones conocidas

(Ninguna tras aplicar D9.)
