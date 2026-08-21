package org.princast.oma.servicenowsync.util;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.princast.oma.servicenowsync.config.ServiceNowProperties;
import org.princast.oma.servicenowsync.config.ServiceNowProperties.Credentials;

/**
 * ServiceNowClient llama a la API real de ServiceNow instanciando su propio
 * RestTemplate/HttpClient dentro de cada metodo, por lo que no se puede
 * interceptar sin refactorizarlo para inyectar esas dependencias. Aqui solo
 * se cubre la parte que no implica red: la construccion del cliente a partir
 * de la configuracion.
 */
class ServiceNowClientTest {

    @Test
    void seConstruyeConLasCredencialesDeLaConfiguracion() {
        ServiceNowProperties properties = new ServiceNowProperties();
        Credentials credentials = new Credentials();
        credentials.setUser("usuario");
        credentials.setPassword("clave");
        properties.setCredentials(credentials);

        assertThatCode(() -> new ServiceNowClient(properties)).doesNotThrowAnyException();
    }
}
