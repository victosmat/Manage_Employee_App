package com.example.demo.customizer;

import org.springframework.boot.web.embedded.jetty.ConfigurableJettyWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

public class ProdCustomizer implements WebServerFactoryCustomizer<ConfigurableJettyWebServerFactory> {
    @Override
    public void customize(ConfigurableJettyWebServerFactory factory) {
        factory.setPort(5000);
    }
}
