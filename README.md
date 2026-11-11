# Taller de Contenedores y Patrones — Gestión de artículos

Aplicación Java de consola que inserta y consulta artículos en PostgreSQL o MySQL mediante JDBC. La fábrica selecciona el adaptador y el DAO centraliza el acceso a datos. Docker Compose ejecuta MySQL y CloudBeaver. PostgreSQL se conecta por la red Docker compartida `red-fis`.

## Requisitos

- Docker Desktop y Docker Compose.
- Una red Docker llamada `red-fis`.
- Un contenedor PostgreSQL accesible como `pg-fis` en esa red, con base `fis_db` y credenciales válidas.
- Acceso a Internet para descargar las imágenes y dependencias durante la primera compilación.

Si la red aún no existe: `docker network create red-fis`. El Compose de este repositorio no administra ni elimina el contenedor PostgreSQL existente.

## Configuración

Copia `.env.example` a `.env` y configura credenciales locales. El archivo `.env` está excluido de Git; no guardes contraseñas reales en el repositorio.

```powershell
Copy-Item .env.example .env
```

Por defecto la aplicación selecciona PostgreSQL. Para probar MySQL, agrega esta variable a `.env`:

```dotenv
DEFAULT_DB_CLASS=implementacion.AdaptadorMysql
```

La aplicación usa `PG_USER` y `PG_PASSWORD` para PostgreSQL. En MySQL usa el usuario `app_fis` y `MYSQL_PASSWORD`. Los archivos `.properties` contienen valores de conexión no secretos como respaldo.

## Arranque

Desde la raíz del repositorio:

```powershell
docker compose up -d mysql-fis cloudbeaver-fis
docker compose --profile app build app
docker compose --profile app run --rm app
```

La aplicación se ejecuta una vez, inserta dos artículos de ejemplo, muestra el listado y termina. Los datos permanecen en la base aunque el contenedor de aplicación se elimine.

MySQL publica el puerto `3307` del equipo y usa internamente `3306`. CloudBeaver se abre en <http://localhost:8978>. Si un puerto está ocupado, cambia solo el puerto publicado en `docker-compose.yml`.

## CloudBeaver

En el primer acceso, completa el asistente para crear el administrador de CloudBeaver. Crea las conexiones dentro de la red usando los puertos internos:

| Campo | PostgreSQL | MySQL |
| --- | --- | --- |
| Host | `pg-fis` | `mysql-fis` |
| Puerto | `5432` | `3306` |
| Base de datos | `fis_db` | `fis_db` |
| Usuario | `PG_USER` de `.env` | `app_fis` |
| Contraseña | `PG_PASSWORD` de `.env` | `MYSQL_PASSWORD` de `.env` |

Si MySQL muestra `Public Key Retrieval is not allowed`, agrega `allowPublicKeyRetrieval=true` en las propiedades del driver.

Consulta los artículos guardados por el programa:

```sql
SELECT idarticulo, nombrearticulo, valor
FROM articulos
ORDER BY idarticulo;
```

PostgreSQL y MySQL conservan bases de datos independientes. Antes de ejecutar la aplicación, la base seleccionada debe contener la tabla `articulos`; los scripts están en `Docker-factory-method/init/`. La inicialización automática del script MySQL ocurre únicamente cuando su volumen está vacío.

## Estructura

- `Docker-factory-method/dao`: operaciones de acceso a datos.
- `Docker-factory-method/entidades`: modelo `articulos`.
- `Docker-factory-method/patronfactorymethod`: interfaz, fábrica y punto de entrada.
- `implementacion`: adaptadores JDBC PostgreSQL y MySQL.
- `utilidades`: carga de propiedades.
- `Docker-factory-method/init`: scripts SQL de creación de la tabla.
- `pom.xml`, `Dockerfile` y `docker-compose.yml`: dependencias, compilación e infraestructura.

## Persistencia y operación

```powershell
docker compose ps
docker logs mysql-fis
docker logs cloudbeaver-fis
docker compose down
```

`docker compose down` conserva los volúmenes. No uses `docker compose down -v` salvo que quieras borrar los datos de MySQL y la configuración de CloudBeaver. PostgreSQL es externo a este Compose y debe iniciarse por separado.
