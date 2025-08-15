# Política de Seguridad (MVP)

Esta política resume las decisiones, parámetros y buenas prácticas implementadas en el microservicio.

## 1) Autenticación

- **JWT** firmado con **HS256** (secreto en `JWT_SECRET`).
- **Access token**: vida corta (p.ej. 15 min).
- **Refresh token**: vida más larga (p.ej. 7 días), **persistido** en BD y **rotado** en cada refresh.
- El filtro `JwtAuthenticationFilter` **rechaza** refresh tokens para acceder a endpoints protegidos.
- El `subject` del JWT es el email del usuario. Claims adicionales: `rol`, `permisos`, `typ` (`access` / `refresh`).

## 2) Autorización

- Autorización por **permisos** mediante `@PreAuthorize("hasAuthority('modulo:accion')")`.
- Los permisos provienen de BD y se inyectan como authorities al validar el access token.
- **Rol único** por usuario en este MVP (puede ampliarse en el futuro).

## 3) Gestión de contraseñas

- Hash con **BCrypt** (`PasswordEncoder`).
- **Nunca** guardar contraseñas en texto claro.
- Política sugerida: mínimo 8 caracteres, complejidad media.

## 4) Refresh tokens

- Al hacer refresh:
  1. Se valida firma + tipo `typ=refresh`.
  2. Se consulta el token en BD y se verifica `activo=true` y no expirado.
  3. Se **invalida** el refresh anterior y se emite uno **nuevo** (rotación).
- Logout:
  - Si `logOutAllDevices=true`: se invalidan **todos** los refresh del usuario.
  - Si `false`: se invalida **solo** el token recibido.

## 5) Transporte y cabeceras

- Requiere `Authorization: Bearer <access>` en endpoints protegidos.
- Habilitar **HTTPS** en despliegues productivos (TLS en gateway / LB).
- Configurar **CORS** según el/los frontend(s).
- **CSRF deshabilitado** por ser API stateless.

## 6) Protección adicional recomendada

- Rate limiting (a nivel gateway / API) para `/auth/login` y `/auth/refresh`.
- Auditoría mínima (quién y cuándo realiza login/logout).
- Logs con niveles adecuados, **sin PII sensible**.
- Rotación periódica de `JWT_SECRET` (requiere estrategia de múltiples claves si se planifica).
- Integrar **Actuator** protegido, métricas y health checks.
- Revisiones de dependencia (OWASP Dependency-Check o Renovate).

## 7) Manejo de errores

- Respuestas normalizadas con `ApiResponse { status, message, data }`.
- `@RestControllerAdvice` mapea excepciones comunes a HTTP 400/401/403/409/500.

## 8) Amenazas conocidas y mitigaciones (resumen)

| Amenaza | Mitigación |
|---|---|
| Robo de access token | Vida corta + HTTPS + `HttpOnly` si se usa cookie. |
| Uso de refresh para acceder a APIs | Claim `typ=refresh` + validación en filtro. |
| Reutilización de refresh antiguo | **Rotación** e invalidación en BD. |
| Fuerza bruta de login | Rate limiting + lockout temporal opcional. |
| Secreto filtrado | Gestión segura de secretos (vault/ENV), rotación programada. |

---
