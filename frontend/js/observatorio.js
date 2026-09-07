/**
 * MAPS Connect · Observatorio
 * Fondo cósmico, transición de navegación y bienvenida dinámica para el skin
 * observatorio. Conserva el contenido y las funciones de cada pantalla.
 */
(function iniciarObservatorio() {
    if (window.Observatorio) {
        window.Observatorio.activar?.();
        return;
    }

    const ZONAS = {
        'inicio.html': { x: 0, y: 20, scale: 0.62 },
        'perfil.html': { x: 1000, y: -80, scale: 1.15 },
        'certificados.html': { x: 650, y: -60, scale: 1.1 },
        'comunidad.html': { x: 300, y: -40, scale: 1.05 },
        'mensajes.html': { x: -300, y: -50, scale: 1.15 },
        'empresarial.html': { x: -650, y: -30, scale: 1.05 },
        'circulos.html': { x: -950, y: -50, scale: 1.1 },
        'ajustes.html': { x: -1200, y: -40, scale: 1.2 },
        'admin.html': { x: -640, y: 70, scale: 0.95 },
        'docente.html': { x: 180, y: 50, scale: 0.92 },
        'usuario.html': { x: 1000, y: -80, scale: 1.15 },
        'index.html': { x: 0, y: 20, scale: 0.62 },
        'registro.html': { x: 0, y: 20, scale: 0.62 },
        'onboarding.html': { x: 0, y: 20, scale: 0.62 }
    };

    const escapear = valor => String(valor ?? '').replace(/[&<>"']/g, caracter => ({
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

    const Observatorio = {
        _navegacionEnlazada: false,
        _inicioPendiente: false,

        activo() {
            return document.documentElement.getAttribute('data-estilo') === 'observatorio';
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

            document.documentElement.classList.remove('observatory-leaving');
            document.documentElement.classList.add('observatory-enabled');
            this._crearEscena();
            this._aplicarZonaGuardada();
            this._enlazarNavegacion();
            requestAnimationFrame(() => {
                if (this.activo()) document.documentElement.classList.add('observatory-ready');
            });
        },

        desactivar() {
            document.documentElement.classList.remove('observatory-enabled', 'observatory-ready', 'observatory-leaving');
            document.querySelectorAll('.observatory-scene, .observatory-comet-transition, .observatory-welcome-overlay')
                .forEach(elemento => elemento.remove());
        },

        _crearEscena() {
            if (!document.querySelector('.observatory-scene')) {
                const escena = document.createElement('div');
                escena.className = 'observatory-scene';
                escena.setAttribute('aria-hidden', 'true');
                escena.innerHTML = this._marcadoEscena();
                document.body.prepend(escena);
            }

            if (!document.querySelector('.observatory-comet-transition')) {
                const transicion = document.createElement('div');
                transicion.className = 'observatory-comet-transition';
                transicion.id = 'observatoryCometTransition';
                transicion.setAttribute('aria-hidden', 'true');
                document.body.prepend(transicion);
            }
        },

        _marcadoEscena() {
            return [
                '<div class="observatory-cosmos">',
                '<svg class="observatory-cosmos-layer" id="observatoryCosmosLayer" viewBox="0 0 2600 1100" xmlns="http://www.w3.org/2000/svg">',
                '<defs>',
                '<radialGradient id="obsNebulaBlue" cx="50%" cy="50%" r="50%"><stop offset="0%" stop-color="#2C4870" stop-opacity=".38"/><stop offset="100%" stop-color="#2C4870" stop-opacity="0"/></radialGradient>',
                '<radialGradient id="obsNebulaViolet" cx="50%" cy="50%" r="50%"><stop offset="0%" stop-color="#5B4590" stop-opacity=".43"/><stop offset="100%" stop-color="#5B4590" stop-opacity="0"/></radialGradient>',
                '<radialGradient id="obsNebulaDark" cx="50%" cy="50%" r="50%"><stop offset="0%" stop-color="#2E1F4D" stop-opacity=".56"/><stop offset="100%" stop-color="#2E1F4D" stop-opacity="0"/></radialGradient>',
                '<linearGradient id="obsSkyGradient" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#08070F"/><stop offset="100%" stop-color="#020207"/></linearGradient>',
                '<radialGradient id="obsPlanetGradient" cx="35%" cy="35%" r="65%"><stop offset="0%" stop-color="#9B7FD1"/><stop offset="100%" stop-color="#3A2A5C"/></radialGradient>',
                '</defs>',
                '<rect width="2600" height="1100" fill="url(#obsSkyGradient)"/>',
                '<ellipse cx="300" cy="350" rx="260" ry="180" fill="url(#obsNebulaBlue)"/><ellipse cx="420" cy="420" rx="200" ry="160" fill="url(#obsNebulaDark)"/>',
                '<ellipse cx="1300" cy="300" rx="340" ry="220" fill="url(#obsNebulaViolet)"/><ellipse cx="1450" cy="260" rx="220" ry="180" fill="url(#obsNebulaBlue)"/>',
                '<ellipse cx="2100" cy="380" rx="280" ry="190" fill="url(#obsNebulaDark)"/><ellipse cx="2000" cy="420" rx="200" ry="150" fill="url(#obsNebulaViolet)"/>',
                '<ellipse cx="700" cy="700" rx="220" ry="150" fill="url(#obsNebulaViolet)"/><ellipse cx="1900" cy="650" rx="240" ry="160" fill="url(#obsNebulaDark)"/><ellipse cx="1000" cy="800" rx="260" ry="170" fill="url(#obsNebulaBlue)"/>',
                '<g transform="translate(2480,220)" opacity=".9"><ellipse cx="0" cy="0" rx="70" ry="18" fill="none" stroke="#8B6BC7" stroke-width="3" opacity=".5" transform="rotate(-18)"/><circle cx="0" cy="0" r="34" fill="url(#obsPlanetGradient)"/><ellipse cx="0" cy="0" rx="70" ry="18" fill="none" stroke="#C9B8E8" stroke-width="1.5" opacity=".6" transform="rotate(-18)"/></g>',
                '<g fill="#C9D6E8">',
                '<circle cx="80" cy="120" r="1.6" class="obs-twinkle-a"/><circle cx="220" cy="60" r="1.2" class="obs-twinkle-b"/><circle cx="400" cy="150" r="1.8" class="obs-twinkle-c"/><circle cx="560" cy="80" r="1.3" class="obs-twinkle-a"/><circle cx="780" cy="180" r="1.5" class="obs-twinkle-b"/><circle cx="950" cy="70" r="1.2" class="obs-twinkle-c"/>',
                '<circle cx="1150" cy="140" r="1.7" class="obs-twinkle-a"/><circle cx="1350" cy="60" r="1.3" class="obs-twinkle-b"/><circle cx="1550" cy="160" r="1.6" class="obs-twinkle-c"/><circle cx="1750" cy="90" r="1.4" class="obs-twinkle-a"/><circle cx="1950" cy="170" r="1.8" class="obs-twinkle-b"/><circle cx="2150" cy="70" r="1.2" class="obs-twinkle-c"/><circle cx="2350" cy="150" r="1.6" class="obs-twinkle-a"/>',
                '<circle cx="150" cy="850" r="1.4" class="obs-twinkle-c"/><circle cx="500" cy="920" r="1.6" class="obs-twinkle-a"/><circle cx="900" cy="870" r="1.3" class="obs-twinkle-b"/><circle cx="1300" cy="940" r="1.7" class="obs-twinkle-c"/><circle cx="1700" cy="880" r="1.4" class="obs-twinkle-a"/><circle cx="2100" cy="930" r="1.6" class="obs-twinkle-b"/><circle cx="2450" cy="860" r="1.3" class="obs-twinkle-c"/>',
                '<circle cx="40" cy="300" r="1.1" class="obs-twinkle-b"/><circle cx="300" cy="250" r="1.3" class="obs-twinkle-c"/><circle cx="650" cy="220" r="1" class="obs-twinkle-a"/><circle cx="1050" cy="180" r="1.4" class="obs-twinkle-b"/><circle cx="1500" cy="230" r="1.2" class="obs-twinkle-c"/><circle cx="1850" cy="200" r="1.1" class="obs-twinkle-a"/><circle cx="2250" cy="250" r="1.3" class="obs-twinkle-b"/>',
                '</g>',
                '<g id="obs-zone-perfil"><path d="M240,420 L300,360 L370,400 L430,340 L380,460 L300,440 Z" stroke="#7FA0C4" stroke-width="1.5" fill="none" opacity=".7"/><g fill="#E4ECF7"><circle cx="240" cy="420" r="4" class="obs-twinkle-a"/><circle cx="300" cy="360" r="5" class="obs-twinkle-b"/><circle cx="370" cy="400" r="4" class="obs-twinkle-c"/><circle cx="430" cy="340" r="5" class="obs-twinkle-a"/><circle cx="380" cy="460" r="4" class="obs-twinkle-b"/><circle cx="300" cy="440" r="4" class="obs-twinkle-c"/></g></g>',
                '<g id="obs-zone-ruta"><path d="M650,300 L650,460 M570,380 L730,380 M600,340 L700,420 M700,340 L600,420" stroke="#7FA0C4" stroke-width="1.5" fill="none" opacity=".7"/><g fill="#E4ECF7"><circle cx="650" cy="300" r="5" class="obs-twinkle-a"/><circle cx="650" cy="460" r="5" class="obs-twinkle-b"/><circle cx="570" cy="380" r="4" class="obs-twinkle-c"/><circle cx="730" cy="380" r="4" class="obs-twinkle-a"/><circle cx="650" cy="380" r="6" class="obs-twinkle-b"/></g></g>',
                '<g id="obs-zone-comunidad"><path d="M960,420 L1040,380 L1120,430 L1200,390 L1280,440" stroke="#7FA0C4" stroke-width="1.3" fill="none" opacity=".66"/><g fill="#E4ECF7"><circle cx="960" cy="420" r="4" class="obs-twinkle-a"/><circle cx="1040" cy="380" r="5" class="obs-twinkle-b"/><circle cx="1120" cy="430" r="4" class="obs-twinkle-c"/><circle cx="1200" cy="390" r="5" class="obs-twinkle-a"/><circle cx="1280" cy="440" r="4" class="obs-twinkle-b"/><circle cx="1000" cy="480" r="3" class="obs-twinkle-c"/><circle cx="1160" cy="490" r="3" class="obs-twinkle-a"/></g></g>',
                '<g id="obs-zone-inicio"><path d="M1150,260 L1240,200 L1340,230 L1420,190 L1470,270 L1400,330 L1300,320 L1220,360 Z" stroke="#A9C2DE" stroke-width="2" fill="none" opacity=".82"/><g fill="#F2F6FC"><circle cx="1150" cy="260" r="6" class="obs-twinkle-a"/><circle cx="1240" cy="200" r="7" class="obs-twinkle-b"/><circle cx="1340" cy="230" r="6" class="obs-twinkle-c"/><circle cx="1420" cy="190" r="8" class="obs-twinkle-a"/><circle cx="1470" cy="270" r="6" class="obs-twinkle-b"/><circle cx="1400" cy="330" r="6" class="obs-twinkle-c"/><circle cx="1300" cy="320" r="5" class="obs-twinkle-a"/><circle cx="1220" cy="360" r="5" class="obs-twinkle-b"/></g></g>',
                '<g id="obs-zone-mensajes"><path d="M1650,300 L1850,420" stroke="#E4ECF7" stroke-width="2" opacity=".5" stroke-dasharray="2,6"/><circle cx="1850" cy="420" r="6" fill="#F2F6FC" class="obs-twinkle-a"/><path d="M1730,470 L1790,440 L1850,480 L1910,450" stroke="#7FA0C4" stroke-width="1.4" fill="none" opacity=".65"/><g fill="#E4ECF7"><circle cx="1730" cy="470" r="4" class="obs-twinkle-b"/><circle cx="1790" cy="440" r="4" class="obs-twinkle-c"/><circle cx="1850" cy="480" r="5" class="obs-twinkle-a"/><circle cx="1910" cy="450" r="4" class="obs-twinkle-b"/></g></g>',
                '<g id="obs-zone-empresarial"><path d="M2000,400 L2080,350 L2160,390 L2240,340 L2320,400 L2240,440 L2160,430 L2080,450 Z" stroke="#7FA0C4" stroke-width="1.4" fill="none" opacity=".65"/><g fill="#E4ECF7"><circle cx="2000" cy="400" r="4" class="obs-twinkle-a"/><circle cx="2080" cy="350" r="5" class="obs-twinkle-b"/><circle cx="2160" cy="390" r="4" class="obs-twinkle-c"/><circle cx="2240" cy="340" r="5" class="obs-twinkle-a"/><circle cx="2320" cy="400" r="4" class="obs-twinkle-b"/><circle cx="2240" cy="440" r="4" class="obs-twinkle-c"/><circle cx="2160" cy="430" r="4" class="obs-twinkle-a"/><circle cx="2080" cy="450" r="4" class="obs-twinkle-b"/></g></g>',
                '<g id="obs-zone-circulos"><circle cx="2390" cy="430" r="90" stroke="#7FA0C4" stroke-width="1.4" fill="none" opacity=".6"/><g fill="#E4ECF7"><circle cx="2390" cy="340" r="5" class="obs-twinkle-a"/><circle cx="2470" cy="390" r="4" class="obs-twinkle-b"/><circle cx="2470" cy="470" r="4" class="obs-twinkle-c"/><circle cx="2390" cy="520" r="5" class="obs-twinkle-a"/><circle cx="2310" cy="470" r="4" class="obs-twinkle-b"/><circle cx="2310" cy="390" r="4" class="obs-twinkle-c"/><circle cx="2390" cy="430" r="6" class="obs-twinkle-b"/></g></g>',
                '<g id="obs-zone-ajustes"><circle cx="2520" cy="380" r="9" fill="#F2F6FC" class="obs-twinkle-a"/><path d="M2520,350 L2520,410 M2490,380 L2550,380" stroke="#E4ECF7" stroke-width="1" opacity=".5"/><circle cx="2470" cy="440" r="3" fill="#E4ECF7" class="obs-twinkle-b"/><circle cx="2570" cy="440" r="3" fill="#E4ECF7" class="obs-twinkle-c"/></g>',
                '</svg></div>',
                '<div class="observatory-vignette"></div>',
                '<div class="observatory-shooting-stars"><span class="observatory-shooting-star s1"></span><span class="observatory-shooting-star s2"></span><span class="observatory-shooting-star s3"></span><span class="observatory-shooting-star s4"></span><span class="observatory-shooting-star s5"></span></div>'
            ].join('');
        },

        _aplicarZonaGuardada() {
            let destino = nombrePaginaActual();
            try {
                destino = sessionStorage.getItem('observatory-zone') || destino;
                sessionStorage.removeItem('observatory-zone');
            } catch (e) {
                // El fondo sigue funcionando aun si el almacenamiento está bloqueado.
            }
            this.panearHacia(destino);
        },

        panearHacia(pagina) {
            const capa = document.getElementById('observatoryCosmosLayer');
            if (!capa) return;
            const zona = ZONAS[pagina] || ZONAS['inicio.html'];
            capa.style.transform = 'translate(' + zona.x + 'px, ' + zona.y + 'px) scale(' + zona.scale + ')';
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
                this.panearHacia(paginaDestino);
                this._cerrarMenu();
                try { sessionStorage.setItem('observatory-zone', paginaDestino); } catch (e) {}

                const estela = document.getElementById('observatoryCometTransition');
                estela?.classList.remove('active');
                requestAnimationFrame(() => estela?.classList.add('active'));
                document.documentElement.classList.add('observatory-leaving');
                window.setTimeout(() => { window.location.href = href; }, 390);
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
            if (!pendiente || document.querySelector('.observatory-welcome-overlay')) return false;

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
            overlay.className = 'observatory-welcome-overlay';
            overlay.id = 'welcomeOverlay';
            overlay.setAttribute('role', 'dialog');
            overlay.setAttribute('aria-modal', 'true');
            overlay.setAttribute('aria-label', 'Bienvenida a MAPS Connect');
            overlay.innerHTML = [
                '<div class="observatory-welcome-card">',
                '<span class="observatory-burst-ring r1"></span><span class="observatory-burst-ring r2"></span><span class="observatory-burst-ring r3"></span>',
                '<span class="observatory-burst-particle" style="--px:-90px;--py:-60px;--delay:.05s"></span><span class="observatory-burst-particle" style="--px:100px;--py:-40px;--delay:.1s"></span><span class="observatory-burst-particle" style="--px:-70px;--py:80px;--delay:.02s"></span><span class="observatory-burst-particle" style="--px:110px;--py:70px;--delay:.15s"></span>',
                '<span class="observatory-burst-particle" style="--px:0;--py:-110px;--delay:.08s"></span><span class="observatory-burst-particle" style="--px:-120px;--py:10px;--delay:.12s"></span><span class="observatory-burst-particle" style="--px:130px;--py:-10px;--delay:.03s"></span><span class="observatory-burst-particle" style="--px:20px;--py:120px;--delay:.18s"></span>',
                '<span class="observatory-corner-mark tl">✦</span><span class="observatory-corner-mark tr">✦</span><span class="observatory-corner-mark bl">✦</span><span class="observatory-corner-mark br">✦</span>',
                '<p class="observatory-modal-eyebrow">Una nueva estrella se enciende</p>',
                '<h1 class="observatory-modal-title">El cosmos te da la bienvenida, ' + escapear(nombreCorto) + '</h1>',
                '<div class="observatory-star-avatar" aria-hidden="true">' + escapear(iniciales) + '</div>',
                '<div class="observatory-modal-user-name">' + escapear(nombreCompleto) + '</div>',
                '<div class="observatory-modal-status">' + escapear(estado) + '</div></div>'
            ].join('');
            document.body.appendChild(overlay);

            window.setTimeout(() => {
                overlay.classList.add('fade-out');
                window.setTimeout(() => overlay.remove(), 760);
            }, 1600);
            return true;
        }
    };

    window.Observatorio = Observatorio;
    document.dispatchEvent(new CustomEvent('observatorio:listo'));
    Observatorio.activar();
})();
