# Tasks — 5 Establecer horario

- [x] Agregar `deleteAll()` a `BusinessHourRepository` (application) e implementarlo en `BusinessHourRepositoryImpl` (D7).
- [x] Crear `DayScheduleRequest` y `SetStoreScheduleRequest` en `application/dtos/request` (D4).
- [x] Crear `DayScheduleResponse` y `SetStoreScheduleResponse` en `application/dtos/response` (D6).
- [x] Agregar `INVALID_STORE_SCHEDULE_MESSAGE` a `ErrorMessages` (D5).
- [x] Crear `StoreScheduleValidator` en `application/validators`: valida día repetido, `startTime < endTime` y descanso completo y dentro del rango del día; lanza `BadRequestException` (D5, R3).
- [x] Crear `StoreService` con `setSchedule(SetStoreScheduleRequest)`: valida (D5), reemplaza el horario (D2) mapeando cada día a una o dos filas `BusinessHour` (D3) y arma el `SetStoreScheduleResponse` (D6).
- [x] Escribir `StoreServiceTest`: happy path con y sin descanso, con horarios distintos por día (R1, R2); rechazo por día repetido, rango inválido y descanso incompleto/fuera de rango (R3).
- [x] Crear `StoreController` con `PUT /store/schedule` → `StoreService.setSchedule` (D1).
- [x] Escribir `StoreControllerTest`: 200 con el horario guardado (R1, R2), 400 por solicitud inválida (R3), 401/403 por rol.
- [x] Agregar `/store/**` a `hasRole("ADMIN")` en `SecurityConfig` (D8).
- [x] Documentar `PUT /store/schedule` en OpenAPI dentro del grupo `store` (`@Tag`, `@Operation`, `@ApiResponses` 200/400/401/403/500).
