package org.princast.oma.servicenowsync.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.princast.oma.servicenowsync.config.ServiceNowProperties.Credentials;
import org.princast.oma.servicenowsync.config.ServiceNowProperties.Tipo;

class ServiceNowPropertiesTest {

    @Test
    void almacenaCredencialesYRutasPorTabla() {
        ServiceNowProperties properties = new ServiceNowProperties();

        Credentials credentials = new Credentials();
        credentials.setUser("usuario");
        credentials.setPassword("clave");
        properties.setCredentials(credentials);

        Tipo tipo = new Tipo();
        tipo.setJson("json/");
        tipo.setCsv("csv/");
        tipo.setSql("sql/");
        properties.getRutas().put("cambios", tipo);

        assertThat(properties.getCredentials().getUser()).isEqualTo("usuario");
        assertThat(properties.getCredentials().getPassword()).isEqualTo("clave");
        assertThat(properties.getRutas().get("cambios").getJson()).isEqualTo("json/");
        assertThat(properties.getRutas().get("cambios").getCsv()).isEqualTo("csv/");
        assertThat(properties.getRutas().get("cambios").getSql()).isEqualTo("sql/");
    }

    @Test
    void empiezaConCredencialesPorDefectoYSinRutas() {
        ServiceNowProperties properties = new ServiceNowProperties();

        assertThat(properties.getCredentials()).isNotNull();
        assertThat(properties.getRutas()).isEmpty();
    }
}
