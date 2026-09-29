## Decisiones

- **D1** `POST /services` en `ServiceController` (nuevo) → `ServiceService` (nuevo); ruta restringida a `ADMIN`; responde `201` con `CreateServiceResponse`.
- **D2** `CreateServiceRequest`: `name` (`@NotBlank`), `description` (`@NotBlank`), `durationMinutes` (`int`, `@Positive`), `price` (`BigDecimal`, `@Positive`). `@Positive` cubre R4 vía el advice genérico de forma.
- **D3** `normalizedName` = `name.trim().toLowerCase()`, igual que ya hace `OfferingSchema`; se calcula en `ServiceService` antes de guardar.
- **D4** Verificar duplicado con `findByNormalizedName` antes de guardar; si existe → `ResourceAlreadyExistsException` con constante nueva `SERVICE_NAME_ALREADY_EXISTS_MESSAGE` en `ErrorMessages`.
- **D5** Agregar `findByNormalizedName(String)` a `OfferingRepository`, `OfferingJpaRepository` (query derivada) y `OfferingRepositoryImpl`.
- **D6** El servicio se crea con `enabled = true` explícito en `ServiceService` (el modelo de dominio `Offering`, al ser un `record`, no aplica el default de la entity JPA).
- **D7** `CreateServiceResponse`: `id`, `name`, `description`, `durationMinutes`, `price`, `enabled`, `createdAt`.
- **D8** Acceso restringido a `ADMIN` en `SecurityConfig` con `.requestMatchers("/services/**").hasRole("ADMIN")`; no autenticados → 401 vía `authenticationEntryPoint`; autenticados sin rol `ADMIN` → 403 vía `accessDeniedHandler` (ambos ya existentes).

## Alternativas descartadas

- Validador de negocio para costo/duración > 0: descartado, `@Positive` en el DTO ya cumple R4 sin lógica extra.

## Limitaciones conocidas

(Ninguna.)
