package org.princast.oma.servicenowsync;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.princast.oma.servicenowsync.config.OracleProperties;
import org.springframework.stereotype.Component;

import com.ibatis.common.jdbc.ScriptRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OracleConnection {

	private final OracleProperties properties;

	public OracleConnection(OracleProperties properties) {
		this.properties = properties;
	}

	public void cargarDatos() throws SQLException {

		try (Connection conn = DriverManager.getConnection(
				properties.getUrl(), properties.getUser(), properties.getPassword())) {

			ScriptRunner runner = new ScriptRunner(conn, false, true);

			runner.runScript(new FileReader("SQL/Cambios2026-6.sql"));
			runner.runScript(new FileReader("SQL/Factoria2026-6.sql"));
			runner.runScript(new FileReader("SQL/Incidencias2026-6.sql"));
			runner.runScript(new FileReader("SQL/IncidenciasTMP2026-6.sql"));
			runner.runScript(new FileReader("SQL/Relaciones2026-6.sql"));
			runner.runScript(new FileReader("SQL/Solicitudes2026-6.sql"));
			runner.runScript(new FileReader("SQL/Tareas2026-6.sql"));

			//conn.commit();

			log.info("Scripts ejecutados correctamente");

		} catch (Exception e) {
			log.error("Error ejecutando los scripts SQL", e);
		}

	}

}
