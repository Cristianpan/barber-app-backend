## Decisiones

- **D1** `PUT /store/info` en `StoreInfoController` (nuevo) → `StoreInfoService` (nuevo); ruta restringida a `ADMIN`; responde `200` con `SetStoreInfoResponse`.
- **D2** `SetStoreInfoRequest`: `name` (`@NotBlank`, requerido por la columna no nula de `StoreInfoSchema`), `address`, `phone`, `email` (`@Email`), `history`, `aboutUs` (opcionales).
- **D3** `StoreInfoService` obtiene el registro existente vía `storeInfoRepository.findAll().stream().findFirst()` (fila única, ver comentario en `StoreInfo`); si existe, conserva su `id` y actualiza los campos; si no, crea uno nuevo. Cubre R2.
- **D4** `SetStoreInfoResponse`: `id`, `name`, `address`, `phone`, `email`, `history`, `aboutUs`, `updatedAt`.
- **D5** `SecurityConfig`: agregar `.requestMatchers("/store/**").hasRole("ADMIN")`; no autenticados → `401` vía `authenticationEntryPoint`, autenticados sin rol `ADMIN` → `403` vía `accessDeniedHandler` (ambos ya existentes). Cubre R3.

## Alternativas descartadas

- Endpoints separados de creación y actualización: descartados; al ser un registro único (fila única de `store_info`), un único `PUT` idempotente cubre R1 y R2 sin duplicar lógica.
- Endpoint de consulta (`GET /store/info`): descartado, ningún `R<n>` de esta feature lo pide.

## Limitaciones conocidas

(Ninguna.)
