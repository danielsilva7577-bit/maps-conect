/**
 * MAPS Connect - Personalización del color de la página
 * Permite elegir un color de acento (preestablecido o a medida) que se
 * aplica en toda la aplicación (sidebar, topbar, botones, enlaces,
 * tarjetas, chat...). Guarda la preferencia en localStorage y la aplica
 * como atributo "data-color" + variables --accent-* sobre <html>;
 * frontend/css/colores.css se encarga de mapearlas a las variables
 * existentes del tema (claro y oscuro).
 */
const Colores = {
    KEY: 'color-maps',

    PRESETS: [
        { id: 'verde', hex: '#087527', nombre: 'Verde' },
        { id: 'rosa', hex: '#e1367f', nombre: 'Rosa' },
        { id: 'azul', hex: '#0a6ab3', nombre: 'Azul' },
        { id: 'morado', hex: '#7c3aed', nombre: 'Morado' },
        { id: 'naranja', hex: '#ea580c', nombre: 'Naranja' },
        { id: 'teal', hex: '#0d9488', nombre: 'Teal' }
    ],

    guardado() {
        try { return localStorage.getItem(this.KEY); } catch (e) { return null; }
    },

    limpiar() {
        const r = document.documentElement;
        r.removeAttribute('data-color');
        const vars = ['--accent', '--accent-dark', '--accent-mid', '--accent-bright',
            '--accent-deep', '--accent-soft', '--accent-soft-border', '--accent-contrast'];
        for (const k of vars) r.style.removeProperty(k);
        try { localStorage.removeItem(this.KEY); } catch (e) { /* almacenamiento no disponible */ }
    },

    /* --- Utilidades de color (HSL) --- */
    _alHsl(hex) {
        hex = hex.replace('#', '');
        const r = parseInt(hex.slice(0, 2), 16) / 255;
        const g = parseInt(hex.slice(2, 4), 16) / 255;
        const b = parseInt(hex.slice(4, 6), 16) / 255;
        const max = Math.max(r, g, b), min = Math.min(r, g, b);
        let h = 0, s = 0;
        const l = (max + min) / 2;
        if (max !== min) {
            const d = max - min;
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
            switch (max) {
                case r: h = (g - b) / d + (g < b ? 6 : 0); break;
                case g: h = (b - r) / d + 2; break;
                default: h = (r - g) / d + 4;
            }
            h *= 60;
        }
        return { h, s, l };
    },

    _aHex({ h, s, l }) {
        h = ((h % 360) + 360) % 360;
        const c = (1 - Math.abs(2 * l - 1)) * s;
        const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
        const m = l - c / 2;
        let r = 0, g = 0, b = 0;
        if (h < 60) { r = c; g = x; }
        else if (h < 120) { r = x; g = c; }
        else if (h < 180) { g = c; b = x; }
        else if (h < 240) { g = x; b = c; }
        else if (h < 300) { r = x; b = c; }
        else { r = c; b = x; }
        const to = (v) => Math.round((v + m) * 255).toString(16).padStart(2, '0');
        return '#' + to(r) + to(g) + to(b);
    },

    _conLuz({ h, s, l }, nl) { return this._aHex({ h, s, l: nl }); },

    /* --- Aplicación --- */

    /** Aplica el color guardado; si no hay, restaura el color por defecto. */
    aplicar() {
        const hex = this.guardado();
        if (!hex) { this.limpiar(); return; }
        this.set(hex, false);
    },

    /** Aplica un color de acento y (por defecto) lo guarda como preferencia.
     *  Calcula toda la paleta derivada (oscuros, claros, tintes) y la aplica
     *  como variables CSS, de modo que toda la página se "sintoniza". */
    set(hex, guardar = true) {
        if (!/^#[0-9a-f]{6}$/i.test(hex)) return;
        const base = this._alHsl(hex);
        const clampL = (l) => Math.min(Math.max(l, 0), 1);
        const accentDark = clampL(Math.max(base.l * 0.62, 0.3));
        const midL = clampL(Math.max(Math.min(base.l + 0.14, 0.52), 0.38));
        const brightL = clampL(Math.max(Math.min(base.l + 0.3, 0.68), 0.5));
        const deepL = clampL(Math.max(Math.min(base.l * 0.55, 0.32), 0.16));
        const vars = {
            '--accent': hex,
            '--accent-soft': this._conLuz(base, 0.93),
            '--accent-soft-border': this._conLuz(base, 0.8),
            '--accent-dark': this._conLuz(base, accentDark),
            '--accent-mid': this._conLuz(base, midL),
            '--accent-bright': this._conLuz(base, brightL),
            '--accent-deep': this._conLuz(base, deepL),
            '--accent-contrast': base.l > 0.62 ? '#1f2937' : '#ffffff'
        };
        const r = document.documentElement;
        r.setAttribute('data-color', 'personalizado');
        for (const k in vars) r.style.setProperty(k, vars[k]);
        if (guardar) {
            try { localStorage.setItem(this.KEY, hex); } catch (e) { /* almacenamiento no disponible */ }
        }
    }
};

/* Se aplica en cuanto se carga el script, igual que Tema (evita parpadeo). */
Colores.aplicar();