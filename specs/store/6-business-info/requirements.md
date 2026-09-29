# 6 — Establecer información de negocio
Módulo: `store`

## R1
CUANDO el administrador registra la información de su negocio proporcionando nombre, ubicación, historia, correo, teléfono y sobre nosotros, el sistema DEBE guardar la información y responder con los datos guardados.

## R2
MIENTRAS ya exista información de negocio registrada, CUANDO el administrador vuelve a registrarla, el sistema DEBE actualizar la información existente en lugar de crear un registro nuevo.

## R3
SI un usuario sin sesión activa o sin rol de administrador intenta registrar la información de negocio ENTONCES el sistema DEBE rechazar la petición.
