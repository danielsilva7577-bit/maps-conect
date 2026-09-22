/**
 * MAPS Connect · Minimalista
 * Transición de navegación (fade) y bienvenida monocroma del skin
 * "minimalista". Mantiene intactas las funciones y rutas reales de la
 * plataforma.
 */
(function iniciarMinimalista() {
    if (window.Minimalista) {
        window.Minimalista.activar?.();
        return;
    }

    const escapar = valor => String(valor ?? '').replace(/[&<>"']/g, caracter => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    })[caracter]);

    const nombrePaginaActual = () => {
        const nombre = window.location.pathname.split('/').pop();
        return (nombre || 'inicio.html').toLowerCase();
    };

    const Minimalista = {
        _navegacionEnlazada: false,
        _inicioPendiente: false,

        activo() {
            return document.documentElement.getAttribute('data-estilo') === 'minimalista';
        },

        activar() {
            if (!this.activo()) return;
            if (!document.body) {
                if (!this._inicioPendiente) {
                    this._inicioPendiente = true;
                    document.addEventListener('DOMContentLoaded', () => {
                        this._inicioPendiente = false;
                        this.activar();
                    }, { once: true });
                }
                return;
            }

            document.documentElement.classList.remove('minimalista-leaving');
            document.documentElement.classList.add('minimalista-enabled');
            this._crearElementos();
            this._enlazarNavegacion();
            requestAnimationFrame(() => {
                if (this.activo()) document.documentElement.classList.add('minimalista-ready');
            });
        },

        desactivar() {
            document.documentElement.classList.remove('minimalista-enabled', 'minimalista-ready', 'minimalista-leaving');
            document.querySelectorAll('.minimalista-page-transition, .minimalista-welcome-overlay')
                .forEach(elemento => elemento.remove());
        },

        _crearElementos() {
            if (!document.querySelector('.minimalista-page-transition')) {
                const transicion = document.createElement('div');
                transicion.className = 'minimalista-page-transition';
                transicion.id = 'minimalistaPageTransition';
                transicion.setAttribute('aria-hidden', 'true');
                document.body.prepend(transicion);
            }
        },

        _enlazarNavegacion() {
            if (this._navegacionEnlazada) return;
            this._navegacionEnlazada = true;

            document.addEventListener('click', evento => {
                if (!this.activo() || !(evento.target instanceof Element)) return;

                const enlace = evento.target.closest('#sidebar a[href], .sidebar-nav a[href]');
                if (!enlace || evento.defaultPrevented) return;
                if (evento.ctrlKey || evento.metaKey || evento.shiftKey || evento.altKey ||
                    (evento.button !== undefined && evento.button !== 0)) return;

                const href = enlace.getAttribute('href');
                if (!href || href === '#') return;

                let destino;
                try {
                    destino = new URL(href, window.location.href);
                } catch (e) {
                    return;
                }
                if (destino.origin !== window.location.origin || !destino.pathname.endsWith('.html')) return;

                const paginaDestino = (destino.pathname.split('/').pop() || '').toLowerCase();
                const paginaActual = nombrePaginaActual();
                if (paginaDestino === paginaActual && destino.search === window.location.search && destino.hash === window.location.hash) return;

                evento.preventDefault();
                this._cerrarMenu();

                const hoja = document.getElementById('minimalistaPageTransition');
                hoja?.classList.remove('active');
                requestAnimationFrame(() => hoja?.classList.add('active'));
                document.documentElement.classList.add('minimalista-leaving');
                window.setTimeout(() => { window.location.href = href; }, 360);
            }, true);
        },

        _cerrarMenu() {
            document.getElementById('sidebar')?.classList.remove('open');
            document.querySelector('.app-layout')?.classList.remove('menu-open');
            const alternador = document.getElementById('sidebar-toggle');
            alternador?.classList.remove('open');
            alternador?.setAttribute('aria-expanded', 'false');
        },

        mostrarBienvenida() {
            if (!this.activo() || !document.body) return false;

            let pendiente = false;
            try {
                pendiente = sessionStorage.getItem('bienvenida-pendiente') === '1' ||
                    localStorage.getItem('bienvenida-pendiente') === '1';
                sessionStorage.removeItem('bienvenida-pendiente');
                localStorage.removeItem('bienvenida-pendiente');
            } catch (e) {
                return false;
            }
            if (!pendiente || document.querySelector('.minimalista-welcome-overlay')) return false;

            const usuario = typeof Auth !== 'undefined' ? Auth.getUser?.() : null;
            const nombreCompleto = String(usuario?.nombre || 'Usuario').trim() || 'Usuario';
            const nombreCorto = nombreCompleto.split(/\s+/)[0] || 'Usuario';
            const iniciales = nombreCompleto.split(/\s+/).filter(Boolean).slice(0, 2)
                .map(parte => parte.charAt(0).toLocaleUpperCase('es-MX')).join('') || 'MC';
            const rol = {
                ESTUDIANTE: 'Estudiante',
                PROFESOR: 'Docente',
                ADMINISTRADOR: 'Administración'
            }[String(usuario?.rol || '').toUpperCase()] || 'Sesión';
            const identificador = usuario?.matricula || usuario?.numeroEmpleado;
            const estado = identificador ? identificador + ' · sesión activa' : rol + ' · sesión activa';

            const overlay = document.createElement('div');
            overlay.className = 'minimalista-welcome-overlay';
            overlay.id = 'welcomeOverlay';
            overlay.setAttribute('role', 'dialog');
            overlay.setAttribute('aria-modal', 'true');
            overlay.setAttribute('aria-label', 'Bienvenida a MAPS Connect');
            overlay.innerHTML = [
                '<div class="minimalista-welcome-card">',
                '<div class="minimalista-avatar" aria-hidden="true">' + escapar(iniciales) + '</div>',
                '<h1 class="minimalista-welcome-title">Bienvenido de vuelta, ' + escapar(nombreCorto) + '</h1>',
                '<span class="minimalista-welcome-status">' + escapar(estado) + '</span>',
                '<p class="minimalista-welcome-sub">Es tu espacio académico. Retoma tu ruta, tus mensajes y tu comunidad.</p>',
                '<button type="button" class="minimalista-btn-continue">Continuar</button>',
                '</div>'
            ].join('');
            document.body.appendChild(overlay);

            const cerrar = () => {
                overlay.classList.add('fade-out');
                window.setTimeout(() => overlay.remove(), 320);
            };
            overlay.querySelector('.minimalista-btn-continue').addEventListener('click', cerrar, { once: true });
            document.addEventListener('keydown', e => {
                if (e.key === 'Escape') cerrar();
            }, { once: true });
            return true;
        }
    };

    window.Minimalista = Minimalista;
    document.dispatchEvent(new CustomEvent('minimalista:listo'));
    Minimalista.activar();
})();