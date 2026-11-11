# Taller de Contenedores y Patrones — Gestión de artículos

## Integrantes

- Kevin Garay
- Violeta Fajardo
- Julian Perez
- Jose Guerrero

## Descripción

Aplicación Java de consola para registrar y consultar artículos en PostgreSQL o MySQL mediante JDBC. Utiliza una fábrica de adaptadores para seleccionar el motor desde archivos de propiedades y un DAO para separar las operaciones SQL del programa principal. Los motores se ejecutan en Docker y pueden administrarse desde CloudBeaver.

Proyecto académico de Arquitectura de Software, Ingeniería de Sistemas.

## Estado y alcance implementado

Según lo reportado por el equipo, ya están implementados el entorno de contenedores, las clases Java compartidas, la configuración de conexiones y la tabla `articulos` en ambos motores. El desarrollo debe continuar sobre esta base.

La aplicación permite insertar y listar artículos. En cada ejecución, el programa principal consulta los registros, intenta guardar dos artículos de ejemplo y vuelve a consultar para mostrar la cantidad y el contenido en consola. La implementación compartida no incluye métodos de actualización o eliminación en el DAO.

La revisión de este README se basa en los archivos y scripts proporcionados; no constituye una prueba de ejecución del proyecto.

## Tecnologías

| Tecnología | Versión de referencia | Uso |
| --- | --- | --- |
| Docker Engine / Docker Desktop | Compatible con el equipo | Ejecución y aislamiento de servicios |
| PostgreSQL | 18 | Motor de base de datos relacional |
| MySQL | 8.4 | Motor de base de datos relacional |
| CloudBeaver Community | `latest` en la guía | Cliente web y editor SQL |
| Java / JDK | Según la configuración del proyecto | Aplicación de consola y clases de acceso a datos |
| JDBC | Controladores de PostgreSQL y MySQL | Conexiones y consultas desde Java |
| SQL | Dialecto de cada motor | Definición y consulta de datos |
| Archivos `.properties` | Recursos del classpath | Selección del adaptador y parámetros de conexión |
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

## Despliegue de infraestructura desde cero

El entorno ya está implementado. Si los contenedores existen, usar la sección de operación para iniciarlos; estos comandos quedan como referencia para reproducir la infraestructura en otro equipo.

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

## Modelo de datos implementado

La tabla `articulos` ya fue añadida en MySQL y PostgreSQL con el mismo script:

```sql
CREATE TABLE articulos (
    idarticulo NUMERIC(6,0) PRIMARY KEY,
    nombrearticulo VARCHAR(50) NOT NULL,
    valor NUMERIC(7,2) NOT NULL
);
```

| Columna | Tipo | Uso |
| --- | --- | --- |
| `idarticulo` | `NUMERIC(6,0)` | Identificador único, suministrado por Java; no es autoincremental |
| `nombrearticulo` | `VARCHAR(50)` | Nombre obligatorio del artículo |
| `valor` | `NUMERIC(7,2)` | Valor obligatorio con dos decimales |

El script documenta el esquema existente. Ejecutarlo otra vez sobre la misma tabla produce un error de objeto existente. Para un entorno nuevo, aplicarlo en la base configurada para cada motor.

```sql
SELECT idarticulo, nombrearticulo, valor FROM articulos;
```

Los motores mantienen datos independientes. Cambiar el adaptador no copia ni sincroniza artículos entre ellos.

## Componentes Java implementados

| Paquete / componente | Responsabilidad |
| --- | --- |
| `entidades.articulos` | Modelo con `Long idarticulo`, `String nombrearticulo` y `double valor`; constructores, getters, setters y `toString()` |
| `dao.DAOarticulos` | `findAllProducts()` lista los artículos y `saveProduct(articulos)` inserta uno mediante `PreparedStatement` |
| `patronfactorymethod.IntAdaptador` | Contrato `Connection getConnection()` |
| `implementacion.AdaptadorMysql` | Construye la URL JDBC y abre conexiones a MySQL |
| `implementacion.AdaptadorPostgresql` | Construye la URL JDBC y abre conexiones a PostgreSQL |
| `patronfactorymethod.FactoryBaseDatos` | Crea adaptadores por `TiposBD` o por el nombre de clase definido en propiedades |
| `patronfactorymethod.TiposBD` | Enumeración con `MySQL` y `POstgresql`, respetando los nombres actuales |
| `utilidades.UtilidadesAcceso` | Carga propiedades mediante el classloader |
| `patronfactorymethod.PatronFactoryMethod` | Punto de entrada `main`; inserta dos artículos de ejemplo y muestra el listado |

La fábrica centraliza la creación del adaptador y el DAO usa su interfaz común. El proyecto presenta este mecanismo como práctica de Factory Method y lo combina con DAO y adaptadores de conexión.

## Configuración de la aplicación

Los recursos deben estar disponibles en el classpath dentro de `Propiedades/`.

### Selección del motor

Archivo `Propiedades/ConfiguracionFactory.properties`:

```properties
defaultDBClass=implementacion.AdaptadorPostgresql
#defaultDBClass=implementacion.AdaptadorMysql
```

Mantener una sola selección activa. El constructor de `DAOarticulos` obtiene el adaptador por esta configuración; para cambiar el motor, editar la propiedad y volver a ejecutar la aplicación.

### MySQL

El adaptador busca exactamente `Propiedades/DatosMySQL.properties`:

```properties
host=localhost
port=3306
dbname=patrones
user=USUARIO_MYSQL
password=CONTRASENA_MYSQL
```

El encabezado del material compartido dice `DatosMysql.properties`, pero el código carga `DatosMySQL.properties`. Comprobar el nombre real y unificarlo con la ruta del código, especialmente en sistemas que distinguen mayúsculas.

### PostgreSQL

El adaptador busca exactamente `Propiedades/DatosPOstgresql.properties`:

```properties
host=localhost
port=5432
service=patrones
user=USUARIO_POSTGRESQL
password=CONTRASENA_POSTGRESQL
```

`service` representa el nombre de la base de datos en la URL JDBC. Los valores en mayúsculas son marcadores que deben sustituirse por credenciales válidas.

### Integración con Docker

Los archivos Java compartidos apuntan a la base `patrones`, mientras que el despliegue de referencia del taller crea `fis_db`. Alinear ambos: usar `patrones` si es la base donde ya se creó `articulos`, o configurar `dbname` y `service` con `fis_db` si la tabla está allí. No asumir que las dos bases son equivalentes ni que el cambio de propiedades crea una base o tabla.

También deben coincidir los usuarios y contraseñas con los realmente configurados en cada motor. No sustituir credenciales de un entorno existente basándose solo en los ejemplos del despliegue.

- Java ejecutado en el equipo: `localhost` y el puerto publicado del motor.
- CloudBeaver dentro de `red-fis`: `pg-fis:5432` y `mysql-fis:3306`.
- Si en el futuro Java se ejecuta en un contenedor de esa red: usar los nombres internos de los motores.

## Ejecución de la aplicación Java

1. Iniciar los contenedores existentes y comprobar que los motores acepten conexiones.
2. Abrir el proyecto Java existente en el IDE o utilizar su herramienta de compilación real.
3. Incluir los controladores JDBC de PostgreSQL y MySQL en las dependencias de compilación y ejecución.
4. Verificar que los archivos de `Propiedades/` se copien al classpath con sus nombres exactos.
5. Seleccionar el adaptador y comprobar base, usuario, contraseña, host y puerto.
6. Confirmar que `articulos` exista en la base seleccionada y que el usuario tenga permisos de lectura e inserción.
7. Ejecutar `patronfactorymethod.PatronFactoryMethod` como aplicación Java.

El código MySQL compartido referencia `com.mysql.jdbc.Driver`. La dependencia debe ser compatible con esa referencia o debe actualizarse el código junto con el controlador elegido. No se especifican versiones de JDK o de los controladores porque no se proporcionó la configuración de compilación.

No se presupone Maven, Gradle ni un JAR ejecutable: el material compartido contiene clases y propiedades, pero no incluye un descriptor de construcción que permita documentar un comando de compilación exacto.

### Resultado esperado

En una base vacía, el programa intenta insertar `Articulo A1` con valor `120` y `Articulo B2` con valor `130`, y muestra el total y cada artículo. En ejecuciones posteriores calcula los identificadores desde la cantidad de registros existentes.

La generación actual mediante `cantidad + 1` puede producir claves duplicadas si hay huecos en los identificadores o ejecuciones simultáneas. Debe tratarse como una limitación de la implementación, no como garantía de unicidad.

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

### Punto de partida

El equipo ya implementó la infraestructura y el código Java documentado. Continuar el proyecto existente y conservar sus datos. Antes de editar, revisar los archivos reales del repositorio y contrastarlos con las clases compartidas. No reconstruir desde cero ni tratar las clases, el DAO, la fábrica o la tabla `articulos` como funcionalidades pendientes.

### Reglas de desarrollo

1. Mantener la separación entre entidad, DAO, adaptadores, fábrica, utilidades y punto de entrada. Centralizar el SQL de artículos en el DAO.
2. Conservar la selección del motor mediante `defaultDBClass` y la interfaz `IntAdaptador`.
3. Mantener compatibilidad con PostgreSQL y MySQL. El esquema actual de `articulos` es común a ambos motores y el identificador se asigna desde Java.
4. Preservar los nombres actuales de clases, paquetes, métodos y recursos; si se corrigen sus convenciones o mayúsculas, actualizar todas sus referencias y documentar el cambio.
5. Verificar la configuración real de la base `patrones` o `fis_db` antes de modificar conexiones. No cambiar credenciales, bases ni puertos de un entorno existente por asumir que usa los ejemplos del taller.
6. No agregar frontend, API, frameworks ni funcionalidades sin un requisito concreto. El programa Java actual es de consola y permite insertar y listar; CloudBeaver es el cliente web administrativo.
7. No asumir que existen Maven, Gradle, Docker Compose o pruebas automatizadas. Revisar el repositorio y usar su mecanismo real de construcción.
8. Evitar borrar volúmenes, recrear tablas o reinicializar datos como corrección rutinaria. Cualquier evolución del esquema debe preservar los registros existentes.
9. Mantener consultas parametrizadas. Evitar registrar contraseñas: el adaptador MySQL compartido imprime una URL que contiene credenciales; eliminar o enmascarar esa información al mejorar el código.
10. Actualizar este documento al cambiar configuración, dependencias, comandos o comportamiento. Separar lo implementado de las mejoras propuestas y de las pruebas realmente ejecutadas.

### Mejoras identificadas, aún no acreditadas como implementadas

- Sustituir `cantidad + 1` por una estrategia de identificadores que evite colisiones, coordinando los cambios entre Java y los dos motores.
- Usar `try-with-resources` para cerrar conexiones, sentencias y resultados.
- Tratar explícitamente fallos de conexión y carga de propiedades. Actualmente algunos métodos devuelven `null` y el programa principal asume una lista válida.
- Comprobar el resultado de `saveProduct()` y comunicar inserciones fallidas.
- Evaluar `BigDecimal` para representar valores monetarios en lugar de `double`.
- Actualizar la creación por reflexión y revisar la compatibilidad de los controladores JDBC.
- Externalizar credenciales reales y excluirlas del control de versiones.

### Validación de cambios

- Compilar usando la configuración real del proyecto.
- Ejecutar la aplicación por separado con cada adaptador y verificar inserciones y consultas en `articulos`.
- Confirmar que el cambio de motor se realiza mediante propiedades y que las bases conservan datos independientes.
- Verificar persistencia al detener y reiniciar los contenedores.
- Probar errores relevantes a cada cambio, como conexión no disponible o identificador duplicado.
- Si no se dispone del proyecto completo o de Docker, indicar esa limitación y no afirmar que la ejecución fue validada.

## Referencias

- Guía académica: **Taller Contenedores y Patrones (1).pdf**.
- Clases Java y archivos de propiedades compartidos por el equipo en **Markdown pegado(1).md**.
- Scripts de creación de `articulos` para MySQL y PostgreSQL proporcionados por el equipo.
