# Tasks — 4 Establecer contraseña con token

## Dominio y constantes

- [x] Añadir `INVITATION_TOKEN_NOT_FOUND_MESSAGE` e `INVITATION_TOKEN_INVALID_MESSAGE` en `ErrorMessages` (D3, D4).

## Application

- [x] Crear `SetPasswordRequest` DTO en `application/dtos/request` con `@NotBlank String token` y `@NotBlank @Size(min=8, max=20) String password` (D2, R2).
- [x] Añadir método `setPassword(SetPasswordRequest)` en `AuthService`: buscar token, validar `used` y `expiresAt`, hashear contraseña, guardar usuario y marcar token como usado (R1, R3, D3, D4, D5, D6).

## Infraestructura

- [x] Registrar `Clock` como bean en la configuración de la aplicación si aún no existe (D5).
- [x] Añadir endpoint `POST /auth/set-password` en `AuthController` que invoque `authService.setPassword(request)` y devuelva `204 No Content` (D1).
- [x] Documentar el endpoint en OpenAPI dentro del grupo `auth`: respuestas `204`, `400` (forma inválida y token inválido/expirado), `404` (token no encontrado) y `500` (D1, D3, D4).

## Tests

- [x] `AuthServiceTest` — clase anidada `SetPasswordTests`:
  - `shouldSetPasswordWithValidToken` — happy path: verifica hash, `UserRepository.save()` con nueva contraseña y `InvitationTokenRepository.save()` con `used=true` (R1).
  - `shouldRejectWhenTokenNotFound` — token inexistente → `ResourceNotFoundException` con `INVITATION_TOKEN_NOT_FOUND_MESSAGE` (R3, D3).
  - `shouldRejectWhenTokenIsUsedOrExpired` — token usado y token expirado → `BadRequestException` con `INVITATION_TOKEN_INVALID_MESSAGE` (R3, D4).
- [x] `AuthControllerTest` — clase anidada `SetPasswordTests`:
  - `shouldSetPasswordSuccessfully` — `204` sin body (R1, D1).
  - `shouldReturnNotFoundWhenTokenNotFound` — mapeo de `ResourceNotFoundException` → `404` (R3, D3).
  - `shouldReturnBadRequestWhenTokenInvalid` — mapeo de `BadRequestException` → `400` (R3, D4).
  - `shouldRejectInvalidRequest` — request sin campos → `400` genérico (R2, D2).
