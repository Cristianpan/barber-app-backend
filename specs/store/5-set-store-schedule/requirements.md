# 5 — Establecer horario
Módulo: `store`

## R1
CUANDO el administrador envía el horario de atención indicando los días que labora, el horario de
atención de cada uno de esos días y, si aplica, su hora de descanso, el sistema DEBE guardar ese
horario reemplazando el horario de atención existente.

## R2
El sistema DEBE permitir que el horario de atención sea distinto entre un día y otro, incluyendo que
un día tenga hora de descanso y otro no.

## R3
SI el horario enviado tiene un día de la semana repetido, un horario donde la hora de inicio no es
anterior a la hora de fin, o una hora de descanso incompleta (solo inicio o solo fin) o fuera del
horario de atención del día ENTONCES el sistema DEBE rechazar la solicitud como inválida.
