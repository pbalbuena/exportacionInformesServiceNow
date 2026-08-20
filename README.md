# ServiceNowSync

Aplicación batch (Spring Boot 3 / Java 17) que sincroniza datos de ServiceNow con la base de datos Oracle de OMA. Descarga informes de ServiceNow, los convierte a CSV y genera e inserta scripts SQL en Oracle, para las siguientes tablas:

- Cambios (`change_request`)
- Incidencias (`incident_sla`)
- Solicitudes (`sc_req_item`)
- Tareas (`sc_task_sla`)
- Relaciones (`task_rel_task`)
- Incidencias temporales (`incident_sla`, subconjunto reducido)
- Horas de factoría (`sc_req_item`, subconjunto reducido)

## Flujo de procesamiento

Por cada tabla, `ServiceNowProcessor` ejecuta la misma tubería:

1. **Descarga** el informe de ServiceNow vía API REST (`ServiceNowClient`) para un rango de fechas.
2. **Convierte** el JSON descargado a CSV (`JsonToCsv`), resolviendo referencias de ServiceNow (campos `link`/`sys_id`) contra la propia API cuando hace falta el valor legible. Cualquier campo del JSON que no esté declarado en el `CsvSchema` de la tabla (columnas que no interesan o que no existen en Oracle) se descarta automáticamente antes de escribir el CSV.
3. **Traduce** el CSV a un script `INSERT` SQL (`ServiceNowCSVtoScript`), usando el mapeo de columnas ServiceNow → Oracle definido en los ficheros `API/*.properties`. El nombre del script (`<tabla><año>-<mes>.sql`) se genera automáticamente a partir de la fecha de ejecución.
4. **Ejecuta** contra Oracle (`OracleConnection`) únicamente los scripts SQL que se acaban de generar en esa misma ejecución (se le pasa la lista de rutas generadas, no una lista fija).

### Rango de fechas

- Si la aplicación se lanza con dos argumentos de programa, se usan como `fechaDesde` y `fechaHasta` (formato `yyyy-MM-dd HH:mm:ss`), p. ej.:

  ```bash
  mvn spring-boot:run -Dspring-boot.run.arguments="2026-06-01 00:00:00,2026-06-30 23:59:59"
  ```

- Si no se reciben argumentos (caso habitual al desplegar en JBoss, donde no hay forma de pasar argumentos de programa), se calcula automáticamente el **mes natural anterior**.

### Ejecución automática

- **Al arrancar/desplegar**: el `CommandLineRunner` de `ServiceNowSyncApplication` lanza `ServiceNowProcessor.procesar()` cada vez que la aplicación arranca (por ejemplo, en cada despliegue del WAR en JBoss).
- **Programada mensual**: `ServiceNowProcessor.procesarProgramado()` está anotado con `@Scheduled(cron = "0 0 5 1 * *")` (requiere `@EnableScheduling` en `ServiceNowSyncApplication`) y se ejecuta automáticamente el **día 1 de cada mes a las 5:00**, hora del servidor donde esté desplegada la aplicación, para mantener la base de datos actualizada con los datos del mes natural anterior sin intervención manual.

## Requisitos

- Java 17
- Maven 3.9+
- Acceso de red a ServiceNow (`itsmasturias.service-now.com`) y a la base de datos Oracle de destino
- Para despliegue: JBoss EAP 8 (u otro contenedor compatible con WAR)

## Configuración

Toda la configuración externalizable vive en `src/main/resources/application.properties` (ver [`ServiceNowProperties`](src/main/java/org/princast/oma/servicenowsync/config/ServiceNowProperties.java) y [`OracleProperties`](src/main/java/org/princast/oma/servicenowsync/config/OracleProperties.java)):

```properties
# Credenciales de ServiceNow (Basic Auth)
servicenow.credentials.user=
servicenow.credentials.password=

# Conexion a Oracle (formato jdbc:oracle:thin:@//host:puerto/service_name)
bbdd.url=jdbc:oracle:thin:@//host:1521/SERVICE_NAME
bbdd.user=
bbdd.password=

# Rutas de trabajo por tabla (json/csv/sql de salida)
servicenow.rutas.<tabla>.json=
servicenow.rutas.<tabla>.csv=
servicenow.rutas.<tabla>.sql=
```

`<tabla>` es una de: `cambios`, `incidencias`, `solicitudes`, `tareas`, `relaciones`, `incidenciasTmp`, `factoria`.

> ⚠️ **No dejes credenciales reales en el `application.properties` empaquetado si el repositorio se sube a control de versiones.** En despliegue, sobrescribe estos valores con un `application.properties` externo (fuera del artefacto) que Spring Boot cargue con prioridad — ver sección de despliegue.

### Mapeo de columnas (`API/*.properties`)

Cada tabla tiene un fichero de mapeo `campo_servicenow=COLUMNA_ORACLE` usado por `ServiceNowCSVtoScript` para construir el `INSERT`:

| Tabla Oracle | Fichero de mapeo |
| --- | --- |
| `MAECAMBIO_SNOW` | `API/cambios.properties` |
| `MAEINCIDENCIA_SNOW` | `API/incidencias.properties` |
| `TMP_INCIDENCIA_SLA` | `API/tmp_incidencias.properties` |
| `MAESOLICITUD_SNOW` | `API/solicitudes.properties` |
| `MAETAREASOLICITUD_SNOW` | `API/tareas.properties` |
| `MAERELACION_SNOW` | `API/relaciones.properties` |
| `TMP_SOLICITUD_SNOW` | `API/tmp_solicitud.properties` |

Los prefijos de columna (`FE_`, `TE_`, `FL_`, `NU_`) determinan cómo se formatea el valor (fecha, texto, booleano S/N o numérico).

## Compilación

```bash
mvn clean package
```

Genera `target/ExportacionInformesServiceNow-1.0.0.war`.

## Ejecución

### En local (jar/war ejecutable)

El WAR se genera con el contenedor Tomcat embebido en scope `provided`, lo que permite ejecutarlo también como artefacto autocontenido:

```bash
java -jar target/ExportacionInformesServiceNow-1.0.0.war
```

o, para desarrollo iterativo:

```bash
mvn spring-boot:run
```

### Despliegue en JBoss EAP 8

1. Copiar `target/ExportacionInformesServiceNow-1.0.0.war` al directorio `standalone/deployments/` de JBoss EAP 8.
2. Externalizar la configuración con credenciales reales (Oracle y ServiceNow) fuera del artefacto, por ejemplo añadiendo al arranque de JBoss:
   ```bash
   -Dspring.config.additional-location=file:/ruta/segura/application.properties
   ```
3. El WAR incluye [`WEB-INF/jboss-deployment-structure.xml`](src/main/webapp/WEB-INF/jboss-deployment-structure.xml), que excluye el subsistema de logging propio de JBoss para que el logging de la aplicación (Logback/SLF4J) no entre en conflicto con `org.jboss.logmanager`.

## Logging

La configuración de logging está en [`logback-spring.xml`](src/main/resources/logback-spring.xml):

- Salida por consola (capturada por `server.log` de JBoss).
- Fichero rotativo diario y por tamaño (`serviceNowSync.log`, 50 MB/fichero, 30 días de histórico, 1 GB de tope), en `${jboss.server.log.dir}` cuando corre dentro de JBoss, o en `./logs` si se ejecuta como jar/war suelto.

## Estructura del proyecto

```text
src/main/java/org/princast/oma/servicenowsync/
├── ServiceNowSyncApplication.java   # Entry point (@EnableScheduling, CommandLineRunner + SpringBootServletInitializer)
├── OracleConnection.java            # Ejecuta contra Oracle la lista de scripts SQL generados en la ejecucion
├── config/
│   ├── ServiceNowProperties.java    # servicenow.* (credenciales + rutas por tabla)
│   └── OracleProperties.java        # bbdd.* (conexión Oracle)
├── processor/
│   └── ServiceNowProcessor.java     # Orquesta la tubería descarga -> csv -> sql por tabla; @Scheduled mensual
└── util/
    ├── ServiceNowClient.java        # Cliente REST de ServiceNow
    ├── JsonToCsv.java                # Conversión JSON -> CSV, resolución de referencias y filtrado de columnas no declaradas
    └── ServiceNowCSVtoScript.java   # Conversión CSV -> script SQL usando API/*.properties

API/            # Mapeos de columnas ServiceNow -> Oracle por tabla
```

## Control de versiones

El `.gitignore` excluye artefactos de compilación (`target/`), ficheros de proyecto específicos de IDE (`.classpath`, `.factorypath`, `.project`, `.settings/` de Eclipse; `.idea/` de IntelliJ), y las carpetas de datos generados en tiempo de ejecución (`JSON/`, `CSV/`, `SQL/`, `logs/` en la raíz del repo).

> ⚠️ **`src/main/resources/application.properties` está versionado con credenciales reales de Oracle y ServiceNow en texto plano.** Esto no debería estar en el historial de git; hay que sacar esas credenciales del fichero empaquetado (dejarlas vacías, como se documenta en la sección de Configuración) y cargarlas solo vía el `application.properties` externo del despliegue.
