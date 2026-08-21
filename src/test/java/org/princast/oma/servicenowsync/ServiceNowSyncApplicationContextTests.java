package org.princast.oma.servicenowsync;

import org.junit.jupiter.api.Test;
import org.princast.oma.servicenowsync.processor.ServiceNowProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Arranca el contexto completo de Spring para comprobar que todos los beans y
 * la configuracion (incluido el scheduling) se conectan correctamente. Se
 * mockea el ServiceNowProcessor para que el CommandLineRunner no llegue a
 * llamar de verdad a ServiceNow ni a Oracle durante el arranque del contexto.
 */
@SpringBootTest
class ServiceNowSyncApplicationContextTests {

    @MockBean
    private ServiceNowProcessor serviceNowProcessor;

    @Test
    void elContextoDeSpringArrancaCorrectamente() {
    }
}
