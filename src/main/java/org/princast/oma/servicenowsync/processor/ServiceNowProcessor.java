package org.princast.oma.servicenowsync.processor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.princast.oma.servicenowsync.OracleConnection;
import org.princast.oma.servicenowsync.config.ServiceNowProperties;
import org.princast.oma.servicenowsync.util.JsonToCsv;
import org.princast.oma.servicenowsync.util.ServiceNowCSVtoScript;
import org.princast.oma.servicenowsync.util.ServiceNowClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceNowProcessor {

	private final ServiceNowProperties properties;
	private final ServiceNowClient serviceNowClient;
	private final OracleConnection oracleConnection;

	public ServiceNowProcessor(ServiceNowProperties properties, ServiceNowClient serviceNowClient,
			OracleConnection oracleConnection) {
		this.properties = properties;
		this.serviceNowClient = serviceNowClient;
		this.oracleConnection = oracleConnection;
	}
	
	private static final DateTimeFormatter FECHA_SERVICENOW = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	final int mes = LocalDate.now().getMonth().getValue() - 1;
	final String fecha = "" + LocalDate.now().getYear() + "-" + mes;

	private String rutaJson(String tabla) {
		return properties.getRutas().get(tabla).getJson() + fecha + ".json";
	}

	private String rutaCsv(String tabla) {
		return properties.getRutas().get(tabla).getCsv() + fecha + ".csv";
	}

	private String rutaSql(String tabla) {
		return properties.getRutas().get(tabla).getSql() + fecha + ".sql";
	}
	
	// Se ejecuta el dia 1 de cada mes a las 5:00 (hora del servidor) para
	// mantener actualizados en base de datos los datos del mes natural anterior.
	@Scheduled(cron = "0 0 5 1 * *")
	public void procesarProgramado() {
		log.info("Iniciando sincronizacion programada mensual con ServiceNow.");
		try {
			procesar(null);
		} catch (Exception e) {
			log.error("Error en la sincronizacion programada mensual con ServiceNow.", e);
		}
	}

	public void procesar(String[] args) throws Exception {

	    String fechaDesde;
	    String fechaHasta;

	    if (args != null && args.length >= 2) {
	        // Permite seguir invocando la app con fechas explicitas (p.ej. jar ejecutable
	        // via spring-boot:run -Dspring-boot.run.arguments="desde hasta").
	        fechaDesde = args[0];
	        fechaHasta = args[1];
	    } else {
	        // Desplegada en JBoss no se reciben argumentos de programa: se usa por
	        // defecto el mes natural anterior.
	        YearMonth mesAnterior = YearMonth.now().minusMonths(1);
	        fechaDesde = mesAnterior.atDay(1).atStartOfDay().format(FECHA_SERVICENOW);
	        fechaHasta = mesAnterior.atEndOfMonth().atTime(23, 59, 59).format(FECHA_SERVICENOW);
	    }

	    log.info("Rango de fechas para la sincronizacion: {} - {}", fechaDesde, fechaHasta);

	    ServiceNowCSVtoScript insert = new ServiceNowCSVtoScript();

	    // Rutas de los scripts SQL generados en esta ejecucion; son los unicos
	    // que se ejecutaran contra Oracle a continuacion.
	    List<String> scriptsGenerados = new ArrayList<>();

	    // Cambios
	    log.info("Descargando datos de la tabla de cambios.");
	    serviceNowClient.descargarJSONCambios(
	            rutaJson("cambios"),
	            fechaDesde,
	            fechaHasta);

	    log.info("Cargando cambios y exportando CSV.");
	    JsonToCsv.CambiosToCSV(
	            rutaJson("cambios"),
	            rutaCsv("cambios"),
	            serviceNowClient);
	    log.info("Cambios convertidos a CSV.");

	    log.info("Convirtiendo CSV en SQL.");
	    insert.csvToScript(
	            rutaCsv("cambios"),
	            rutaSql("cambios"),
	            "MAECAMBIO_SNOW");
	    scriptsGenerados.add(rutaSql("cambios"));


	    // Incidencias
	    log.info("Descargando datos de la tabla de incidencias.");
	    serviceNowClient.descargarJSONIncidencias(
	            rutaJson("incidencias"),
	            fechaDesde,
	            fechaHasta);

	    log.info("Cargando incidencias y exportando CSV.");
	    JsonToCsv.IncidenciasToCSV(
	            rutaJson("incidencias"),
	            rutaCsv("incidencias"),
	            serviceNowClient);
	    log.info("Incidencias convertidas a CSV.");

	    log.info("Convirtiendo CSV en SQL.");
	    insert.csvToScript(
	            rutaCsv("incidencias"),
	            rutaSql("incidencias"),
	            "MAEINCIDENCIA_SNOW");
	    scriptsGenerados.add(rutaSql("incidencias"));


	    // Solicitudes
	    log.info("Descargando datos de la tabla de solicitudes.");
	    serviceNowClient.descargarJSONSolicitudes(
	            rutaJson("solicitudes"),
	            fechaDesde,
	            fechaHasta);

	    log.info("Cargando solicitudes y exportando CSV.");
	    JsonToCsv.SolicitudesToCSV(
	            rutaJson("solicitudes"),
	            rutaCsv("solicitudes"),
	            serviceNowClient);
	    log.info("Solicitudes convertidas a CSV.");

	    log.info("Convirtiendo CSV en SQL.");
	    insert.csvToScript(
	            rutaCsv("solicitudes"),
	            rutaSql("solicitudes"),
	            "MAESOLICITUD_SNOW");
	    scriptsGenerados.add(rutaSql("solicitudes"));


	    // Tareas
	    log.info("Descargando datos de la tabla de tareas.");
	    serviceNowClient.descargarJSONTareas(
	            rutaJson("tareas"),
	            fechaDesde,
	            fechaHasta);

	    log.info("Cargando tareas y exportando CSV.");
	    JsonToCsv.TareasToCSV(
	            rutaJson("tareas"),
	            rutaCsv("tareas"),
	            serviceNowClient);
	    log.info("Tareas convertidas a CSV.");

	    log.info("Convirtiendo CSV en SQL.");
	    insert.csvToScript(
	            rutaCsv("tareas"),
	            rutaSql("tareas"),
	            "MAETAREASOLICITUD_SNOW");
	    scriptsGenerados.add(rutaSql("tareas"));


	    // Relaciones
	    log.info("Descargando datos de la tabla de relaciones.");
	    serviceNowClient.descargarJSONRelaciones(
	            rutaJson("relaciones"),
	            fechaDesde,
	            fechaHasta);

	    log.info("Cargando relaciones y exportando CSV.");
	    JsonToCsv.RelacionesToCSV(
	            rutaJson("relaciones"),
	            rutaCsv("relaciones"));
	    log.info("Relaciones convertidas a CSV.");

	    log.info("Convirtiendo CSV en SQL.");
	    insert.csvToScript(
	            rutaCsv("relaciones"),
	            rutaSql("relaciones"),
	            "MAERELACION_SNOW");
	    scriptsGenerados.add(rutaSql("relaciones"));


	    // Incidencias TMP
	    log.info("Descargando datos de la tabla de incidencias TMP.");
	    serviceNowClient.descargarJSONIncidenciasTMP(
	            rutaJson("incidenciasTmp"),
	            fechaDesde,
	            fechaHasta);

	    log.info("Cargando incidencias TMP y exportando CSV.");
	    JsonToCsv.IncidenciasTMPToCSV(
	            rutaJson("incidenciasTmp"),
	            rutaCsv("incidenciasTmp"),
	            serviceNowClient);
	    log.info("Incidencias TMP convertidas a CSV.");

	    log.info("Convirtiendo CSV en SQL.");
	    insert.csvToScript(
	            rutaCsv("incidenciasTmp"),
	            rutaSql("incidenciasTmp"),
	            "TMP_INCIDENCIA_SLA");
	    scriptsGenerados.add(rutaSql("incidenciasTmp"));


	    // Horas de factoría
	    log.info("Descargando datos de la tabla de horas de factoría.");
	    serviceNowClient.descargarJSONFactoria(
	            rutaJson("factoria"),
	            fechaDesde,
	            fechaHasta);

	    log.info("Cargando horas de factoría y exportando CSV.");
	    JsonToCsv.FactoriaToCSV(
	            rutaJson("factoria"),
	            rutaCsv("factoria"),
	            serviceNowClient);
	    log.info("Horas de factoría convertidas a CSV.");

	    log.info("Convirtiendo CSV en SQL.");
	    insert.csvToScript(
	            rutaCsv("factoria"),
	            rutaSql("factoria"),
	            "TMP_SOLICITUD_SNOW");
	    scriptsGenerados.add(rutaSql("factoria"));

	    oracleConnection.cargarDatos(scriptsGenerados);
	}

}
