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
| `KEEPALIVE_ENABLED` | Auto-ping cada 10 min a la propia URL pública (por defecto `true`; solo actúa en Render) |
| `APP_TZ` | Zona en la que la API devuelve las fechas (por defecto `America/Bogota`); en la BD se guardan en UTC |

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/health` | Público | Estado del servicio, sin tocar la BD (para el auto-ping) |
| GET | `/api/health/db` | Público | Verifica la conexión a Neon |
| POST | `/api/auth/login` | Público | `{"username","password"}` → `{token, tipo, expiraEn, usuario{id,nombre,username,rol}}` |
| GET | `/api/auth/me` | Token | Datos del usuario del token |
| GET | `/api/clientes?q=&page=0&size=20` | Token | Lista paginada; `q` busca por nombre o documento |
| GET | `/api/clientes/{id}` | Token | Cliente con su lista `motos` |
| POST | `/api/clientes` | Token | `{"documento","nombre","telefono"?}` → 201 · documento repetido → 409 |
| PUT | `/api/clientes/{id}` | Token | Actualiza datos del cliente |
| DELETE | `/api/clientes/{id}` | ADMINISTRADOR | 204 · si tiene motos → 409 |
| GET | `/api/clientes/{id}/motos` | Token | Motos del cliente (1 a N) |
| POST | `/api/clientes/{id}/motos` | Token | `{"placa","marca","modelo"}` → 201 · placa repetida → 409 |
| GET | `/api/motos?placa=&page=0&size=20` | Token | Lista paginada; `placa` busca parcial |
| GET | `/api/motos/{id}` | Token | Moto con `clienteId` y `clienteNombre` |
| PUT | `/api/motos/{id}` | Token | `{"placa","marca","modelo","clienteId"?}`; `clienteId` traspasa la moto |
| DELETE | `/api/motos/{id}` | ADMINISTRADOR | 204 |

Listas paginadas: `{contenido, pagina, tamano, totalElementos, totalPaginas}`.
Placas: se aceptan `ABC12D`, `ABC123` o `ABC12`, con espacio o guion; se guardan en
mayúsculas sin separadores. Documentos: se quitan puntos y espacios (`1.010.101` → `1010101`).

Las rutas protegidas requieren `Authorization: Bearer <token>`. Sin token o con token
inválido/vencido responden **401**. Los errores tienen el formato
`{timestamp, status, error, mensaje, campos}`.

Roles (columna `usuario.rol`): `ADMINISTRADOR`, `MECANICO`, `VENDEDOR` (también existen
`GERENTE` y `EMPLEADO` en el CHECK de la BD). En los controladores:
`@PreAuthorize("hasRole('ADMINISTRADOR')")`.

## Despliegue en Render (S1-T03)

La imagen se construye con el [`Dockerfile`](Dockerfile) (Java 21, multi-etapa, usuario no root)
y el servicio se define en [`render.yaml`](../render.yaml) en la raíz del repo.

1. En https://dashboard.render.com → **New → Blueprint** y conecta el repositorio.
2. Elige la rama a desplegar (la que tenga `render.yaml` y `soulmotors-api/`).
3. Render pedirá los valores marcados como secretos:
   - `DB_URL`: `jdbc:postgresql://<host-pooler>.neon.tech/neondb?sslmode=require`
   - `DB_USER`, `DB_PASSWORD`: credenciales de Neon
   - `CORS_ORIGINS`: URL del frontend en Vercel, p. ej. `https://soul-motors.vercel.app,http://localhost:5173`
   - `JWT_SECRET` lo genera Render solo.
4. Al terminar, prueba `https://<servicio>.onrender.com/api/health` y `/api/health/db`.
5. En Vercel define `VITE_API_BASE_URL=https://<servicio>.onrender.com/api`.

### Mantenerlo despierto (auto-ping + cron)

Render Free apaga el servicio tras **15 min sin tráfico** y tarda ~1 min en despertar.

- **Auto-ping interno:** mientras está encendida, la API se llama a sí misma cada 10 min a
  `/api/health` usando `RENDER_EXTERNAL_URL` (variable que pone Render). Se desactiva con
  `KEEPALIVE_ENABLED=false`.
- **Cron externo (respaldo):** en https://cron-job.org crea un cron job
  - URL: `https://<servicio>.onrender.com/api/health`
  - Frecuencia: cada 10 minutos · Método GET · timeout 30 s
  - Así se despierta aunque Render la reinicie.

`/api/health` **no consulta la BD**, para que Neon pueda suspenderse y no gaste horas de
cómputo. Un servicio despierto todo el mes usa ~730 de las 750 horas gratis de Render por
workspace: si tienen otro servicio Free en el mismo workspace, desactiven el auto-ping de uno.
