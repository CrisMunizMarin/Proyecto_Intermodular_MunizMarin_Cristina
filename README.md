## MentorCore

Aplicación web Spring Boot para la gestión de la Formación en Empresa (FE).

### Requisitos de despliegue

- Java 21
- MySQL 8
- Variables de entorno para el perfil `prod`
- Almacenamiento persistente para los archivos subidos

### Despliegue con Docker

El proyecto incluye un `Dockerfile` multi-stage que:

1. compila la aplicación con Maven
2. genera el JAR
3. arranca la app con perfil `prod`

Construcción local:

```bash
docker build -t mentorcore:latest .
```

Ejecución local con variables:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MENTORCORE_DB_URL='jdbc:mysql://HOST:3306/BD?useSSL=false&serverTimezone=Europe/Madrid&allowPublicKeyRetrieval=true&characterEncoding=UTF-8' \
  -e MENTORCORE_DB_USERNAME=usuario \
  -e MENTORCORE_DB_PASSWORD=password \
  -e MENTORCORE_UPLOADS_PATH=/data/uploads \
  -e MENTORCORE_APP_BASE_URL=https://tu-dominio \
  -e MENTORCORE_MAIL_HOST=smtp.gmail.com \
  -e MENTORCORE_MAIL_PORT=587 \
  -e MENTORCORE_MAIL_USERNAME=tu-email \
  -e MENTORCORE_MAIL_PASSWORD=tu-password \
  -e MENTORCORE_SQL_INIT_MODE=never \
  mentorcore:latest
```

### Despliegue en Railway

#### 1. Subir el proyecto a GitHub

Railway puede desplegar directamente desde el repositorio.

#### 2. Crear un proyecto en Railway

- Añadir un servicio para la aplicación
- Añadir un servicio MySQL en el mismo proyecto

Documentación oficial:

- [Deploy a Spring Boot App](https://docs.railway.com/guides/spring-boot)
- [Railway MySQL](https://docs.railway.com/databases/mysql)
- [Variables](https://docs.railway.com/variables)

#### 3. Variables necesarias en Railway

Configura estas variables en el servicio de la app:

- `SPRING_PROFILES_ACTIVE=prod`
- `MENTORCORE_DB_URL`
- `MENTORCORE_DB_USERNAME`
- `MENTORCORE_DB_PASSWORD`
- `MENTORCORE_UPLOADS_PATH=/data/uploads`
- `MENTORCORE_APP_BASE_URL=https://TU-URL-PUBLICA`
- `MENTORCORE_MAIL_HOST`
- `MENTORCORE_MAIL_PORT`
- `MENTORCORE_MAIL_USERNAME`
- `MENTORCORE_MAIL_PASSWORD`

Variables recomendadas opcionales:

- `JAVA_OPTS=-Xms256m -Xmx512m`

#### 4. Primer arranque de una base vacía

En el **primer despliegue** contra una base nueva:

- `MENTORCORE_SQL_INIT_MODE=always`

Eso ejecutará:

- `schema.sql`
- `data.sql`

Una vez creada la estructura y sembrados los datos iniciales, cambia la variable a:

- `MENTORCORE_SQL_INIT_MODE=never`

#### 5. Volumen persistente para uploads

Los documentos y convenios se guardan en disco. En Railway debes montar almacenamiento persistente y apuntar:

- `MENTORCORE_UPLOADS_PATH=/data/uploads`

Sin ese volumen, los archivos podrían perderse tras reinicios o redeploys.

### Variables y perfiles

El proyecto está preparado para:

- `dev` en local
- `prod` en despliegue

El perfil activo se controla por:

```properties
SPRING_PROFILES_ACTIVE
```

### Seguridad

- No subir credenciales reales al repositorio
- Usar variables de entorno para base de datos y correo
- Mantener `MENTORCORE_SQL_INIT_MODE=never` después del primer arranque
