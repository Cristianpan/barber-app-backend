# Design — 4 Establecer contraseña con token

## Decisiones

- **D1** `POST /auth/set-password` en `AuthController` → `AuthService`; público (`permitAll`), sin body de respuesta (`204 No Content`).
- **D2** `SetPasswordRequest` DTO: `@NotBlank String token`, `@NotBlank @Size(min=8, max=20) String password`.
- **D3** Token no encontrado en `InvitationTokenRepository.findByToken` → `ResourceNotFoundException` con nueva constante `INVITATION_TOKEN_NOT_FOUND_MESSAGE = "El token de invitación no ha sido encontrado"`.
- **D4** Token encontrado pero `used == true` o `expiresAt` anterior al instante actual → `BadRequestException` con nueva constante `INVITATION_TOKEN_INVALID_MESSAGE = "El token de invitación no es válido o ha expirado"`.
- **D5** Usar `Clock` (inyectado como bean) para obtener el instante actual al comparar `expiresAt`; facilita pruebas unitarias.
- **D6** Hashear la contraseña con el puerto `PasswordHasher`; persistir el usuario actualizado con `UserRepository.save()`; marcar el token como usado con `InvitationTokenRepository.save()`.
