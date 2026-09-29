# 3 — Crear servicio
Módulo: `services`

## R1
CUANDO un administrador registra un nuevo servicio proporcionando nombre, descripción, duración en minutos y costo, el sistema DEBE crear el servicio y responder con sus datos.

## R2
CUANDO se registra un nuevo servicio, el sistema DEBE asignarle el estatus de habilitado por defecto.

## R3
SI el nombre del servicio ya está registrado ENTONCES el sistema DEBE rechazar el registro indicando que el nombre ya existe.

## R4
SI el costo o la duración proporcionados son menores o iguales a cero ENTONCES el sistema DEBE rechazar el registro.

## R5
SI un usuario sin sesión activa o sin rol de administrador intenta registrar un servicio ENTONCES el sistema DEBE rechazar la petición.
