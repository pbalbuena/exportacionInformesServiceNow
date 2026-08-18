package org.princast.oma.servicenowsync;

import org.princast.oma.servicenowsync.processor.ServiceNowProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServiceNowSyncApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ServiceNowSyncApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ServiceNowSyncApplication.class);
    }

    @Bean
    CommandLineRunner commandLineRunner(ServiceNowProcessor processor) {
        return args -> {
            processor.procesar(args);
        };
    }
}