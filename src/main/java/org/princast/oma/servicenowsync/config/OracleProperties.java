package org.princast.oma.servicenowsync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bbdd")
public class OracleProperties {

	private String url;
	private String user;
	private String password;

}
