/**
 * MAPS Connect · Biblioteca Gótica
 * Fondo de estanterías, transición de página y bienvenida dinámica.
 * Mantiene intactas las funciones y rutas reales de la plataforma.
 */
(function iniciarBiblioteca() {
    if (window.Biblioteca) {
        window.Biblioteca.activar?.();
        return;
    }

    const ZONAS = {
        'inicio.html': { x: 0, y: 20, scale: 0.62 },
        'perfil.html': { x: 1000, y: -85, scale: 1.15 },
        'certificados.html': { x: 650, y: -65, scale: 1.1 },
        'comunidad.html': { x: 300, y: -50, scale: 1.05 },
        'mensajes.html': { x: -300, y: -60, scale: 1.15 },
        'empresarial.html': { x: -650, y: -45, scale: 1.05 },
        'circulos.html': { x: -950, y: -65, scale: 1.1 },
        'ajustes.html': { x: -1200, y: -55, scale: 1.2 },
        'admin.html': { x: -640, y: 45, scale: 0.96 },
        'docente.html': { x: 180, y: 42, scale: 0.94 },
        'usuario.html': { x: 1000, y: -85, scale: 1.15 },
        'index.html': { x: 0, y: 20, scale: 0.62 },
        'registro.html': { x: 0, y: 20, scale: 0.62 },
        'onboarding.html': { x: 0, y: 20, scale: 0.62 }
    };

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

    const Biblioteca = {
        _navegacionEnlazada: false,
        _inicioPendiente: false,

        activo() {
            return document.documentElement.getAttribute('data-estilo') === 'biblioteca';
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

            document.documentElement.classList.remove('biblioteca-leaving');
            document.documentElement.classList.add('biblioteca-enabled');
            this._crearEscena();
            this._aplicarZonaGuardada();
            this._enlazarNavegacion();
            requestAnimationFrame(() => {
                if (this.activo()) document.documentElement.classList.add('biblioteca-ready');
            });
        },

        desactivar() {
            document.documentElement.classList.remove('biblioteca-enabled', 'biblioteca-ready', 'biblioteca-leaving');
            document.querySelectorAll('.biblioteca-scene, .biblioteca-page-transition, .biblioteca-welcome-overlay')
                .forEach(elemento => elemento.remove());
        },

        _crearEscena() {
            if (!document.querySelector('.biblioteca-scene')) {
                const escena = document.createElement('div');
                escena.className = 'biblioteca-scene';
                escena.setAttribute('aria-hidden', 'true');
                escena.innerHTML = this._marcadoEscena();
                document.body.prepend(escena);
            }

            if (!document.querySelector('.biblioteca-page-transition')) {
                const transicion = document.createElement('div');
                transicion.className = 'biblioteca-page-transition';
                transicion.id = 'bibliotecaPageTransition';
                transicion.setAttribute('aria-hidden', 'true');
                document.body.prepend(transicion);
            }
        },

        _marcadoEscena() {
            return [
                '<div class="biblioteca-library">',
                '<svg class="biblioteca-library-layer" id="bibliotecaLibraryLayer" viewBox="0 0 2600 1100" xmlns="http://www.w3.org/2000/svg">',
                '<defs>',
                '<linearGradient id="bibSkyGradient" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#170a0d"/><stop offset="62%" stop-color="#100608"/><stop offset="100%" stop-color="#0a0404"/></linearGradient>',
                '<linearGradient id="bibWoodGradient" x1="0" y1="1" x2="0" y2="0"><stop offset="0%" stop-color="#160b08"/><stop offset="100%" stop-color="#3a2515"/></linearGradient>',
                '<radialGradient id="bibCandleGlow" cx="50%" cy="50%" r="50%"><stop offset="0%" stop-color="#d97996" stop-opacity=".52"/><stop offset="100%" stop-color="#8c1f3d" stop-opacity="0"/></radialGradient>',
                '<radialGradient id="bibWineGlow" cx="50%" cy="50%" r="50%"><stop offset="0%" stop-color="#6b1b30" stop-opacity=".48"/><stop offset="100%" stop-color="#6b1b30" stop-opacity="0"/></radialGradient>',
                '</defs>',
                '<rect width="2600" height="1100" fill="url(#bibSkyGradient)"/>',
                '<ellipse cx="280" cy="400" rx="280" ry="210" fill="url(#bibWineGlow)"/><ellipse cx="1300" cy="350" rx="460" ry="300" fill="url(#bibCandleGlow)"/><ellipse cx="2140" cy="430" rx="300" ry="220" fill="url(#bibWineGlow)"/>',
                '<path d="M0,540 H2600 V1000 H0 Z" fill="url(#bibWoodGradient)" opacity=".92"/>',
                '<g fill="#160c08" opacity=".76"><rect x="0" y="570" width="2600" height="12"/><rect x="0" y="684" width="2600" height="12"/><rect x="0" y="798" width="2600" height="12"/><rect x="0" y="930" width="2600" height="80"/></g>',
                '<g stroke="#4a0c1e" stroke-width="7" opacity=".72"><path d="M0,560 V930 M360,560 V930 M720,560 V930 M1080,560 V930 M1440,560 V930 M1800,560 V930 M2160,560 V930 M2520,560 V930"/></g>',
                '<g class="bib-book-row" fill="#6b1b30"><rect x="20" y="590" width="14" height="86"/><rect x="46" y="588" width="11" height="88"/><rect x="70" y="592" width="18" height="84"/><rect x="102" y="590" width="10" height="86"/><rect x="128" y="588" width="16" height="88"/><rect x="164" y="592" width="12" height="84"/><rect x="202" y="590" width="20" height="86"/><rect x="246" y="588" width="11" height="88"/><rect x="282" y="592" width="15" height="84"/><rect x="396" y="590" width="16" height="86"/><rect x="428" y="588" width="10" height="88"/><rect x="454" y="592" width="18" height="84"/><rect x="492" y="590" width="13" height="86"/><rect x="530" y="588" width="17" height="88"/><rect x="566" y="592" width="10" height="84"/><rect x="748" y="590" width="15" height="86"/><rect x="778" y="588" width="18" height="88"/><rect x="812" y="592" width="11" height="84"/><rect x="850" y="590" width="20" height="86"/><rect x="894" y="588" width="13" height="88"/><rect x="926" y="592" width="16" height="84"/><rect x="1088" y="590" width="18" height="86"/><rect x="1120" y="588" width="12" height="88"/><rect x="1150" y="592" width="20" height="84"/><rect x="1460" y="590" width="16" height="86"/><rect x="1492" y="588" width="13" height="88"/><rect x="1522" y="592" width="18" height="84"/><rect x="1812" y="590" width="20" height="86"/><rect x="1850" y="588" width="11" height="88"/><rect x="1880" y="592" width="15" height="84"/><rect x="1920" y="590" width="18" height="86"/><rect x="1960" y="588" width="10" height="88"/><rect x="1990" y="592" width="20" height="84"/><rect x="2180" y="590" width="16" height="86"/><rect x="2210" y="588" width="18" height="88"/><rect x="2245" y="592" width="12" height="84"/><rect x="2290" y="590" width="20" height="86"/><rect x="2330" y="588" width="13" height="88"/><rect x="2370" y="592" width="16" height="84"/><rect x="2440" y="590" width="20" height="86"/><rect x="2480" y="588" width="12" height="88"/><rect x="2512" y="592" width="18" height="84"/></g>',
                '<g fill="#8c1f3d" opacity=".72"><rect x="90" y="704" width="12" height="82"/><rect x="116" y="702" width="18" height="84"/><rect x="148" y="706" width="10" height="80"/><rect x="430" y="704" width="16" height="82"/><rect x="460" y="702" width="12" height="84"/><rect x="488" y="706" width="19" height="80"/><rect x="800" y="704" width="14" height="82"/><rect x="832" y="702" width="19" height="84"/><rect x="868" y="706" width="11" height="80"/><rect x="1540" y="704" width="18" height="82"/><rect x="1574" y="702" width="12" height="84"/><rect x="1604" y="706" width="16" height="80"/><rect x="2020" y="704" width="16" height="82"/><rect x="2050" y="702" width="11" height="84"/><rect x="2080" y="706" width="19" height="80"/><rect x="2340" y="704" width="14" height="82"/><rect x="2370" y="702" width="19" height="84"/><rect x="2404" y="706" width="11" height="80"/></g>',
                '<g fill="#b32a4c" opacity=".82"><circle cx="120" cy="250" r="2.1" class="bib-twinkle-a"/><circle cx="330" cy="180" r="1.5" class="bib-twinkle-b"/><circle cx="560" cy="280" r="2" class="bib-twinkle-c"/><circle cx="820" cy="200" r="1.7" class="bib-twinkle-a"/><circle cx="1050" cy="250" r="1.9" class="bib-twinkle-b"/><circle cx="1300" cy="180" r="1.6" class="bib-twinkle-c"/><circle cx="1510" cy="240" r="2.1" class="bib-twinkle-a"/><circle cx="1740" cy="210" r="1.6" class="bib-twinkle-b"/><circle cx="1980" cy="270" r="2" class="bib-twinkle-c"/><circle cx="2200" cy="190" r="1.5" class="bib-twinkle-a"/><circle cx="2440" cy="260" r="2" class="bib-twinkle-b"/></g>',
                '<g id="bib-zone-perfil"><rect x="220" y="700" width="138" height="14" fill="#2b1810"/><rect x="230" y="714" width="10" height="86" fill="#160c08"/><rect x="338" y="714" width="10" height="86" fill="#160c08"/><circle cx="270" cy="678" r="54" fill="url(#bibWineGlow)"/><rect x="262" y="636" width="12" height="48" fill="#2b1810"/><path d="M263,636 Q268,610 273,636 Z" fill="#d97996" class="bib-twinkle-b"/><path d="M298,670 L334,645 L346,681 Z" fill="#ebd9dc" opacity=".78"/></g>',
                '<g id="bib-zone-ruta"><circle cx="650" cy="680" r="62" fill="#2b1810" stroke="#8c1f3d" stroke-width="3"/><ellipse cx="650" cy="680" rx="62" ry="21" fill="none" stroke="#b32a4c" stroke-width="1.5" opacity=".58"/><path d="M590,680 Q650,642 710,680 M650,618 V742" stroke="#b32a4c" stroke-width="1.5" fill="none" opacity=".5"/><rect x="643" y="742" width="14" height="76" fill="#160c08"/><path d="M760,620 L804,642 L804,720 L760,700 Z" fill="#6b1b30"/></g>',
                '<g id="bib-zone-comunidad"><rect x="960" y="574" width="12" height="246" fill="#160c08"/><rect x="1280" y="574" width="12" height="246" fill="#160c08"/><path d="M960,650 H1292 M960,735 H1292" stroke="#160c08" stroke-width="9"/><rect x="1002" y="588" width="16" height="54" fill="#8c1f3d"/><rect x="1030" y="590" width="12" height="52" fill="#6b1b30"/><rect x="1060" y="588" width="20" height="54" fill="#4a0c1e"/><rect x="1130" y="674" width="16" height="54" fill="#8c1f3d"/><rect x="1160" y="676" width="12" height="52" fill="#6b1b30"/><rect x="1190" y="674" width="18" height="54" fill="#4a0c1e"/></g>',
                '<g id="bib-zone-inicio"><path d="M1120,910 V500 Q1300,310 1480,500 V910" fill="none" stroke="#8c1f3d" stroke-width="5" opacity=".62"/><path d="M1150,910 V525 Q1300,365 1450,525 V910" fill="none" stroke="#5c1428" stroke-width="3" opacity=".6"/><g transform="translate(1300,300)"><path d="M0,-72 V0" stroke="#6b1b30" stroke-width="4"/><ellipse cx="0" cy="0" rx="80" ry="14" fill="#2b1810" stroke="#b32a4c" stroke-width="2"/><circle cx="-56" cy="5" r="14" fill="url(#bibCandleGlow)"/><circle cx="0" cy="8" r="16" fill="url(#bibCandleGlow)"/><circle cx="56" cy="5" r="14" fill="url(#bibCandleGlow)"/><rect x="-58" y="-4" width="5" height="19" fill="#d97996" class="bib-twinkle-a"/><rect x="-2" y="-1" width="5" height="23" fill="#d97996" class="bib-twinkle-b"/><rect x="53" y="-4" width="5" height="19" fill="#d97996" class="bib-twinkle-c"/></g><g transform="translate(1300,835)"><path d="M-68,18 L0,-2 L68,18 L68,34 L0,15 L-68,34 Z" fill="#ebd9dc" opacity=".86"/><rect x="-15" y="24" width="30" height="65" fill="#2b1810"/><ellipse cx="0" cy="95" rx="250" ry="24" fill="#45142a" opacity=".58"/></g></g>',
                '<g id="bib-zone-mensajes"><rect x="1710" y="745" width="182" height="16" fill="#2b1810"/><rect x="1722" y="761" width="12" height="68" fill="#160c08"/><rect x="1868" y="761" width="12" height="68" fill="#160c08"/><path d="M1754,730 L1792,721 L1788,744 L1750,741 Z" fill="#ebd9dc" opacity=".84"/><circle cx="1820" cy="738" r="10" fill="#160c08"/><path d="M1814,734 L1842,704" stroke="#b32a4c" stroke-width="3"/></g>',
                '<g id="bib-zone-empresarial"><rect x="1970" y="622" width="120" height="214" fill="#241209"/><rect x="2110" y="600" width="145" height="236" fill="#2b1810"/><rect x="2275" y="622" width="120" height="214" fill="#241209"/><g fill="#6b1b30"><rect x="1986" y="646" width="88" height="14"/><rect x="1986" y="678" width="88" height="14"/><rect x="1986" y="710" width="88" height="14"/><rect x="2128" y="624" width="108" height="14"/><rect x="2128" y="656" width="108" height="14"/><rect x="2128" y="688" width="108" height="14"/></g><rect x="1988" y="900" width="390" height="12" fill="#8c1f3d" opacity=".42"/></g>',
                '<g id="bib-zone-circulos"><circle cx="2390" cy="700" r="104" fill="none" stroke="#5c1428" stroke-width="4" opacity=".7"/><circle cx="2390" cy="700" r="64" fill="#2b1810" opacity=".82"/><circle cx="2390" cy="700" r="24" fill="url(#bibCandleGlow)"/><rect x="2378" y="640" width="24" height="12" fill="#6b1b30"/><rect x="2378" y="748" width="24" height="12" fill="#6b1b30"/></g>',
                '<g id="bib-zone-ajustes"><rect x="2488" y="770" width="88" height="14" fill="#2b1810"/><rect x="2498" y="784" width="10" height="52" fill="#160c08"/><rect x="2556" y="784" width="10" height="52" fill="#160c08"/><circle cx="2534" cy="752" r="20" fill="url(#bibCandleGlow)"/><rect x="2528" y="755" width="12" height="14" fill="#160c08"/></g>',
                '</svg></div>',
                '<div class="biblioteca-vignette"></div>',
                '<div class="biblioteca-dust-field"><span class="biblioteca-dust-mote s1"></span><span class="biblioteca-dust-mote s2"></span><span class="biblioteca-dust-mote s3"></span><span class="biblioteca-dust-mote s4"></span><span class="biblioteca-dust-mote s5"></span></div>'
            ].join('');
        },

        _aplicarZonaGuardada() {
            let destino = nombrePaginaActual();
            try {
                destino = sessionStorage.getItem('biblioteca-zone') || destino;
                sessionStorage.removeItem('biblioteca-zone');
            } catch (e) {}
            this.panearHacia(destino);
        },

        panearHacia(pagina) {
            const capa = document.getElementById('bibliotecaLibraryLayer');
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
                try { sessionStorage.setItem('biblioteca-zone', paginaDestino); } catch (e) {}

                const hoja = document.getElementById('bibliotecaPageTransition');
                hoja?.classList.remove('active');
                requestAnimationFrame(() => hoja?.classList.add('active'));
                document.documentElement.classList.add('biblioteca-leaving');
                window.setTimeout(() => { window.location.href = href; }, 410);
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
            if (!pendiente || document.querySelector('.biblioteca-welcome-overlay')) return false;

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
            overlay.className = 'biblioteca-welcome-overlay';
            overlay.id = 'welcomeOverlay';
            overlay.setAttribute('role', 'dialog');
            overlay.setAttribute('aria-modal', 'true');
            overlay.setAttribute('aria-label', 'Bienvenida a la Biblioteca MAPS');
            overlay.innerHTML = [
                '<div class="biblioteca-welcome-book">',
                '<section class="biblioteca-book-page biblioteca-book-left"><span class="biblioteca-page-ornament">❦</span><div class="biblioteca-seal-avatar" aria-hidden="true">' + escapar(iniciales) + '</div><div class="biblioteca-modal-user-name">' + escapar(nombreCompleto) + '</div><div class="biblioteca-modal-status">' + escapar(estado) + '</div></section>',
                '<span class="biblioteca-book-spine"></span>',
                '<section class="biblioteca-book-page biblioteca-book-right"><p class="biblioteca-modal-eyebrow">Capítulo de hoy</p><h1 class="biblioteca-modal-title">Bienvenido de vuelta, ' + escapar(nombreCorto) + '</h1><p class="biblioteca-modal-status">La biblioteca guarda tu lugar donde lo dejaste.</p></section>',
                '</div>'
            ].join('');
            document.body.appendChild(overlay);

            window.setTimeout(() => {
                overlay.classList.add('fade-out');
                window.setTimeout(() => overlay.remove(), 760);
            }, 2100);
            return true;
        }
    };

    window.Biblioteca = Biblioteca;
    document.dispatchEvent(new CustomEvent('biblioteca:listo'));
    Biblioteca.activar();
})();