# 4 — Establecer contraseña con token
Módulo: `auth`

## R1
CUANDO el empleado envía un token de invitación válido junto con una contraseña nueva,
el sistema DEBE establecer esa contraseña para su cuenta e invalidar el token para que
no pueda volver a utilizarse.

## R2
La contraseña DEBE tener un mínimo de 8 caracteres y un máximo de 20.

## R3
SI el token no existe, ya fue utilizado o ha expirado, ENTONCES el sistema DEBE
rechazar la solicitud e informar que el token no es válido.
