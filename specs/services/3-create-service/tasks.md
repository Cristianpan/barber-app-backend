# Tasks — 3 Crear servicio

- [x] Agregar constante `SERVICE_NAME_ALREADY_EXISTS_MESSAGE` en `ErrorMessages` (D4)
- [x] Agregar `findByNormalizedName(String)` a `OfferingRepository` (D5)
- [x] Agregar query derivada `findByNormalizedName` en `OfferingJpaRepository` (D5)
- [x] Implementar `findByNormalizedName` en `OfferingRepositoryImpl` (D5)
- [x] Crear `CreateServiceRequest` en `application/dtos/request` (D2)
- [x] Crear `CreateServiceResponse` en `application/dtos/response` (D7)
- [x] Crear `ServiceService` en `application/services` con método `createService` (R1, R2, R3, R4, D2–D6)
- [x] Crear `ServiceController` con `POST /services` y `@Tag(name = "Services")` (R1, R5, D1)
- [x] Actualizar `SecurityConfig`: restringir `/services/**` a `ADMIN` (D8)
- [x] Documentar `POST /services` en OpenAPI dentro del grupo `services` (201, 400, 401, 403, 409, 500) (D1)
- [x] Escribir `ServiceServiceTest` (`CreateServiceTests`): registro exitoso, estatus habilitado por defecto, nombre duplicado (R1, R2, R3)
- [x] Escribir `ServiceControllerTest` (`CreateServiceTests`): 201 éxito, 400 request inválido (incluye costo/duración ≤ 0), 401 no autenticado, 403 sin rol admin, 409 nombre duplicado (R1, R4, R5)
