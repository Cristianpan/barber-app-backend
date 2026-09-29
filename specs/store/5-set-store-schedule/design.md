# Design — 5 Establecer horario

## Decisiones

- **D1** `PUT /store/schedule` en `StoreController` (nuevo) → `StoreService` (nuevo); rol ADMIN, como `/employees` y `/services`.
- **D2** Reemplazo total en una transacción: `BusinessHourRepository.deleteAll()` y luego `save` de las filas nuevas (patrón ya usado por `StoreInfoRepository`/`BusinessHourRepository`).
- **D3** Un día del request sin descanso se guarda como una fila `BusinessHour` (`startTime`-`endTime`); con descanso, como dos filas (`startTime`-`breakStart`, `breakEnd`-`endTime`). Un día de la semana ausente en el request queda sin filas (cerrado ese día).
- **D4** `SetStoreScheduleRequest(List<DayScheduleRequest> days)`, con `DayScheduleRequest(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, LocalTime breakStart, LocalTime breakEnd)`; `breakStart`/`breakEnd` nulos si el día no tiene descanso.
- **D5** Validación de negocio (día repetido, `startTime >= endTime`, descanso incompleto o fuera del rango del día) en `StoreScheduleValidator` (`application/validators`) → `BadRequestException` con `INVALID_STORE_SCHEDULE_MESSAGE`.
- **D6** `SetStoreScheduleResponse(List<DayScheduleResponse> days)` con la misma forma que el request, reconstruida agrupando por `dayOfWeek` las filas `BusinessHour` recién guardadas.
- **D7** `BusinessHourRepository` gana el método `deleteAll()`; el resto de sus firmas no cambia.
- **D8** `SecurityConfig` agrega `/store/**` a `hasRole("ADMIN")`.

## Alternativas descartadas

- Un endpoint por día (`PUT /store/schedule/{dayOfWeek}`): descartado porque el `acceptance` pide registrar el horario completo en un solo paso y R2 exige poder variarlo día a día sin llamadas parciales.
