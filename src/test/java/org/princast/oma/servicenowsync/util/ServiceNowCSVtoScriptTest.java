package org.princast.oma.servicenowsync.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServiceNowCSVtoScriptTest {

    @TempDir
    Path tempDir;

    private final ServiceNowCSVtoScript convertidor = new ServiceNowCSVtoScript();

    private Path escribirCsv(String nombre, List<String> lineas) throws Exception {
        Path csv = tempDir.resolve(nombre);
        Files.write(csv, lineas);
        return csv;
    }

    @Test
    void generaInsertsParaCambiosConFechaValidaYCamposNumericos() throws Exception {
        Path csv = escribirCsv("cambios.csv", List.of(
                "number,short_description,start_date,sys_mod_count",
                "CHG0001,Despliegue,15/06/2026 10:30:00,7"));
        Path sql = tempDir.resolve("cambios.sql");

        convertidor.csvToScript(csv.toString(), sql.toString(), "MAECAMBIO_SNOW");

        String contenido = Files.readString(sql);
        assertThat(contenido)
                .contains("INSERT INTO MAECAMBIO_SNOW")
                .contains("CA_CODIGO")
                .contains("'CHG0001'")
                .contains("TO_DATE('2026-06-15 10:30:00', 'YYYY-MM-DD HH24:mi:ss')")
                .contains(", 7)");
    }

    @Test
    void fechaInvalidaSeConvierteEnNull() throws Exception {
        Path csv = escribirCsv("cambios_fecha_mala.csv", List.of(
                "number,start_date",
                "CHG0002,fecha-no-valida"));
        Path sql = tempDir.resolve("cambios_fecha_mala.sql");

        convertidor.csvToScript(csv.toString(), sql.toString(), "MAECAMBIO_SNOW");

        String contenido = Files.readString(sql);
        assertThat(contenido).contains("NULL");
    }

    @Test
    void duracionEnTextoSeConvierteASegundosCuandoNoEsNumerica() throws Exception {
        Path csv = escribirCsv("cambios_duracion.csv", List.of(
                "number,sys_mod_count",
                "CHG0003,1 Dias 2 Horas 30 minutos"));
        Path sql = tempDir.resolve("cambios_duracion.sql");

        convertidor.csvToScript(csv.toString(), sql.toString(), "MAECAMBIO_SNOW");

        String contenido = Files.readString(sql);
        long segundosEsperados = 86400 + 2 * 3600 + 30 * 60;
        assertThat(contenido).contains(String.valueOf(segundosEsperados));
    }

    @Test
    void generaInsertsParaIncidenciasConvirtiendoBooleanosAsN() throws Exception {
        Path csv = escribirCsv("incidencias.csv", List.of(
                "inc_number,inc_active",
                "INC0001,verdadero"));
        Path sql = tempDir.resolve("incidencias.sql");

        convertidor.csvToScript(csv.toString(), sql.toString(), "MAEINCIDENCIA_SNOW");

        String contenido = Files.readString(sql);
        assertThat(contenido)
                .contains("INSERT INTO MAEINCIDENCIA_SNOW")
                .contains("CA_CODIGO")
                .contains("FL_ACTIVO")
                .contains("'S'");
    }

    @Test
    void booleanoFalsoSeConvierteAN() throws Exception {
        Path csv = escribirCsv("incidencias_falso.csv", List.of(
                "inc_number,inc_active",
                "INC0002,falso"));
        Path sql = tempDir.resolve("incidencias_falso.sql");

        convertidor.csvToScript(csv.toString(), sql.toString(), "MAEINCIDENCIA_SNOW");

        String contenido = Files.readString(sql);
        assertThat(contenido).contains("'N'");
    }

    @Test
    void columnasNoPresentesEnElMappingSeIgnoran() throws Exception {
        Path csv = escribirCsv("cambios_columna_extra.csv", List.of(
                "number,campo_no_mapeado",
                "CHG0004,valor-ignorado"));
        Path sql = tempDir.resolve("cambios_columna_extra.sql");

        convertidor.csvToScript(csv.toString(), sql.toString(), "MAECAMBIO_SNOW");

        String contenido = Files.readString(sql);
        assertThat(contenido).contains("CHG0004").doesNotContain("valor-ignorado");
    }

    @Test
    void tablaNoReconocidaUsaElMappingPorDefecto() throws Exception {
        Path csv = escribirCsv("solicitud_tmp.csv", List.of(
                "number",
                "RITM0099"));
        Path sql = tempDir.resolve("solicitud_tmp.sql");

        convertidor.csvToScript(csv.toString(), sql.toString(), "TMP_SOLICITUD_SNOW");

        String contenido = Files.readString(sql);
        assertThat(contenido).contains("INSERT INTO TMP_SOLICITUD_SNOW");
    }

    @Test
    void csvVacioNoLanzaExcepcionYNoGeneraInserts() throws Exception {
        Path csv = tempDir.resolve("vacio.csv");
        Files.createFile(csv);
        Path sql = tempDir.resolve("vacio.sql");

        convertidor.csvToScript(csv.toString(), sql.toString(), "MAECAMBIO_SNOW");

        assertThat(Files.readString(sql)).doesNotContain("INSERT INTO");
    }

    @Test
    void loadPropertiesDevuelvePropiedadesVaciasSiElFicheroNoExiste() {
        var props = ServiceNowCSVtoScript.loadProperties("API/no-existe.properties");
        assertThat(props).isEmpty();
    }
}
