/**
 * MAPS Connect - Modo oscuro / claro
 * Guarda la preferencia en localStorage y aplica la clase "dark-mode"
 * sobre el elemento <html>. Cualquier página puede leer/alternar usando Tema.
 */
const Tema = {
    KEY: 'tema-maps',
    OSCURO: 'oscuro',

    activo() {
        return document.documentElement.classList.contains('dark-mode');
    },

    /** Aplica el tema guardado al <html>. Devuelve true si quedó oscuro.
     *  Por defecto el tema es claro; el modo oscuro solo se activa
     *  cuando el usuario lo elige explícitamente. */
    aplicar() {
        const guardado = localStorage.getItem(this.KEY);
        const oscuro = guardado === this.OSCURO;
        this.set(oscuro);
        return oscuro;
    },

    set(oscuro) {
        document.documentElement.classList.toggle('dark-mode', oscuro);
        const icono = document.documentElement.querySelector('[data-tema-icono]');
        if (icono) icono.textContent = oscuro ? '🌙' : '☀️';
    },

    /** Alterna y guarda. Devuelve el nuevo estado (true = oscuro). */
    alternar() {
        const nuevo = !this.activo();
        this.set(nuevo);
        try {
            localStorage.setItem(this.KEY, nuevo ? this.OSCURO : 'claro');
        } catch (e) {
            // almacenamiento no disponible; solo se aplica en esta sesión.
        }
        return nuevo;
    }
};

// Se aplica en cuanto se carga el script (evita parpadeo al pintar).
(globalThis.__MAPS_TEMA_APLICADO__ || (() => { Tema.aplicar(); globalThis.__MAPS_TEMA_APLICADO__ = true; }))();
