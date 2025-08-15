# Guía de Configuración

Esta guía explica cómo parametrizar y ejecutar el microservicio en **dev**, **qa** y **prod** con variables de entorno y perfiles de Spring.

## 1) Variables de entorno

Crea un archivo `.env` (no lo subas a git) o configura variables en tu sistema/CI:

```
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/his_security
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=super-secreto-de-32+caracteres
JWT_ACCESS_TOKEN_EXPIRATION=900000       # 15 min
JWT_REFRESH_TOKEN_EXPIRATION=604800000   # 7 días
```

> En IntelliJ puedes usar el plugin **EnvFile** para cargar `.env` al ejecutar.

Incluye un `.env.example` en el repo con keys vacías para referencia del equipo.

## 2) application.yml por perfil

Ejemplo (fragmento) para `application-dev.yml` usando variables del entorno:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate   # 'update' solo en desarrollo inicial
    properties:
      hibernate:
        format_sql: true
        jdbc:
          lob:
            non_contextual_creation: true

server:
  port: 8081
  servlet:
    context-path: /api-ms-security

security:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION}
    refresh-token-expiration: ${JWT_REFRESH_TOKEN_EXPIRATION}
```

Crea archivos equivalentes para `application-qa.yml` y `application-prod.yml` (con endpoints/puertos/BD propios).

## 3) Perfiles activos

En `application.yml`:
```yaml
spring:
  profiles:
    active: dev
```
O sobreescribe al ejecutar:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=qa
```

## 4) Base de datos

Usa migraciones con **Flyway/Liquibase** (recomendado) o scripts SQL manuales.  
Tablas mínimas: `usuario`, `rol`, `permiso`, `rol_permiso`, `refresh_token`.

## 5) Docker (opcional)

`docker-compose.yml` de ejemplo (local):

```yaml
version: "3.8"
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: his_security
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - dbdata:/var/lib/postgresql/data
volumes:
  dbdata:
```

Luego, configura tu `application-dev.yml` para apuntar a `jdbc:postgresql://localhost:5432/his_security`.