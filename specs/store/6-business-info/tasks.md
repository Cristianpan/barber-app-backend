# Tasks — 6 Establecer información de negocio

- [x] Crear `SetStoreInfoRequest` en `application/dtos/request` (D2)
- [x] Crear `SetStoreInfoResponse` en `application/dtos/response` (D4)
- [x] Crear `StoreInfoService` en `application/services` con método de upsert de la información de negocio (R1, R2, D3)
- [x] Crear `StoreInfoController` con `PUT /store/info` y `@Tag(name = "Store")` (R1, R3, D1)
- [x] Actualizar `SecurityConfig`: restringir `/store/**` a `ADMIN` (D5)
- [x] Documentar `PUT /store/info` en OpenAPI dentro del grupo `store` (200, 400, 401, 403, 500) (D1)
- [x] Escribir `StoreInfoServiceTest` (`SetStoreInfoTests`): registro exitoso cuando no existe información previa, actualización del registro existente en lugar de crear uno nuevo (R1, R2)
- [x] Escribir `StoreInfoControllerTest` (`SetStoreInfoTests`): 200 éxito, 400 request inválido, 401 no autenticado, 403 sin rol admin (R1, R3)
