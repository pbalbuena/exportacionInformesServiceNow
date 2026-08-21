package org.princast.oma.servicenowsync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.princast.oma.servicenowsync.processor.ServiceNowProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;

class ServiceNowSyncApplicationTests {

    @Test
    void elCommandLineRunnerDelegaEnElProcesadorConLosArgumentosRecibidos() throws Exception {
        ServiceNowProcessor processor = mock(ServiceNowProcessor.class);
        String[] args = { "2026-01-01 00:00:00", "2026-01-31 23:59:59" };

        CommandLineRunner runner = new ServiceNowSyncApplication().commandLineRunner(processor);
        runner.run(args);

        verify(processor).procesar(args);
    }

    @Test
    void configureRegistraLaClaseDeArranqueEnElBuilder() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        SpringApplicationBuilder resultado = new ServiceNowSyncApplication().configure(builder);

        assertThat(resultado).isSameAs(builder);
    }
}
