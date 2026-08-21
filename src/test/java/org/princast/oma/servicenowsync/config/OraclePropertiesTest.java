package org.princast.oma.servicenowsync.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OraclePropertiesTest {

    @Test
    void almacenaLosDatosDeConexion() {
        OracleProperties properties = new OracleProperties();

        properties.setUrl("jdbc:oracle:thin:@//host:1521/SERVICE");
        properties.setUser("usuario");
        properties.setPassword("clave");

        assertThat(properties.getUrl()).isEqualTo("jdbc:oracle:thin:@//host:1521/SERVICE");
        assertThat(properties.getUser()).isEqualTo("usuario");
        assertThat(properties.getPassword()).isEqualTo("clave");
    }
}
