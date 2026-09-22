package com.tecmilenio.mapsconect.config;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * Redirige cualquier petición entrante que no empiece con /api hacia /api/...
 * Evita errores 404 si el usuario o dispositivo accede a la raíz del dominio o del túnel.
 */
@Configuration
public class RootRedirectConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatRootRedirectCustomizer() {
        return factory -> factory.addEngineValves(new ValveBase() {
            @Override
            public void invoke(Request request, Response response) throws IOException, ServletException {
                String uri = request.getRequestURI();
                if (uri != null && !uri.startsWith("/api")) {
                    String target = "/api" + (uri.equals("/") ? "/index.html" : uri);
                    if (request.getQueryString() != null) {
                        target += "?" + request.getQueryString();
                    }
                    response.sendRedirect(target);
                    return;
                }
                getNext().invoke(request, response);
            }
        });
    }
}
