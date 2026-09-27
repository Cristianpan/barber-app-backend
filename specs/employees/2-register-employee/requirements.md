# 2 — Registrar nuevo empleado
Módulo: `employees`

## R1
CUANDO un administrador registra un nuevo empleado proporcionando nombre, apellidos, correo y teléfono, el sistema DEBE crear al empleado y responder con sus datos.

## R2
CUANDO el administrador registra un empleado sin indicar un rol, el sistema DEBE asignarle el rol de empleado por defecto.

## R3
CUANDO el administrador registra un nuevo empleado, el sistema DEBE enviarle un correo con un enlace para que establezca su contraseña.

## R4
El enlace para establecer la contraseña DEBE expirar en un máximo de 15 minutos.

## R5
La contraseña que el empleado establezca DEBE tener entre 8 y 20 caracteres.

## R6
SI el correo proporcionado ya está registrado en el sistema ENTONCES el sistema DEBE rechazar el registro indicando que el correo ya existe.

## R7
SI un usuario sin sesión activa o sin rol de administrador intenta registrar un empleado ENTONCES el sistema DEBE rechazar la petición.
