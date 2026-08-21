package org.princast.oma.servicenowsync.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.princast.oma.servicenowsync.OracleConnection;
import org.princast.oma.servicenowsync.config.ServiceNowProperties;
import org.princast.oma.servicenowsync.config.ServiceNowProperties.Tipo;
import org.princast.oma.servicenowsync.util.ServiceNowClient;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ServiceNowProcessorTest {

    private static final DateTimeFormatter FECHA_SERVICENOW = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> TABLAS =
            List.of("cambios", "incidencias", "solicitudes", "tareas", "relaciones", "incidenciasTmp", "factoria");

    @TempDir
    Path tempDir;

    @Mock
    private ServiceNowClient serviceNowClient;

    @Mock
    private OracleConnection oracleConnection;

    private ServiceNowProperties properties;
    private ServiceNowProcessor processor;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        properties = new ServiceNowProperties();
        for (String tabla : TABLAS) {
            Tipo tipo = new Tipo();
            tipo.setJson(tempDir.resolve(tabla).resolve("json").toString() + "/");
            tipo.setCsv(tempDir.resolve(tabla).resolve("csv").toString() + "/");
            tipo.setSql(tempDir.resolve(tabla).resolve("sql").toString() + "/");
            properties.getRutas().put(tabla, tipo);

            // En produccion estas carpetas ya existen de antemano; aqui hay que crearlas,
            // igual que las de "csv" y "sql" (JsonToCsv/ServiceNowCSVtoScript no las crean).
            Files.createDirectories(Path.of(tipo.getJson()));
            Files.createDirectories(Path.of(tipo.getCsv()));
            Files.createDirectories(Path.of(tipo.getSql()));
        }

        processor = new ServiceNowProcessor(properties, serviceNowClient, oracleConnection);

        // Cada descarga simulada escribe un JSON valido (sin filas) en la ruta pedida,
        // igual que haria ServiceNowClient de verdad, para que JsonToCsv / ServiceNowCSVtoScript
        // (que no se mockean) puedan seguir procesando el fichero. Se marca como "lenient"
        // porque no todos los tests llegan a invocar los 7 metodos de descarga.
        doAnswerEscribeJson(List.of());
    }

    private void doAnswerEscribeJson(List<Map<String, Object>> filas) throws Exception {
        org.mockito.stubbing.Answer<Void> respuesta = invocation -> {
            String rutaDestino = invocation.getArgument(0);
            Path destino = Path.of(rutaDestino);
            Files.createDirectories(destino.getParent());
            mapper.writeValue(destino.toFile(), Map.of("result", filas));
            return null;
        };

        org.mockito.Mockito.lenient().doAnswer(respuesta).when(serviceNowClient).descargarJSONCambios(anyString(), anyString(), anyString());
        org.mockito.Mockito.lenient().doAnswer(respuesta).when(serviceNowClient).descargarJSONIncidencias(anyString(), anyString(), anyString());
        org.mockito.Mockito.lenient().doAnswer(respuesta).when(serviceNowClient).descargarJSONSolicitudes(anyString(), anyString(), anyString());
        org.mockito.Mockito.lenient().doAnswer(respuesta).when(serviceNowClient).descargarJSONTareas(anyString(), anyString(), anyString());
        org.mockito.Mockito.lenient().doAnswer(respuesta).when(serviceNowClient).descargarJSONRelaciones(anyString(), anyString(), anyString());
        org.mockito.Mockito.lenient().doAnswer(respuesta).when(serviceNowClient).descargarJSONIncidenciasTMP(anyString(), anyString(), anyString());
        org.mockito.Mockito.lenient().doAnswer(respuesta).when(serviceNowClient).descargarJSONFactoria(anyString(), anyString(), anyString());
    }

    @Test
    void procesarConFechasExplicitasLasPropagaAServiceNow() throws Exception {
        processor.procesar(new String[] { "2026-01-01 00:00:00", "2026-01-31 23:59:59" });

        verify(serviceNowClient).descargarJSONCambios(anyString(), org.mockito.ArgumentMatchers.eq("2026-01-01 00:00:00"),
                org.mockito.ArgumentMatchers.eq("2026-01-31 23:59:59"));
    }

    @Test
    void procesarSinArgumentosUsaElMesNaturalAnterior() throws Exception {
        YearMonth mesAnterior = YearMonth.now().minusMonths(1);
        String fechaDesdeEsperada = mesAnterior.atDay(1).atStartOfDay().format(FECHA_SERVICENOW);
        String fechaHastaEsperada = mesAnterior.atEndOfMonth().atTime(23, 59, 59).format(FECHA_SERVICENOW);

        processor.procesar(null);

        verify(serviceNowClient).descargarJSONCambios(anyString(), org.mockito.ArgumentMatchers.eq(fechaDesdeEsperada),
                org.mockito.ArgumentMatchers.eq(fechaHastaEsperada));
    }

    @Test
    void procesarGeneraUnScriptSqlPorTablaYLosEjecutaContraOracle() throws Exception {
        processor.procesar(null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(oracleConnection).cargarDatos(captor.capture());

        List<String> scripts = captor.getValue();
        assertThat(scripts).hasSize(TABLAS.size());

        for (String script : scripts) {
            assertThat(Files.exists(Path.of(script))).as("El script %s deberia haberse generado", script).isTrue();
        }
    }

    @Test
    void procesarConDatosDeCambiosGeneraElInsertCorrespondiente() throws Exception {
        doAnswerEscribeJson(List.of());
        org.mockito.Mockito.doAnswer(invocation -> {
            String rutaDestino = invocation.getArgument(0);
            Path destino = Path.of(rutaDestino);
            Files.createDirectories(destino.getParent());
            Map<String, Object> fila = Map.of("number", "CHG0055", "state", "3");
            mapper.writeValue(destino.toFile(), Map.of("result", List.of(fila)));
            return null;
        }).when(serviceNowClient).descargarJSONCambios(anyString(), anyString(), anyString());

        processor.procesar(null);

        Path sqlCambios = Path.of(properties.getRutas().get("cambios").getSql() + processor.fecha + ".sql");
        String contenido = Files.readString(sqlCambios);

        assertThat(contenido).contains("INSERT INTO MAECAMBIO_SNOW").contains("CHG0055");
    }

    @Test
    void procesarProgramadoDelegaEnProcesarConMesAnterior() throws Exception {
        ServiceNowProcessor spyProcessor = spy(processor);
        doNothing().when(spyProcessor).procesar(null);

        spyProcessor.procesarProgramado();

        verify(spyProcessor, times(1)).procesar(null);
    }

    @Test
    void procesarProgramadoNoPropagaExcepcionesDelProcesamiento() throws Exception {
        ServiceNowProcessor spyProcessor = spy(processor);
        doThrow(new RuntimeException("fallo simulado")).when(spyProcessor).procesar(null);

        assertThatCode(spyProcessor::procesarProgramado).doesNotThrowAnyException();
    }

    @Test
    void procesarProgramadoTieneElCronMensualEsperado() throws Exception {
        Method metodo = ServiceNowProcessor.class.getDeclaredMethod("procesarProgramado");
        Scheduled anotacion = metodo.getAnnotation(Scheduled.class);

        assertThat(anotacion).isNotNull();
        assertThat(anotacion.cron()).isEqualTo("0 0 5 1 * *");
    }
}
