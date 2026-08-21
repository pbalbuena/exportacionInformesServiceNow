package org.princast.oma.servicenowsync;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.princast.oma.servicenowsync.config.OracleProperties;

class OracleConnectionTest {

    @TempDir
    Path tempDir;

    private OracleProperties propiedadesH2() {
        OracleProperties properties = new OracleProperties();
        // Base de datos H2 en memoria en modo compatibilidad Oracle (soporta TO_DATE),
        // unica por test para evitar interferencias entre ejecuciones.
        properties.setUrl("jdbc:h2:mem:oracleconnection-" + UUID.randomUUID() + ";MODE=Oracle;DB_CLOSE_DELAY=-1");
        properties.setUser("sa");
        properties.setPassword("");
        return properties;
    }

    @Test
    void ejecutaLosScriptsGeneradosYLosDatosQuedanEnLaBaseDeDatos() throws Exception {
        OracleProperties properties = propiedadesH2();
        OracleConnection oracleConnection = new OracleConnection(properties);

        Path script = tempDir.resolve("insert.sql");
        Files.writeString(script, """
                CREATE TABLE TEST_CAMBIOS (CODIGO VARCHAR(20));
                INSERT INTO TEST_CAMBIOS (CODIGO) VALUES ('CHG0001');
                """);

        oracleConnection.cargarDatos(List.of(script.toString()));

        try (Connection conn = DriverManager.getConnection(properties.getUrl(), properties.getUser(), properties.getPassword());
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT CODIGO FROM TEST_CAMBIOS")) {

            org.assertj.core.api.Assertions.assertThat(rs.next()).isTrue();
            org.assertj.core.api.Assertions.assertThat(rs.getString("CODIGO")).isEqualTo("CHG0001");
        }
    }

    @Test
    void unScriptQueNoExisteSeOmiteSinLanzarExcepcion() {
        OracleProperties properties = propiedadesH2();
        OracleConnection oracleConnection = new OracleConnection(properties);

        assertThatCode(() -> oracleConnection.cargarDatos(List.of(tempDir.resolve("no-existe.sql").toString())))
                .doesNotThrowAnyException();
    }

    @Test
    void unScriptConSqlInvalidoNoPropagaExcepcion() throws Exception {
        OracleProperties properties = propiedadesH2();
        OracleConnection oracleConnection = new OracleConnection(properties);

        Path script = tempDir.resolve("invalido.sql");
        Files.writeString(script, "ESTO NO ES SQL VALIDO;");

        assertThatCode(() -> oracleConnection.cargarDatos(List.of(script.toString()))).doesNotThrowAnyException();
    }

    @Test
    void unaListaVaciaNoLanzaExcepcion() {
        OracleProperties properties = propiedadesH2();
        OracleConnection oracleConnection = new OracleConnection(properties);

        assertThatCode(() -> oracleConnection.cargarDatos(List.of())).doesNotThrowAnyException();
    }
}
