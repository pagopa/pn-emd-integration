package it.pagopa.pn.emd.integration;

import it.pagopa.pn.emd.integration.config.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PnEmdIntegrationApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PnEmdIntegrationApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        app.setWebApplicationType(WebApplicationType.REACTIVE);
        app.run(args);
    }
}
