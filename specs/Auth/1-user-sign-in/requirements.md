# 1 — Autenticar usuario por correo y contraseña
Módulo: `Auth`

## R1
CUANDO un usuario envía su correo electrónico y contraseña, el sistema DEBE
autenticarlo, devolver su correo, nombre, apellidos y rol en el cuerpo de la
respuesta, y entregar un token de sesión como cookie segura en la respuesta HTTP.

## R2
SI el correo no existe o la contraseña es incorrecta, ENTONCES el sistema DEBE
rechazar la solicitud con el mensaje "El correo o la contraseña son invalidos",
sin revelar cuál de los dos datos falló.

## R3
SI ocurre un error del servidor durante la autenticación, el sistema DEBE
devolver un estado que refleje el problema sin exponer detalles internos.

## R4
MIENTRAS un request accede a un recurso protegido sin un token de sesión válido,
el sistema DEBE rechazar el acceso como no autorizado.
