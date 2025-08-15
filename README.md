# HIS - Microservicio de Seguridad y Autenticación

Microservicio responsable de **registro**, **autenticación**, **emisión/rotación de tokens JWT** y **revocación (logout)** para el ecosistema HIS.

## 🚀 Funcionalidades (MVP)

- Registro de usuarios (`/auth/register`)
- Login con JWT (access + refresh) (`/auth/login`)
- Refresh de access token por refresh token persistido (`/auth/refresh`)
- Logout con revocación de refresh token (opcionalmente en todos los dispositivos) (`/auth/logout`)
- Autorización por **roles + permisos** embebidos en el JWT y verificados por Spring Security + `@PreAuthorize`
- Manejo de errores uniforme mediante `ApiResponse` + `@RestControllerAdvice`

> Context path configurado: **`/api-ms-security`** (p. ej. `http://localhost:8081/api-ms-security/auth/login`)

---

## 🧱 Arquitectura (resumen)

- **Spring Boot 3 / Java 21**
- **Spring Security 6** con filtro `JwtAuthenticationFilter`
- **JWT (HS256)** para access/refresh tokens
- **PostgreSQL** para usuarios, roles, permisos y refresh tokens
- Persistencia vía **Spring Data JPA**
- Validación con **Jakarta Validation**

---

## 🔑 Endpoints principales

> Prefijo común: `/api-ms-security/auth`

### 1) Registro
`POST /register`
```json
{
  "nombreCompleto": "Jane Doe",
  "email": "jane@his.com",
  "password": "Secreta123"
}
```
**200 OK**
```json
{ "status": true, "message": "Usuario registrado", "data": null }
```

### 2) Login
`POST /login`
```json
{
  "email": "jane@his.com",
  "password": "Secreta123"
}
```
**200 OK**
```json
{
  "status": true,
  "message": "Login exitoso",
  "data": {
    "refreshToken": "<refresh.jwt>",
    "accessToken": "<access.jwt>",
    "tokenType": "Bearer",
    "email": "jane@his.com",
    "rol": "RECEPCIONISTA"
  }
}
```

### 3) Refresh
`POST /refresh`
```json
{
  "refreshToken": "<refresh.jwt>"
}
```
**200 OK**
```json
{
  "status": true,
  "message": "Token renovado",
  "data": {
    "accessToken": "<nuevo_access.jwt>",
    "refreshToken": "<nuevo_refresh.jwt>",
    "tokenType": "Bearer"
  }
}
```

### 4) Logout
`POST /logout`
```json
{
  "refreshToken": "<refresh.jwt>",
  "logOutAllDevices": false
}
```
**200 OK**
```json
{ "status": true, "message": "Sesión cerrada", "data": null }
```

> **Autorización**: los endpoints protegidos exigen header `Authorization: Bearer <access.jwt>`.
> El filtro **rechaza** usar refresh tokens para acceder a endpoints protegidos.

---

## ⚙️ Cómo ejecutar

### Requisitos
- Java 21
- Maven 3.9+
- PostgreSQL 14+
- (Opcional) Docker / Docker Compose

### Variables de entorno
Ver **docs/configuration-guide.md** y `.env.example`.

Variables clave:

| Variable | Descripción |
|---|---|
| `DB_URL` | JDBC URL de Postgres |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales BD |
| `JWT_SECRET` | Secreto para firmar JWT (HS256) |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Expiración del access (ms) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Expiración del refresh (ms) |

### Perfiles
- `dev`, `qa`, `prod` con configuración aislada (`application-<profile>.yml`).

### Levantar en local
```bash
# Perfil dev leyendo variables desde tu entorno/IDE
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Salud del servicio
- `GET /actuator/health`


## 🛡️ Seguridad
Resumen en `docs/security-policy.md`.  
Puntos clave: contraseñas con **BCrypt**, JWT **HS256**, tokens **cortos** (access) y **largos** (refresh) con **rotación e invalidación** en BD; autorización por **permisos**.

---

## 🗂️ Estructura del repo sugerida

```
.
├─ docs/
│  ├─ security-architecture.drawio
│  ├─ security-data-model.drawio
│  ├─ configuration-guide.md
│  └─ security-policy.md
├─ src/
│  ├─ main/java/...       # código fuente
│  └─ main/resources/     # yml por perfil
└─ README.md
```