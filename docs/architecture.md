# Architecture

Paquete base: `com.la_navaja.backend`

Arquitectura hexagonal simplificada. Tres capas: `domain`, `application`, `infrastructure`. Las integraciones externas que no son persistencia (correo, pagos, SMS, ...) siguen el patrón **ports/adapters**: el puerto (interfaz que solo conoce tipos de `domain`) vive en `application/ports` y su adaptador (implementación con la librería externa) en `infrastructure/adapters/<integración>` (ej. `application/ports/InvitationSender` → `infrastructure/adapters/mail/SmtpInvitationSender`). Los repositorios no se tratan como ports/adapters: siguen en `application/repositories` / `infrastructure/repositories`. No hay interfaces in/out genéricas — solo se introducen si son necesarias.

## Regla de dependencia

```
infrastructure -> application -> domain
```

`domain` no depende de ninguna otra capa. `application` no depende de `infrastructure`. `infrastructure` puede depender de ambas.

```
+--------------------------------------------------+
|                  infrastructure                   |
|  schemas | mappers | config | controllers |       |
|  repositories (impl, usa JpaRepository)           |
|  adapters/<integración> (impl. de ports)          |
+--------------------------|-------------------------+
                           v
+--------------------------------------------------+
|                    application                    |
|  services | repositories (interfaces) | validators |
|  ports (interfaces a integraciones externas)      |
|  utils | dtos (request / response)                |
+--------------------------|-------------------------+
                           v
+--------------------------------------------------+
|                       domain                       |
|  models | exceptions | constants (incl. mensajes) |
+--------------------------------------------------+
```

## Capas

- **`domain/`**: `models` (entidades de dominio), `exceptions` (excepciones de dominio), `constants` (constantes y mensajes de excepciones centralizados).
- **`application/`**: `services` (casos de uso), `repositories` (interfaces, solo conocen objetos de dominio), `ports` (interfaces hacia integraciones externas que no son persistencia — correo, pagos, SMS, ...; solo conocen objetos de dominio), `validators`, `utils`, `dtos/request` y `dtos/response`.
- **`infrastructure/`**: `schemas` (entities JPA), `mappers` (schema <-> model), `config` (app config, JWT, security, etc), `controllers`, `repositories` (implementación con `JpaRepository`), `adapters/<integración>` (implementación de un puerto de `application/ports` con la librería externa, ej. `adapters/mail`; traduce los errores de la librería a excepciones de dominio).

Reglas accionables (convenciones de código, qué va dónde, manejo de errores, naming, testing) aplicadas automáticamente al escribir código: ver [`conventions.md`](conventions.md).

## Evolución de la arquitectura

Si en el futuro se necesita agregar una capa nueva o similar (por ejemplo, interfaces in/out genéricas), **primero se debe preguntar al usuario**. Si se aprueba, se implementa y **se actualizan tanto este documento como** [`conventions.md`](conventions.md).
