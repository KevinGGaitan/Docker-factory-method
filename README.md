# Taller de Contenedores — Administración de bases de datos

## Integrantes

- Kevin Garay
- Violeta Fajardo
- Julian Perez
- Jose Guerrero

## Descripción

Entorno de administración de bases de datos PostgreSQL y MySQL mediante CloudBeaver. Los tres servicios se ejecutan en contenedores Docker conectados a una red privada. Desde el navegador es posible explorar tablas, ejecutar consultas SQL e insertar, consultar y modificar registros. Los volúmenes conservan los datos y la configuración al recrear los contenedores.

Proyecto académico de Arquitectura de Software, Ingeniería de Sistemas.

## Alcance

El proyecto integra motores de bases de datos y un cliente web existente. El alcance del taller es configurar, comunicar y verificar estos servicios; no requiere desarrollar un frontend ni una API propios. La tabla `curso` sirve como ejemplo para comprobar las operaciones de almacenamiento y consulta.

Este README describe la configuración prevista por la guía. No certifica que los servicios ya estén desplegados o que exista código adicional en el repositorio.

## Tecnologías

| Tecnología | Versión de referencia | Uso |
| --- | --- | --- |
| Docker Engine / Docker Desktop | Compatible con el equipo | Ejecución y aislamiento de servicios |
| PostgreSQL | 18 | Motor de base de datos relacional |
| MySQL | 8.4 | Motor de base de datos relacional |
| CloudBeaver Community | `latest` en la guía | Cliente web y editor SQL |
| SQL | Dialecto de cada motor | Definición y consulta de datos |
| Volúmenes Docker | Administrados por Docker | Persistencia de datos y configuración |
| Red Docker | `red-fis` | Comunicación interna por nombre |

Para mejorar la reproducibilidad, una evolución del proyecto debe fijar una versión o digest de CloudBeaver una vez comprobada su compatibilidad.

## Arquitectura y funcionamiento

| Contenedor | Imagen | Dirección interna | Puerto del equipo | Volumen y montaje |
| --- | --- | --- | --- | --- |
| `pg-fis` | `postgres:18` | `pg-fis:5432` | `5432` | `pg_fis_data:/var/lib/postgresql` |
| `mysql-fis` | `mysql:8.4` | `mysql-fis:3306` | `3306` | `mysql_fis_data:/var/lib/mysql` |
| `cloudbeaver-fis` | `dbeaver/cloudbeaver:latest` | `cloudbeaver-fis:8978` | `8978` | `cb_fis_workspace:/opt/cloudbeaver/workspace` |

El usuario accede a CloudBeaver en `http://localhost:8978`. CloudBeaver se conecta a cada motor mediante su nombre dentro de `red-fis`. Dentro de un contenedor, `localhost` identifica al propio contenedor; por ello no sirve para alcanzar al otro motor.

Los puertos publicados permiten acceder desde el equipo. Si se cambia el puerto externo de PostgreSQL a `5433`, CloudBeaver continúa usando `pg-fis:5432`.

## Requisitos

- Docker instalado y con el motor en ejecución.
- Acceso a internet para descargar las imágenes.
- Navegador web.
- Puertos `5432`, `3306` y `8978` disponibles, o puertos externos alternativos.
- En Windows con Docker Desktop: backend WSL 2 configurado.

## Ejecución local

Los siguientes comandos son de una sola línea y pueden ejecutarse en Bash, PowerShell o CMD. Las credenciales incluidas son ejemplos exclusivos del laboratorio.

### 1. Crear la red

```sh
docker network create red-fis
```

Si la red ya existe, reutilizarla.

### 2. Iniciar PostgreSQL

```sh
docker run -d --name pg-fis --network red-fis -e POSTGRES_USER=admin_fis -e POSTGRES_PASSWORD=Fis2026*Pg -e POSTGRES_DB=fis_db -p 127.0.0.1:5432:5432 -v pg_fis_data:/var/lib/postgresql postgres:18
```

En PostgreSQL 18 el volumen se monta en `/var/lib/postgresql`. No usar la ruta de montaje de versiones anteriores.

### 3. Iniciar MySQL

```sh
docker run -d --name mysql-fis --network red-fis -e MYSQL_ROOT_PASSWORD=Fis2026*My -e MYSQL_DATABASE=fis_db -e MYSQL_USER=app_fis -e MYSQL_PASSWORD=App2026*My -p 127.0.0.1:3306:3306 -v mysql_fis_data:/var/lib/mysql mysql:8.4
```

### 4. Iniciar CloudBeaver

```sh
docker run -d --name cloudbeaver-fis --network red-fis -p 127.0.0.1:8978:8978 -v cb_fis_workspace:/opt/cloudbeaver/workspace dbeaver/cloudbeaver:latest
```

Los puertos se publican solo en la interfaz local del equipo. Los volúmenes con nombre se crean automáticamente si no existen.

### 5. Comprobar disponibilidad

```sh
docker ps
docker logs pg-fis
docker logs mysql-fis
docker logs cloudbeaver-fis
```

Esperar a que los motores indiquen que aceptan conexiones. MySQL puede tardar más durante su primera inicialización.

### 6. Abrir el cliente web

Acceder a `http://localhost:8978`, completar el asistente inicial y crear el administrador de CloudBeaver. Esta cuenta es independiente de los usuarios de PostgreSQL y MySQL.

Registrar las conexiones:

| Campo | PostgreSQL | MySQL |
| --- | --- | --- |
| Host | `pg-fis` | `mysql-fis` |
| Puerto | `5432` | `3306` |
| Base de datos | `fis_db` | `fis_db` |
| Usuario | `admin_fis` | `app_fis` |
| Contraseña de laboratorio | `Fis2026*Pg` | `App2026*My` |
| Propiedad del controlador | Sin ajuste adicional | `allowPublicKeyRetrieval=true` si se usa el laboratorio sin SSL |

Para comprobar privilegios administrativos en MySQL se puede crear otra conexión con `root` y la contraseña `Fis2026*My`. La configuración sin SSL corresponde al laboratorio local.

## Datos de ejemplo

Ejecutar cada bloque en el editor SQL de su conexión correspondiente. Las inserciones son de demostración: repetirlas agrega nuevos registros.

### PostgreSQL

```sql
CREATE TABLE IF NOT EXISTS curso (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(80)
);
INSERT INTO curso (nombre) VALUES ('Fundamentos de Ingeniería de Software');
SELECT * FROM curso;
```

### MySQL

```sql
USE fis_db;
CREATE TABLE IF NOT EXISTS curso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80)
);
INSERT INTO curso (nombre) VALUES ('Arquitectura de Software');
SELECT * FROM curso;
```

Las bases de datos son independientes: los registros de un motor no se sincronizan automáticamente con el otro.

## Usuarios y persistencia

- PostgreSQL: `admin_fis` es el superusuario definido en el arranque.
- MySQL: `root` administra el servidor; `app_fis` tiene permisos sobre `fis_db`.
- CloudBeaver: su administrador gestiona el cliente web y sus conexiones.
- Las variables de inicialización crean usuarios y bases cuando el volumen está vacío. Cambiarlas después no actualiza automáticamente las contraseñas existentes.
- Los volúmenes de los motores conservan las tablas y sus registros; el de CloudBeaver conserva su espacio de trabajo.

## Operación

Detener y volver a iniciar los servicios existentes:

```sh
docker stop pg-fis mysql-fis cloudbeaver-fis
docker start pg-fis mysql-fis cloudbeaver-fis
```

Consultar la red y los volúmenes:

```sh
docker network inspect red-fis
docker volume ls
```

Repetir `docker run` con los mismos nombres produce un conflicto si los contenedores ya existen. Para continuar una ejecución previa, usar `docker start`.

## Problemas frecuentes

| Problema | Revisión |
| --- | --- |
| Puerto ocupado | Cambiar solo el puerto del equipo; por ejemplo, `127.0.0.1:5433:5432` |
| CloudBeaver no encuentra el motor | Comprobar que ambos contenedores pertenecen a `red-fis` y usar el nombre correcto como host |
| Contraseña rechazada tras cambiar variables | El volumen conserva las credenciales originales; cambiar la contraseña mediante SQL con acceso autorizado |
| PostgreSQL 18 no inicia | Verificar el montaje en `/var/lib/postgresql` y revisar los registros |
| MySQL muestra `Public Key Retrieval is not allowed` | Revisar la propiedad del controlador indicada para el laboratorio |
| Conexión rechazada al arrancar | Esperar la inicialización y revisar `docker logs` |

## Contexto y pautas para desarrollo con IA

### Objetivo de desarrollo

Mantener un entorno académico sencillo, reproducible y documentado para administrar PostgreSQL y MySQL mediante CloudBeaver. Tomar la guía del taller y los archivos reales del repositorio como referencia; este documento no implica que las mejoras sugeridas estén implementadas.

### Reglas para continuar el proyecto

1. Inspeccionar los archivos existentes antes de modificar el proyecto. No asumir que existen `compose.yaml`, scripts, pruebas, backend o frontend.
2. Conservar los tres servicios, la comunicación interna por nombre y la persistencia mediante volúmenes.
3. Mantener las versiones PostgreSQL 18 y MySQL 8.4, salvo que se solicite una migración justificada. Respetar la ruta de datos correspondiente a cada versión.
4. Usar SQL compatible con cada motor: `SERIAL` en el ejemplo de PostgreSQL y `AUTO_INCREMENT` en MySQL. No asumir que todos los scripts son intercambiables.
5. No agregar frameworks, microservicios, patrones de diseño ni funcionalidades de negocio sin un requisito concreto. El PDF proporcionado define un laboratorio de contenedores.
6. Si se solicita automatización, se puede incorporar Docker Compose como mejora. Documentar los archivos creados, variables, dependencias y comprobaciones de disponibilidad; no darlo por existente.
7. En archivos de automatización, usar variables de entorno y un `.env.example` con valores de muestra. Excluir `.env` del control de versiones y no incorporar credenciales reales.
8. Evitar borrar volúmenes, reinicializar bases o ejecutar operaciones destructivas como parte de una corrección rutinaria. Explicar el impacto antes de proponer un reinicio de datos.
9. Actualizar el README cuando cambien los puertos, nombres, versiones, rutas, credenciales de ejemplo o forma de ejecución.
10. Distinguir siempre entre cambios propuestos, cambios implementados y verificaciones efectivamente ejecutadas.

### Criterios de aceptación

- Los tres contenedores permanecen en ejecución y comparten la red prevista.
- CloudBeaver permite abrir una conexión funcional a cada motor.
- Se pueden crear y consultar registros en la tabla `curso` en ambos motores.
- Los datos permanecen después de detener y reiniciar los servicios; también deben conservarse al recrear contenedores reutilizando sus volúmenes.
- La diferencia entre el usuario administrativo y el usuario de aplicación de MySQL puede verificarse mediante sus permisos.
- La documentación permite repetir el despliegue desde un entorno limpio.

Las validaciones deben ejecutarse cuando Docker esté disponible. Si no lo está, indicar esa limitación y no afirmar que el despliegue fue probado.

## Referencia

Guía académica: **Taller Contenedores y Patrones (1).pdf**, sección «Taller Práctico – Contenedores (DOCKER)».
