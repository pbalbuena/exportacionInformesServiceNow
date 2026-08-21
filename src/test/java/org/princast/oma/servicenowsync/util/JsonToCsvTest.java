package org.princast.oma.servicenowsync.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class JsonToCsvTest {

    @TempDir
    Path tempDir;

    @Mock
    private ServiceNowClient serviceNowClient;

    private final ObjectMapper mapper = new ObjectMapper();

    private Path escribirJson(String nombre, List<Map<String, Object>> filas) throws Exception {
        Path entrada = tempDir.resolve(nombre);
        mapper.writeValue(entrada.toFile(), Map.of("result", filas));
        return entrada;
    }

    @Test
    void cambiosToCsvDescartaCamposNoDeclaradosEnElSchema() throws Exception {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("number", "CHG0001");
        fila.put("state", "3");
        fila.put("campo_no_declarado", "no deberia aparecer");

        Path entrada = escribirJson("cambios.json", List.of(fila));
        Path salida = tempDir.resolve("cambios.csv");

        JsonToCsv.CambiosToCSV(entrada.toString(), salida.toString(), serviceNowClient);

        String csv = Files.readString(salida);
        assertThat(csv).contains("CHG0001").contains("number").doesNotContain("campo_no_declarado");
    }

    @Test
    void incidenciasToCsvDescartaCampoDesconocidoDeServiceNow() throws Exception {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("inc_number", "INC0001");
        fila.put("inc_active", "true");
        fila.put("inc_u_security_incident_created", "campo nuevo de ServiceNow");

        Path entrada = escribirJson("incidencias.json", List.of(fila));
        Path salida = tempDir.resolve("incidencias.csv");

        JsonToCsv.IncidenciasToCSV(entrada.toString(), salida.toString(), serviceNowClient);

        String csv = Files.readString(salida);
        assertThat(csv).contains("INC0001").doesNotContain("inc_u_security_incident_created");
    }

    @Test
    void solicitudesToCsvSoloConservaColumnasDelBuilder() throws Exception {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("number", "RITM0001");
        fila.put("state", "closed");
        fila.put("otro_campo_sin_mapear", "x");

        Path entrada = escribirJson("solicitudes.json", List.of(fila));
        Path salida = tempDir.resolve("solicitudes.csv");

        JsonToCsv.SolicitudesToCSV(entrada.toString(), salida.toString(), serviceNowClient);

        String csv = Files.readString(salida);
        assertThat(csv).contains("RITM0001").doesNotContain("otro_campo_sin_mapear");
    }

    @Test
    void tareasToCsvSoloConservaColumnasDelBuilder() throws Exception {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("sct_number", "SCTASK0001");
        fila.put("sct_state", "3");
        fila.put("campo_extra", "x");

        Path entrada = escribirJson("tareas.json", List.of(fila));
        Path salida = tempDir.resolve("tareas.csv");

        JsonToCsv.TareasToCSV(entrada.toString(), salida.toString(), serviceNowClient);

        String csv = Files.readString(salida);
        assertThat(csv).contains("SCTASK0001").doesNotContain("campo_extra");
    }

    @Test
    void incidenciasTmpToCsvSoloConservaColumnasDelBuilder() throws Exception {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("inc_number", "INC0002");
        fila.put("inc_subcategory", "red");
        fila.put("inc_priority", "no deberia aparecer");

        Path entrada = escribirJson("incidenciasTmp.json", List.of(fila));
        Path salida = tempDir.resolve("incidenciasTmp.csv");

        JsonToCsv.IncidenciasTMPToCSV(entrada.toString(), salida.toString(), serviceNowClient);

        String csv = Files.readString(salida);
        assertThat(csv).contains("INC0002").contains("red").doesNotContain("inc_priority");
    }

    @Test
    void factoriaToCsvSoloConservaColumnasDelBuilder() throws Exception {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("number", "RITM0002");
        fila.put("assignment_group", "Factoria");
        fila.put("campo_sin_mapear", "x");

        Path entrada = escribirJson("factoria.json", List.of(fila));
        Path salida = tempDir.resolve("factoria.csv");

        JsonToCsv.FactoriaToCSV(entrada.toString(), salida.toString(), serviceNowClient);

        String csv = Files.readString(salida);
        assertThat(csv).contains("RITM0002").doesNotContain("campo_sin_mapear");
    }

    @Test
    void relacionesToCsvSustituyeReferenciasPorSuDisplayValue() throws Exception {
        Map<String, Object> parentRef = new LinkedHashMap<>();
        parentRef.put("link", "https://fake.example/parent");
        parentRef.put("display_value", "CHG0099");

        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("parent", parentRef);
        fila.put("child", "CHG0100");

        Path entrada = escribirJson("relaciones.json", List.of(fila));
        Path salida = tempDir.resolve("relaciones.csv");

        JsonToCsv.RelacionesToCSV(entrada.toString(), salida.toString());

        String csv = Files.readString(salida);
        assertThat(csv).contains("CHG0099").contains("CHG0100").doesNotContain("https://fake.example/parent");
    }

    @Test
    void procesarReferenciasResuelveElLinkContraLaApiDeServiceNow() throws Exception {
        Map<String, Object> referencia = new LinkedHashMap<>();
        referencia.put("link", "https://fake.example/api/resolver-ok");
        referencia.put("display_value", "Valor por defecto");

        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("assigned_to", referencia);

        when(serviceNowClient.getDataFromUrl("https://fake.example/api/resolver-ok"))
                .thenReturn(Map.of("result", Map.of("name", "Juan Perez")));

        JsonToCsv.procesarReferencias(fila, serviceNowClient);

        assertThat(fila.get("assigned_to")).isEqualTo("Juan Perez");
    }

    @Test
    void procesarReferenciasUsaDisplayValueSiFallaLaLlamadaAServiceNow() throws Exception {
        Map<String, Object> referencia = new LinkedHashMap<>();
        referencia.put("link", "https://fake.example/api/resolver-error");
        referencia.put("display_value", "Valor de respaldo");

        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("assigned_to", referencia);

        when(serviceNowClient.getDataFromUrl(anyString())).thenThrow(new RuntimeException("Timeout simulado"));

        JsonToCsv.procesarReferencias(fila, serviceNowClient);

        assertThat(fila.get("assigned_to")).isEqualTo("Valor de respaldo");
    }

    @Test
    void procesarReferenciasIgnoraCamposQueNoSonReferencias() throws Exception {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("number", "CHG0001");
        fila.put("otro", 42);

        JsonToCsv.procesarReferencias(fila, serviceNowClient);

        assertThat(fila.get("number")).isEqualTo("CHG0001");
        assertThat(fila.get("otro")).isEqualTo(42);
    }

    @Test
    void getPrimerValorDevuelveElPrimerCampoConContenido() {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("a", "");
        fila.put("b", "   ");
        fila.put("c", "valor-b");

        assertThat(JsonToCsv.getPrimerValor(fila, "a", "b", "c")).isEqualTo("valor-b");
    }

    @Test
    void getPrimerValorDevuelveNullSiNingunCampoTieneContenido() {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("a", "");

        assertThat(JsonToCsv.getPrimerValor(fila, "a", "b")).isNull();
    }
}
