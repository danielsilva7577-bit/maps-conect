package com.tecmilenio.mapsconect.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de páginas estáticas.
 *
 * <p>Redirige la raíz ({@code /}) a {@code /index.html}, que es el punto
 * de entrada del frontend SPA (Single Page Application). Al usar
 * {@link Controller} (no {@code @RestController}) en lugar de devolver JSON,
 * Spring resuelve la vista estática configurada en
 * {@code StaticResourceConfig}.</p>
 */
@Controller
public class PageController {

    /**
     * Redirige la raíz de la aplicación al HTML principal del frontend.
     *
     * @return instrucción de redirección a {@code /index.html}
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }
}


