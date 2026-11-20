# soulmotors-api

Backend de Soul Motors: Spring Boot 4 · Java 21 · PostgreSQL (Neon).

## Ejecutar en local

1. Copia `.env.example` a `.env` y llena tus datos (el `.env` no se sube a git).
2. `./mvnw spring-boot:run` (en Windows: `mvnw.cmd spring-boot:run`)
3. La API queda en `http://localhost:8080/api`

Pruebas: `./mvnw test` (no necesitan conexión a Neon).

## Variables de entorno

| Variable | Descripción |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host>/neondb?sslmode=require` |
| `DB_USER` / `DB_PASSWORD` | Credenciales de Neon |
| `JWT_SECRET` | Clave para firmar tokens, mínimo 32 caracteres |
| `JWT_EXPIRATION_MINUTES` | Duración del token (por defecto 480 = 8 h) |
| `CORS_ORIGINS` | Orígenes permitidos separados por comas (por defecto `http://localhost:5173`) |
| `PORT` | Puerto (Render lo asigna; por defecto 8080) |

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/health` | Público | Estado del servicio, sin tocar la BD (para el auto-ping) |
| GET | `/api/health/db` | Público | Verifica la conexión a Neon |
| POST | `/api/auth/login` | Público | `{"username","password"}` → `{token, tipo, expiraEn, usuario{id,nombre,username,rol}}` |
| GET | `/api/auth/me` | Token | Datos del usuario del token |

Las rutas protegidas requieren `Authorization: Bearer <token>`. Sin token o con token
inválido/vencido responden **401**. Los errores tienen el formato
`{timestamp, status, error, mensaje, campos}`.

Roles (columna `usuario.rol`): `ADMINISTRADOR`, `MECANICO`, `VENDEDOR` (también existen
`GERENTE` y `EMPLEADO` en el CHECK de la BD). En los controladores:
`@PreAuthorize("hasRole('ADMINISTRADOR')")`.
