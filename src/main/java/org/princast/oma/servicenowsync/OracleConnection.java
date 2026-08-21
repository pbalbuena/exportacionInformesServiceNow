package org.princast.oma.servicenowsync;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

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

	public void cargarDatos(List<String> scripts) throws SQLException {

		try (Connection conn = DriverManager.getConnection(
				properties.getUrl(), properties.getUser(), properties.getPassword())) {

			ScriptRunner runner = new ScriptRunner(conn, false, true);

			for (String script : scripts) {

				File file = new File(script);

				if (!file.exists()) {
					log.warn("Script SQL no encontrado, se omite: {}", script);
					continue;
				}

				log.info("Ejecutando script SQL: {}", script);

				try (FileReader reader = new FileReader(file)) {
					runner.runScript(reader);
				}
			}

			//conn.commit();

			log.info("Scripts ejecutados correctamente");

		} catch (Exception e) {
			log.error("Error ejecutando los scripts SQL", e);
		}

	}

}
