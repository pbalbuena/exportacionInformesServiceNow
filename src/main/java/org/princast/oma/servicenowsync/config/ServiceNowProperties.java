package org.princast.oma.servicenowsync.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "servicenow")
public class ServiceNowProperties {

	private Map<String, Tipo> rutas = new HashMap<>();

	private Credentials credentials = new Credentials();

	@Getter
	@Setter
	public static class Tipo {

		private String json;
		private String csv;
		private String sql;

	}

	@Getter
	@Setter
	public static class Credentials {

		private String user;
		private String password;

	}

}