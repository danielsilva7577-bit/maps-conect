/**
 * MAPS Connect - Estilos de la página
 * Permite elegir un estilo visual global (skin) que afecta a toda la
 * aplicación (sidebar, topbar, tarjetas, botones, formularios, fondo...).
 * Guarda la preferencia en localStorage y la aplica como atributo
 * "data-estilo" sobre <html>; frontend/css/estilos.css define los skins
 * bajo html[data-estilo="..."].
 *
 * Para añadir un estilo nuevo solo hay que:
 *   1) Agregar su entrada en ESTILOS (id, nombre, descripcion, preview).
 *   2) Escribir su bloque CSS en estilos.css bajo html[data-estilo="id"].
 *   3) Si el estilo inyecta su propio fondo (como "cyber"), añadir la
 *      lógica de inyección en _ponFondo/_limpiarFondo.
 */
const Estilos = {
    KEY: 'estilo-maps',

    STYLES: [
        {
            id: 'clasico',
            nombre: 'Clásico',
            descripcion: 'Estilo predeterminado de MAPS Connect con la paleta institucional.',
            preview: 'linear-gradient(135deg, #087527 0%, #3fb950 60%, #a3e635 100%)'
        },
        {
            id: 'cyber',
            nombre: 'Cyberpunk Network',
            descripcion: 'Red neuronal de neón cian y magenta sobre fondo de terminal oscura.',
            preview: 'linear-gradient(135deg, #00f0ff 0%, #1a0033 50%, #ff007f 100%)'
        },
        {
            id: 'medieval',
            nombre: 'Hogwarts Medieval',
            descripcion: 'Oro y pergamino sobre la fortaleza mágica (experiencia 3D tipo Hogwarts).',
            preview: 'linear-gradient(135deg, #d4af37 0%, #18110b 50%, #5a1414 100%)'
        },
        {
            id: 'saiyan',
            nombre: 'Saiyan Power Edition',
            descripcion: 'Nube Kinto, estelas de ki y aura dorada sobre el dojo de los Z-Warriors (naranja intenso).',
            preview: 'linear-gradient(135deg, #f97316 0%, #1e3a8a 50%, #facc15 100%)'
        },
        {
            id: 'noir',
            nombre: 'Cyber Noir Edition',
            descripcion: 'Callejón nocturno con neón rojo, lluvia digital y expedientes confidenciales (gótico cyber).',
            preview: 'linear-gradient(135deg, #e11d48 0%, #080305 50%, #3a0011 100%)'
        },
        {
            id: 'alchemy',
            nombre: 'Alchemical Codex Edition',
            descripcion: 'Astrolabio, geometría sagrada y oro de filósofo sobre la cámara hermetista (esmeralda).',
            preview: 'linear-gradient(135deg, #10b981 0%, #020d08 50%, #fbbf24 100%)'
        },
        {
            id: 'minimal',
            nombre: 'Minimalist Tricolor Edition',
            descripcion: 'Geometría suiza azul, rojo y blanco con retícula fina y parallax pausado (estilo internacional).',
            preview: 'linear-gradient(135deg, #2563eb 0%, #f8fafc 50%, #dc2626 100%)'
        },
        {
            id: 'invernadero',
            nombre: 'Invernadero',
            descripcion: 'Cubierta vegetal, foliaje mecido y luz tamizada sobre una experiencia académica que crece (verde institucional).',
            preview: 'linear-gradient(135deg, #3E6B4F 0%, #E7EEDA 50%, #C97B4A 100%)'
        }
    ],

    _VARIABLES_ACCENTO: ['--accent', '--accent-dark', '--accent-mid', '--accent-bright',
        '--accent-deep', '--accent-soft', '--accent-soft-border', '--accent-contrast'],

    /** Textos personalizados por skin (botones, enlaces y placeholders).
        Las claves son el texto original exacto y los valores el texto temático. */
    TEXTOS_ESTILO: {
        cyber: {},
        medieval: {
            'Publicar duda': 'Enviar al Palacio',
            'Publicar Duda': 'Enviar al Palacio',
            'Publicar': 'Enviar al Palacio',
            'Publicar de todos modos': 'Enviar de todos modos',
            'Cerrar sesión': 'Abandonar el Castillo',
            'Guardar': 'Guardar en el Pergamino',
            'Enviar Mensaje Privado': 'Enviar por Búho',
            '+ Crear sesión de repaso': '+ Convocar Círculo de Estudio',
            'Crear sesión': 'Convocar Estudio',
            'Unirme': 'Unirme al Círculo',
            '¿Tienes una duda académica? Pregunta a tu comunidad...': 'Lanza tu pregón académico a la Comunidad del Castillo...'
        },
        saiyan: {
            'Publicar duda': 'Lanzar Ataque Ki',
            'Publicar Duda': 'Lanzar Ataque Ki',
            'Publicar': 'Lanzar Ataque Ki',
            'Publicar de todos modos': 'Lanzar de todos modos',
            'Cerrar sesión': 'Descansar / Salir',
            'Guardar': 'Guardar Energía',
            'Enviar Mensaje Privado': 'Telepatía Instantánea',
            '+ Crear sesión de repaso': '+ Crear Sala de Entrenamiento',
            'Crear sesión': 'Crear Entrenamiento',
            'Unirme': 'Entrar al Dojo',
            '¿Tienes una duda académica? Pregunta a tu comunidad...': 'Lanza tu duda o reto técnico al campo de entrenamiento...'
        },
        noir: {
            'Publicar duda': 'Registrar Pista',
            'Publicar Duda': 'Registrar Pista',
            'Publicar': 'Registrar Pista',
            'Publicar de todos modos': 'Registrar de todos modos',
            'Cerrar sesión': 'Cerrar Expediente',
            'Guardar': 'Sellar en Archivo',
            'Enviar Mensaje Privado': 'Enviar Nota Cifrada',
            '+ Crear sesión de repaso': '+ Convocar Ronda de Estudio',
            'Crear sesión': 'Convocar Ronda',
            'Unirme': 'Entrar a la Ronda',
            '¿Tienes una duda académica? Pregunta a tu comunidad...': 'Inscribe una pista, duda o cabo suelto al expediente...'
        },
        alchemy: {
            'Publicar duda': 'Liberar Sello',
            'Publicar Duda': 'Liberar Sello',
            'Publicar': 'Liberar Sello',
            'Publicar de todos modos': 'Liberar de todos modos',
            'Cerrar sesión': 'Cerrar Grimorio / Salir',
            'Guardar': 'Sellar en Papiro',
            'Enviar Mensaje Privado': 'Enviar Sello Privado',
            '+ Crear sesión de repaso': '+ Convocar Círculo Esotérico',
            'Crear sesión': 'Convocar Círculo',
            'Unirme': 'Unirme al Círculo',
            '¿Tienes una duda académica? Pregunta a tu comunidad...': 'Inscribe una consulta o runa técnica al Códice...'
        },
        minimal: {
            'Publicar duda': 'Enviar Solicitud',
            'Publicar Duda': 'Enviar Solicitud',
            'Publicar': 'Enviar Solicitud',
            'Publicar de todos modos': 'Enviar de todos modos',
            'Cerrar sesión': 'Cerrar Sesión',
            'Guardar': 'Guardar Cambios',
            'Enviar Mensaje Privado': 'Enviar Mensaje',
            '+ Crear sesión de repaso': '+ Programar Sesión',
            'Crear sesión': 'Programar Sesión',
            'Unirme': 'Unirme a la Sesión',
            '¿Tienes una duda académica? Pregunta a tu comunidad...': 'Registra tu duda académica en el sistema...'
        },
        invernadero: {
            'Publicar duda': 'Plantar la consulta',
            'Publicar Duda': 'Plantar la consulta',
            'Publicar': 'Plantar la consulta',
            'Publicar de todos modos': 'Plantar de todos modos',
            'Cerrar sesión': 'Regar y cerrar sesión',
            'Guardar': 'Guardar en el semillero',
            'Enviar Mensaje Privado': 'Enviar rama privada',
            '+ Crear sesión de repaso': '+ Crear sesión de cultivo',
            'Crear sesión': 'Crear sesión de cultivo',
            'Unirme': 'Unirme al cultivo',
            '¿Tienes una duda académica? Pregunta a tu comunidad...': 'Siembra una duda o nota para tu comunidad...'
        }
    },

    guardado() {
        try { return localStorage.getItem(this.KEY) || 'clasico'; } catch (e) { return 'clasico'; }
    },

    /** Estilo activo en este momento (sin consultar storage). */
    activo() {
        return document.documentElement.getAttribute('data-estilo') || 'clasico';
    },

    /* ------------------------------------------------------------
       Fondos de estilo (elementos inyectados al final de <body>)
       ------------------------------------------------------------ */

    _cyberSVG() {
        return `
        <svg class="cyber-svg-layer" viewBox="0 0 1400 900" preserveAspectRatio="none" aria-hidden="true">
            <rect width="1400" height="900" fill="#010204"/>
            <g class="data-link">
                <line x1="200" y1="200" x2="500" y2="350"/>
                <line x1="500" y1="350" x2="700" y2="200"/>
                <line x1="700" y1="200" x2="1000" y2="300"/>
                <line x1="1000" y1="300" x2="1200" y2="600"/>
                <line x1="500" y1="350" x2="600" y2="650"/>
                <line x1="600" y1="650" x2="300" y2="750"/>
                <line x1="300" y1="750" x2="200" y2="200"/>
                <line x1="700" y1="200" x2="850" y2="500"/>
                <line x1="850" y1="500" x2="1200" y2="600"/>
                <line x1="500" y1="350" x2="850" y2="500"/>
            </g>
            <circle class="data-node" cx="200" cy="200" r="6"/>
            <circle class="data-node" cx="1000" cy="300" r="5"/>
            <circle class="data-node" cx="600" cy="650" r="6"/>
            <circle class="data-node" cx="300" cy="750" r="5"/>
            <circle class="data-node" cx="1200" cy="600" r="7"/>
            <circle class="core-node" cx="700" cy="350" r="16"/>
            <circle class="data-node" cx="500" cy="350" r="8"/>
            <circle class="data-node" cx="700" cy="200" r="8"/>
            <circle class="data-node" cx="850" cy="500" r="9"/>
        </svg>`;
    },

    /** Mapa de página -> posición de cámara (pan/zoom) sobre el skyline del
        castillo. dx/dy en píxeles sobre la capa de 2600x1100 y escala. */
    _castilloZonas() {
        return {
            'inicio.html': { dx: 0, dy: 40, scale: 0.62 },
            'perfil.html': { dx: 1000, dy: -60, scale: 1.15 },
            'certificados.html': { dx: 650, dy: -40, scale: 1.1 },
            'comunidad.html': { dx: 300, dy: -20, scale: 1.05 },
            'mensajes.html': { dx: -300, dy: -30, scale: 1.15 },
            'empresarial.html': { dx: -650, dy: -10, scale: 1.05 },
            'circulos.html': { dx: -950, dy: -30, scale: 1.1 },
            'ajustes.html': { dx: -1200, dy: -20, scale: 1.2 },
            'admin.html': { dx: 0, dy: 60, scale: 1.25 },
            'docente.html': { dx: 0, dy: 40, scale: 1.15 }
        };
    },

    /** Skyline panorámico del castillo (2600x1100) con zonas por sección. */
    _castleSkyline() {
        return `
        <svg class="castle-layer" id="castleLayer" viewBox="0 0 2600 1100" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <defs>
                <linearGradient id="skyGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#1C0F0B"/>
                    <stop offset="100%" stop-color="#0E0806"/>
                </linearGradient>
                <linearGradient id="stoneGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#4A2E24"/>
                    <stop offset="100%" stop-color="#2A1A15"/>
                </linearGradient>
            </defs>

            <rect width="2600" height="1100" fill="url(#skyGrad)"/>
            <circle cx="1300" cy="180" r="90" fill="#3A2C1E" opacity="0.5"/>

            <g fill="#180E0A" opacity="0.7">
                <polygon points="0,500 200,380 400,500 600,410 800,500 1000,430 1200,500 1400,420 1600,500 1800,440 2000,500 2200,410 2400,500 2600,440 2600,700 0,700"/>
            </g>

            <g id="zone-perfil">
                <rect x="220" y="480" width="160" height="420" fill="url(#stoneGrad)"/>
                <polygon points="200,480 300,380 400,480" fill="#3E1717"/>
                <rect x="270" y="560" width="60" height="90" fill="#0E0806"/>
                <rect x="245" y="700" width="30" height="50" fill="#0E0806"/>
                <rect x="325" y="700" width="30" height="50" fill="#0E0806"/>
                <rect x="290" y="360" width="20" height="40" fill="#A9834B"/>
                <polygon points="290,360 300,335 310,360" fill="#A9834B"/>
            </g>

            <g id="zone-ruta">
                <rect x="560" y="440" width="180" height="460" fill="url(#stoneGrad)"/>
                <polygon points="540,440 650,320 760,440" fill="#3E1717"/>
                <circle cx="650" cy="520" r="34" fill="#0E0806"/>
                <circle cx="650" cy="520" r="24" fill="#A9834B" opacity="0.3"/>
                <rect x="600" y="720" width="30" height="55" fill="#0E0806"/>
                <rect x="670" y="720" width="30" height="55" fill="#0E0806"/>
                <rect x="640" y="300" width="20" height="35" fill="#A9834B"/>
                <polygon points="640,300 650,278 660,300" fill="#A9834B"/>
                <path d="M615,470 L685,470 M650,470 L650,510" stroke="#A9834B" stroke-width="2" opacity="0.4"/>
            </g>

            <g id="zone-comunidad">
                <rect x="900" y="560" width="420" height="340" fill="url(#stoneGrad)"/>
                <polygon points="880,560 1110,470 1340,560" fill="#3E1717"/>
                <circle cx="960" cy="640" r="26" fill="#0E0806"/>
                <circle cx="1040" cy="640" r="26" fill="#0E0806"/>
                <circle cx="1120" cy="640" r="26" fill="#0E0806"/>
                <circle cx="1200" cy="640" r="26" fill="#0E0806"/>
                <circle cx="1280" cy="640" r="26" fill="#0E0806"/>
                <rect x="950" y="760" width="45" height="60" fill="#0E0806"/>
                <rect x="1240" y="760" width="45" height="60" fill="#0E0806"/>
            </g>

            <g id="zone-inicio">
                <rect x="1180" y="330" width="240" height="570" fill="url(#stoneGrad)"/>
                <polygon points="1150,330 1300,180 1450,330" fill="#3E1717"/>
                <rect x="1270" y="150" width="60" height="60" fill="#3E1717"/>
                <polygon points="1270,150 1300,110 1330,150" fill="#5C2323"/>
                <rect x="1290" y="90" width="10" height="40" fill="#A9834B"/>
                <polygon points="1300,90 1330,105 1300,110" fill="#5C2323"/>
                <circle cx="1300" cy="430" r="46" fill="#0E0806"/>
                <circle cx="1300" cy="430" r="32" fill="#A9834B" opacity="0.25"/>
                <rect x="1225" y="560" width="60" height="90" fill="#0E0806"/>
                <rect x="1315" y="560" width="60" height="90" fill="#0E0806"/>
                <rect x="1250" y="760" width="100" height="140" fill="#0E0806"/>
                <polygon points="1250,760 1300,715 1350,760" fill="#180E0A"/>
                <rect x="900" y="850" width="800" height="30" fill="#241512"/>
                <g fill="#241512">
                    <rect x="900" y="826" width="20" height="24"/><rect x="940" y="826" width="20" height="24"/>
                    <rect x="980" y="826" width="20" height="24"/><rect x="1020" y="826" width="20" height="24"/>
                    <rect x="1060" y="826" width="20" height="24"/><rect x="1100" y="826" width="20" height="24"/>
                    <rect x="1140" y="826" width="20" height="24"/><rect x="1180" y="826" width="20" height="24"/>
                    <rect x="1220" y="826" width="20" height="24"/><rect x="1260" y="826" width="20" height="24"/>
                    <rect x="1300" y="826" width="20" height="24"/><rect x="1340" y="826" width="20" height="24"/>
                    <rect x="1380" y="826" width="20" height="24"/><rect x="1420" y="826" width="20" height="24"/>
                    <rect x="1460" y="826" width="20" height="24"/><rect x="1500" y="826" width="20" height="24"/>
                    <rect x="1540" y="826" width="20" height="24"/><rect x="1580" y="826" width="20" height="24"/>
                    <rect x="1620" y="826" width="20" height="24"/><rect x="1660" y="826" width="20" height="24"/>
                </g>
            </g>

            <g id="zone-mensajes">
                <rect x="1700" y="470" width="150" height="430" fill="url(#stoneGrad)"/>
                <polygon points="1680,470 1775,370 1870,470" fill="#3E1717"/>
                <rect x="1745" y="540" width="24" height="70" fill="#0E0806"/>
                <rect x="1785" y="540" width="24" height="70" fill="#0E0806"/>
                <rect x="1730" y="700" width="30" height="50" fill="#0E0806"/>
                <rect x="1795" y="700" width="30" height="50" fill="#0E0806"/>
                <path d="M1700,500 L1660,470 M1870,500 L1910,470" stroke="#A9834B" stroke-width="2" opacity="0.5"/>
                <ellipse cx="1660" cy="465" rx="10" ry="6" fill="#A9834B" opacity="0.5"/>
                <ellipse cx="1910" cy="465" rx="10" ry="6" fill="#A9834B" opacity="0.5"/>
            </g>

            <g id="zone-empresarial">
                <rect x="1970" y="600" width="130" height="300" fill="url(#stoneGrad)"/>
                <rect x="2110" y="560" width="130" height="340" fill="url(#stoneGrad)"/>
                <rect x="2250" y="600" width="130" height="300" fill="url(#stoneGrad)"/>
                <polygon points="1960,600 2035,545 2110,600" fill="#3E1717"/>
                <polygon points="2100,560 2175,500 2250,560" fill="#5C2323"/>
                <polygon points="2240,600 2315,545 2390,600" fill="#3E1717"/>
                <rect x="2000" y="700" width="30" height="45" fill="#0E0806"/>
                <rect x="2150" y="680" width="30" height="45" fill="#0E0806"/>
                <rect x="2300" y="700" width="30" height="45" fill="#0E0806"/>
                <rect x="1990" y="900" width="420" height="14" fill="#A9834B" opacity="0.35"/>
            </g>

            <g id="zone-circulos">
                <rect x="2280" y="500" width="220" height="400" fill="url(#stoneGrad)"/>
                <circle cx="2390" cy="480" r="120" fill="#3E1717"/>
                <circle cx="2390" cy="480" r="90" fill="#2A1512"/>
                <circle cx="2390" cy="480" r="16" fill="#A9834B" opacity="0.4"/>
                <rect x="2330" y="720" width="30" height="55" fill="#0E0806"/>
                <rect x="2420" y="720" width="30" height="55" fill="#0E0806"/>
            </g>

            <g id="zone-ajustes">
                <rect x="2470" y="560" width="110" height="340" fill="url(#stoneGrad)"/>
                <polygon points="2455,560 2525,480 2595,560" fill="#3E1717"/>
                <rect x="2500" y="620" width="50" height="60" fill="#0E0806"/>
                <rect x="2495" y="750" width="26" height="40" fill="#0E0806"/>
                <rect x="2534" y="750" width="26" height="40" fill="#0E0806"/>
                <rect x="2515" y="450" width="18" height="34" fill="#A9834B"/>
                <polygon points="2515,450 2524,430 2533,450" fill="#A9834B"/>
            </g>

            <g fill="#A9834B" opacity="0.4">
                <circle cx="150" cy="90" r="2"/><circle cx="480" cy="60" r="2"/>
                <circle cx="900" cy="100" r="2"/><circle cx="1700" cy="70" r="2"/>
                <circle cx="2100" cy="110" r="2"/><circle cx="2450" cy="80" r="2"/>
            </g>
        </svg>`;
    },

    /** Posiciona la cámara del castillo según la sección activa del menú. */
    _panCastilloZona() {
        const layer = document.querySelector('.castle-layer');
        if (!layer) return;
        const activo = document.querySelector('#sidebar .sidebar-nav a.active, .sidebar-nav a.active');
        const href = activo ? activo.getAttribute('href') : 'inicio.html';
        const zona = (this._castilloZonas()[href] || this._castilloZonas()['inicio.html']);
        layer.style.transform = `translate(${zona.dx}px, ${zona.dy}px) scale(${zona.scale})`;
    },

    /** Re-encuadra la cámara del castillo cuando el menú queda listo, al
        cambiar de skin y al navegar (la página activa determina la zona). */
    _bindCastilloPan() {
        if (this._castlePanBinded) {
            this._panCastilloZona();
            return;
        }
        this._castlePanBinded = true;
        document.addEventListener('estilos:cambio', () => this._panCastilloZona());
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this._panCastilloZona(), { once: true });
        } else {
            this._panCastilloZona();
        }
    },

    _saiyanSVG() {
        return `
        <svg class="saiyan-svg-layer" viewBox="0 0 1400 900" preserveAspectRatio="none" aria-hidden="true">
            <rect width="1400" height="900" fill="#f97316"/>

            <path class="kintoun-cloud" d="M300,500 Q350,420 450,450 Q520,380 620,430 Q700,390 780,460 Q880,440 920,520 Q1000,530 980,620 Q900,680 300,680 Z"/>

            <path class="ki-energy-trail" d="M100,100 L1300,800"/>
            <path class="ki-energy-trail" d="M200,800 L1200,100"/>
            <path class="ki-energy-trail" d="M0,450 L1400,450"/>
        </svg>`;
    },

    _noirSVG() {
        return `
        <svg class="noir-svg-layer" viewBox="0 0 1400 900" preserveAspectRatio="none" aria-hidden="true">
            <rect width="1400" height="900" fill="#080305"/>
            <path d="M0,700 L250,500 L450,550 L750,380 L1050,480 L1400,400 L1400,900 L0,900 Z" fill="#0d0407"/>
            <path class="neon-sign" d="M300,320 L350,280 L400,320 L350,360 Z"/>
            <path class="neon-sign" d="M950,250 L1020,250 L1020,310 L950,310 Z"/>
            <g class="rain-streak">
                <line x1="100" y1="0" x2="60" y2="150"/>
                <line x1="300" y1="0" x2="260" y2="150"/>
                <line x1="500" y1="0" x2="460" y2="150"/>
                <line x1="700" y1="0" x2="660" y2="150"/>
                <line x1="900" y1="0" x2="860" y2="150"/>
                <line x1="1100" y1="0" x2="1060" y2="150"/>
                <line x1="1300" y1="0" x2="1260" y2="150"/>
            </g>
        </svg>`;
    },

    _alchemySVG() {
        return `
        <svg class="alchemy-svg-layer" viewBox="0 0 1400 900" preserveAspectRatio="none" aria-hidden="true">
            <rect width="1400" height="900" fill="#020d08"/>
            <g transform="translate(700, 450)">
                <circle class="sacred-geometry" cx="0" cy="0" r="350" />
                <circle class="sacred-geometry" cx="0" cy="0" r="220" />
                <circle class="sacred-geometry" cx="0" cy="0" r="100" />
                <polygon class="sacred-geometry" points="0,-350 303,175 -303,175" />
                <polygon class="sacred-geometry" points="0,350 -303,-175 303,-175" />
                <circle class="alchemical-ring" cx="0" cy="0" r="280" />
            </g>
        </svg>`;
    },

    _minimalSVG() {
        return `
        <svg class="minimal-svg-layer" viewBox="0 0 1400 900" preserveAspectRatio="none" aria-hidden="true">
            <rect width="1400" height="900" fill="#f8fafc"/>
            <rect class="accent-block-blue" x="80" y="60" width="420" height="300" />
            <rect class="accent-block-red" x="940" y="420" width="360" height="330" />
            <g class="geo-line">
                <line x1="0" y1="200" x2="1400" y2="200" />
                <line x1="0" y1="600" x2="1400" y2="600" />
                <line x1="340" y1="0" x2="340" y2="900" />
                <line x1="1060" y1="0" x2="1060" y2="900" />
            </g>
        </svg>`;
    },

    /** Capas de follaje del invernadero: el dosel de hojas se divide en tres
        profundidades (atrás / medio / frente) que se mecen con el viento. */
    _greenhouseSVG() {
        return `
        <div class="greenhouse-canopy">
            <svg class="canopy-svg canopy-layer-back leaf-sway" viewBox="0 0 1400 900" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
                <g fill="#A9C29B">
                    <ellipse cx="90" cy="30" rx="60" ry="34" transform="rotate(30 90 30)"/>
                    <ellipse cx="180" cy="10" rx="50" ry="28" transform="rotate(-15 180 10)"/>
                    <ellipse cx="1300" cy="20" rx="65" ry="36" transform="rotate(-25 1300 20)"/>
                    <ellipse cx="1220" cy="50" rx="45" ry="26" transform="rotate(20 1220 50)"/>
                    <ellipse cx="70" cy="870" rx="70" ry="38" transform="rotate(-20 70 870)"/>
                    <ellipse cx="1330" cy="860" rx="60" ry="34" transform="rotate(15 1330 860)"/>
                </g>
            </svg>
            <svg class="canopy-svg canopy-layer-mid leaf-sway-rev" viewBox="0 0 1400 900" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
                <g stroke="#3E6B4F" stroke-width="3" fill="none" stroke-linecap="round">
                    <path d="M0,0 C40,60 10,140 60,200 C110,260 70,340 120,400"/>
                    <path d="M1400,0 C1360,70 1390,150 1330,210 C1270,270 1310,350 1250,410"/>
                    <path d="M0,900 C50,830 20,750 70,690 C120,630 80,560 130,500"/>
                    <path d="M1400,900 C1350,830 1380,750 1320,690 C1260,630 1300,560 1250,500"/>
                </g>
                <g fill="#5E8A63">
                    <ellipse cx="60" cy="200" rx="20" ry="11" transform="rotate(35 60 200)"/>
                    <ellipse cx="120" cy="400" rx="18" ry="10" transform="rotate(-20 120 400)"/>
                    <ellipse cx="1330" cy="210" rx="20" ry="11" transform="rotate(-35 1330 210)"/>
                    <ellipse cx="1250" cy="410" rx="18" ry="10" transform="rotate(20 1250 410)"/>
                    <ellipse cx="70" cy="690" rx="19" ry="10" transform="rotate(-30 70 690)"/>
                    <ellipse cx="130" cy="500" rx="17" ry="9" transform="rotate(25 130 500)"/>
                    <ellipse cx="1320" cy="690" rx="19" ry="10" transform="rotate(30 1320 690)"/>
                    <ellipse cx="1250" cy="500" rx="17" ry="9" transform="rotate(-25 1250 500)"/>
                </g>
            </svg>
            <svg class="canopy-svg canopy-layer-front leaf-drift" viewBox="0 0 1400 900" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
                <g fill="#3E6B4F">
                    <ellipse cx="30" cy="0" rx="90" ry="55" transform="rotate(20 30 0)"/>
                    <ellipse cx="1370" cy="0" rx="95" ry="58" transform="rotate(-18 1370 0)"/>
                    <ellipse cx="10" cy="900" rx="85" ry="50" transform="rotate(-22 10 900)"/>
                    <ellipse cx="1390" cy="900" rx="88" ry="52" transform="rotate(18 1390 900)"/>
                </g>
                <g fill="#2F5340" opacity="0.5">
                    <circle cx="120" cy="70" r="6"/>
                    <circle cx="1280" cy="80" r="5"/>
                    <circle cx="90" cy="830" r="5"/>
                    <circle cx="1310" cy="820" r="6"/>
                </g>
            </svg>
        </div>
        <div class="light-dapple"></div>`;
    },

    _limpiarFondo() {
        document.querySelectorAll('.cyber-bg-viewport, .medieval-bg-viewport, .saiyan-bg-viewport, .noir-bg-viewport, .alchemy-bg-viewport, .minimal-bg-viewport, .invernadero-bg-viewport, .greenhouse-canopy, .light-dapple, .atmosphere-overlay, .castle-vignette, .cyber-blade-transition, .med-blade-transition, .saiyan-aura-transition, .noir-blade-transition, .alchemy-blade-transition, .minimal-blade-transition, .invernadero-leaf-transition, .heraldic-transition').forEach(el => el.remove());
    },

    _limpiarAccentoInline() {
        const r = document.documentElement;
        for (const k of this._VARIABLES_ACCENTO) r.style.removeProperty(k);
    },

    /** Carga las fuentes decorativas (Cinzel/Bangers/Montserrat) cuando el
        skin activo las usa; evita cargarlas en skins que no las necesitan. */
    _cargarFuentes() {
        if (this._fuentesCargadas) return;
        this._fuentesCargadas = true;
        const id = 'estilos-fuentes';
        if (document.getElementById(id)) return;
        const link = document.createElement('link');
        link.id = id;
        link.rel = 'stylesheet';
        link.href = 'https://fonts.googleapis.com/css2?family=Bangers&family=Cinzel:wght@400;700;900&family=EB+Garamond:ital,wght@0,400;0,500;0,600;1,400&family=Fraunces:opsz,wght@9..144,400;9..144,500;9..144,600;9..144,700&family=Inter:wght@400;500;600;700;800&family=Montserrat:wght@500;600;700;900&family=Share+Tech+Mono&family=Special+Elite&family=Space+Mono:ital,wght@0,400;0,700;1,400&family=Work+Sans:wght@400;500;600;700&display=swap';
        document.head.appendChild(link);
    },

    _ponFondo() {
        const estilo = this.activo();
        if (estilo === 'medieval') return this._ponFondoMedieval();
        if (estilo === 'saiyan') return this._ponFondoSaiyan();
        if (estilo === 'noir') return this._ponFondoNoir();
        if (estilo === 'alchemy') return this._ponFondoAlchemy();
        if (estilo === 'minimal') return this._ponFondoMinimal();
        if (estilo === 'invernadero') return this._ponFondoInvernadero();
        return this._ponFondoCyber();
    },

    _ponFondoCyber() {
        if (document.querySelector('.cyber-bg-viewport')) return;
        const inyectar = () => {
            const viewport = document.createElement('div');
            viewport.className = 'cyber-bg-viewport';
            viewport.innerHTML = this._cyberSVG();

            const atmosfera = document.createElement('div');
            atmosfera.className = 'atmosphere-overlay';

            // Cortina de transición "acordeón neural" al navegar (mockup).
            const blade = document.createElement('div');
            blade.className = 'cyber-blade-transition';
            blade.id = 'accordionBlade';
            blade.innerHTML = `
                <div class="accordion-panel"></div>
                <div class="accordion-panel"></div>
                <div class="accordion-panel"></div>
                <div class="accordion-panel"></div>`;

            document.body.insertBefore(viewport, document.body.firstChild);
            document.body.insertBefore(atmosfera, document.body.firstChild);
            document.body.insertBefore(blade, document.body.firstChild);
            this._registrarParallax(viewport.querySelector('.cyber-svg-layer'), 'cyber');
            this._bindBlade();
        };

        if (document.body) {
            inyectar();
        } else {
            // El script corre en <head> (anti-FOUC): el fondo se inyecta al
            // terminar de cargar el DOM, y de paso se vuelve a liberar el
            // acento por si colores.js aplicó sus variables inline.
            document.addEventListener('DOMContentLoaded', () => {
                this._limpiarAccentoInline();
                inyectar();
            }, { once: true });
        }
    },

    _ponFondoMedieval() {
        if (document.querySelector('.medieval-bg-viewport')) return;
        this._cargarFuentes();
        const inyectar = () => {
            const viewport = document.createElement('div');
            viewport.className = 'medieval-bg-viewport';
            viewport.innerHTML = this._castleSkyline();

            // Viñeta nocturna del castillo.
            const vineta = document.createElement('div');
            vineta.className = 'castle-vignette';

            // Cortina heráldica "sello del castillo" al navegar (mockup El Castillo).
            const heraldico = document.createElement('div');
            heraldico.className = 'heraldic-transition';
            heraldico.id = 'heraldicTransition';

            document.body.insertBefore(viewport, document.body.firstChild);
            document.body.insertBefore(vineta, document.body.firstChild);
            document.body.insertBefore(heraldico, document.body.firstChild);
            this._bindCastilloPan();
            this._bindBlade();
        };

        if (document.body) {
            inyectar();
        } else {
            document.addEventListener('DOMContentLoaded', () => {
                this._limpiarAccentoInline();
                inyectar();
            }, { once: true });
        }
    },

    _ponFondoSaiyan() {
        if (document.querySelector('.saiyan-bg-viewport')) return;
        this._cargarFuentes();
        const inyectar = () => {
            const viewport = document.createElement('div');
            viewport.className = 'saiyan-bg-viewport';
            viewport.innerHTML = this._saiyanSVG();

            const atmosfera = document.createElement('div');
            atmosfera.className = 'atmosphere-overlay';

            // Cortina de transición "aura de ki" al navegar (mockup Saiyan).
            const blade = document.createElement('div');
            blade.className = 'saiyan-aura-transition';

            document.body.insertBefore(viewport, document.body.firstChild);
            document.body.insertBefore(atmosfera, document.body.firstChild);
            document.body.insertBefore(blade, document.body.firstChild);
            this._registrarParallax(viewport.querySelector('.saiyan-svg-layer'), 'saiyan');
            this._bindBlade();
        };

        if (document.body) {
            inyectar();
        } else {
            document.addEventListener('DOMContentLoaded', () => {
                this._limpiarAccentoInline();
                inyectar();
            }, { once: true });
        }
    },

    _ponFondoNoir() {
        if (document.querySelector('.noir-bg-viewport')) return;
        this._cargarFuentes();
        const inyectar = () => {
            const viewport = document.createElement('div');
            viewport.className = 'noir-bg-viewport';
            viewport.innerHTML = this._noirSVG();

            const atmosfera = document.createElement('div');
            atmosfera.className = 'atmosphere-overlay';

            // Cortina de transición "barrido" del callejón noir al navegar (mockup Noir).
            const blade = document.createElement('div');
            blade.className = 'noir-blade-transition';

            document.body.insertBefore(viewport, document.body.firstChild);
            document.body.insertBefore(atmosfera, document.body.firstChild);
            document.body.insertBefore(blade, document.body.firstChild);
            this._registrarParallax(viewport.querySelector('.noir-svg-layer'), 'noir');
            this._bindBlade();
        };

        if (document.body) {
            inyectar();
        } else {
            document.addEventListener('DOMContentLoaded', () => {
                this._limpiarAccentoInline();
                inyectar();
            }, { once: true });
        }
    },

    _ponFondoAlchemy() {
        if (document.querySelector('.alchemy-bg-viewport')) return;
        this._cargarFuentes();
        const inyectar = () => {
            const viewport = document.createElement('div');
            viewport.className = 'alchemy-bg-viewport';
            viewport.innerHTML = this._alchemySVG();

            const atmosfera = document.createElement('div');
            atmosfera.className = 'atmosphere-overlay';

            // Cortina de transmutación al navegar (mockup Alchemical Codex).
            const blade = document.createElement('div');
            blade.className = 'alchemy-blade-transition';

            document.body.insertBefore(viewport, document.body.firstChild);
            document.body.insertBefore(atmosfera, document.body.firstChild);
            document.body.insertBefore(blade, document.body.firstChild);
            this._registrarParallax(viewport.querySelector('.alchemy-svg-layer'), 'alchemy');
            this._bindBlade();
        };

        if (document.body) {
            inyectar();
        } else {
            document.addEventListener('DOMContentLoaded', () => {
                this._limpiarAccentoInline();
                inyectar();
            }, { once: true });
        }
    },

    _ponFondoMinimal() {
        if (document.querySelector('.minimal-bg-viewport')) return;
        this._cargarFuentes();
        const inyectar = () => {
            const viewport = document.createElement('div');
            viewport.className = 'minimal-bg-viewport';
            viewport.innerHTML = this._minimalSVG();

            // Cortina tricolor (azul→blanco→rojo) al navegar (mockup Swiss).
            const blade = document.createElement('div');
            blade.className = 'minimal-blade-transition';

            document.body.insertBefore(viewport, document.body.firstChild);
            document.body.insertBefore(blade, document.body.firstChild);
            this._registrarParallax(viewport.querySelector('.minimal-svg-layer'), 'minimal');
            this._bindBlade();
        };

        if (document.body) {
            inyectar();
        } else {
            document.addEventListener('DOMContentLoaded', () => {
                this._limpiarAccentoInline();
                inyectar();
            }, { once: true });
        }
    },

    _ponFondoInvernadero() {
        if (document.querySelector('.invernadero-bg-viewport')) return;
        this._cargarFuentes();
        const inyectar = () => {
            const viewport = document.createElement('div');
            viewport.className = 'invernadero-bg-viewport';
            viewport.innerHTML = this._greenhouseSVG();

            // Cortina de transición "hoja" al navegar (mockup invernadero).
            const leaf = document.createElement('div');
            leaf.className = 'invernadero-leaf-transition';
            leaf.id = 'leafTransition';

            document.body.insertBefore(viewport, document.body.firstChild);
            document.body.insertBefore(leaf, document.body.firstChild);
            this._bindBlade();
        };

        if (document.body) {
            inyectar();
        } else {
            document.addEventListener('DOMContentLoaded', () => {
                this._limpiarAccentoInline();
                inyectar();
            }, { once: true });
        }
    },

    /**
     * Motor de parallax unificado: un único listener de mousemove comparte el
     * cursor y una única RAF por frame aplica el transform al fondo del skin
     * activo. En vez de 6 listeners y una escritura de estilo por evento,
     * hay 1 listener y como máximo 1 write por frame (lo esencial para
     * suavidad en pantallas de 60-120 Hz).
     */
    _registrarParallax(layer, color, extra) {
        if (!layer || !color || layer.dataset.parallax) return;
        layer.dataset.parallax = '1';
        if (!this._parallaxCapas) this._parallaxCapas = {};
        this._parallaxCapas[color] = { layer, extra: extra || null };
        if (this._parallaxListo) return;
        this._parallaxListo = true;
        window.addEventListener('mousemove', e => {
            this._parallaxX = e.clientX;
            this._parallaxY = e.clientY;
            if (this._parallaxRAFId) return;
            this._parallaxRAFId = requestAnimationFrame(() => {
                this._parallaxRAFId = 0;
                this._aplicarParallax();
            });
        }, { passive: true });
    },

    /** Aplica el parallax del skin activo con su geometría específica. */
    _aplicarParallax() {
        const estilo = document.documentElement.getAttribute('data-estilo');
        const capa = this._parallaxCapas && this._parallaxCapas[estilo];
        if (!capa) return;
        const layer = capa.layer;
        // Quita la transición CSS en el primer movimiento para que el
        // parallax siga el cursor de forma inmediata y no se sienta "muerto".
        if (layer.style.transition !== 'none') layer.style.transition = 'none';
        const x = (this._parallaxX / window.innerWidth) - 0.5;
        const y = (this._parallaxY / window.innerHeight) - 0.5;
        let t = '';
        switch (estilo) {
            case 'cyber':
                t = `translate3d(${x * 30}px, ${y * 22}px, 0) scale(1.05) rotateX(${15 - y * 8}deg)`;
                break;
            case 'medieval':
                t = `translate3d(${x * 24}px, ${y * 18}px, 0) scale(${1 + y * 0.05}) rotateY(${x * 4}deg)`;
                if (capa.extra) capa.extra.style.transform = `translateX(${x * -35}px) translateY(${y * -22}px)`;
                break;
            case 'saiyan':
                t = `translate3d(${x * -30}px, ${y * -20}px, 0) scale(${1 + y * 0.06})`;
                break;
            case 'noir':
                t = `translate3d(${x * 22}px, ${y * 16}px, 0) scale(${1 + y * 0.05}) rotateX(${8 - y * 5}deg)`;
                break;
            case 'alchemy':
                t = `translate3d(${x * 30}px, ${y * 22}px, 0) scale(${1 + y * 0.05}) rotateX(${15 - y * 8}deg)`;
                break;
            case 'minimal':
                t = `translate3d(${x * 18}px, ${y * 14}px, 0) scale(${1 + y * 0.03}) rotateX(${4 - y * 2}deg)`;
                break;
        }
        if (t) layer.style.transform = t;
    },

    /** Cortina de transición al navegar entre páginas del sidebar. */
    _bindBlade() {
        if (this._bladeBinded) return;
        this._bladeBinded = true;

        document.addEventListener('click', (e) => {
            const estilo = document.documentElement.getAttribute('data-estilo');
            if (estilo !== 'cyber' && estilo !== 'medieval' && estilo !== 'saiyan' && estilo !== 'noir' && estilo !== 'alchemy' && estilo !== 'minimal' && estilo !== 'invernadero') return;
            if (!(e.target instanceof Element)) return;

            const enlace = e.target.closest('#sidebar a[href$=".html"], .sidebar-nav a[href$=".html"]');
            if (!enlace) return;
            if (e.defaultPrevented) return;
            if (e.ctrlKey || e.metaKey || e.shiftKey || e.altKey || (e.button !== undefined && e.button !== 0)) return;

            const href = enlace.getAttribute('href');
            if (!href || href === '#') return;

            if (estilo === 'medieval') {
                // Transición heráldica del sello del castillo (mockup El Castillo).
                const heraldico = document.querySelector('.heraldic-transition');
                if (!heraldico) return;
                e.preventDefault();
                this._triggerHeraldico(() => { window.location.href = href; });
                return;
            }

            if (estilo === 'cyber') {
                // Transición de "acordeón neural": cierra los pliegues, cambia la
                // vista en el punto medio y luego abre los pliegues nuevamente.
                const accordion = document.querySelector('.cyber-blade-transition');
                if (!accordion) return;
                e.preventDefault();
                this._triggerAccordion(() => { window.location.href = href; });
                return;
            }

            if (estilo === 'invernadero') {
                // Cortina de círculo "hoja creciendo" (mockup invernadero).
                const leaf = document.querySelector('.invernadero-leaf-transition');
                if (!leaf) return;
                e.preventDefault();
                this._triggerLeaf(() => { window.location.href = href; });
                return;
            }

            const blade = document.querySelector(estilo === 'noir' ? '.noir-blade-transition' : estilo === 'alchemy' ? '.alchemy-blade-transition' : estilo === 'minimal' ? '.minimal-blade-transition' : '.saiyan-aura-transition');
            if (!blade) return;

            e.preventDefault();
            blade.classList.add('active');
            setTimeout(() => { window.location.href = href; }, estilo === 'noir' ? 340 : estilo === 'alchemy' ? 340 : estilo === 'minimal' ? 350 : 350);
        }, true);
    },

    /** Dispara la cortina de acordeón: cierra los pliegues, ejecuta onComplete
        en el punto medio (400 ms) y abre los pliegues nuevamente. */
    _triggerAccordion(onComplete) {
        const accordion = document.querySelector('.cyber-blade-transition');
        if (!accordion) {
            if (typeof onComplete === 'function') onComplete();
            return;
        }

        accordion.classList.add('active');

        setTimeout(() => {
            if (typeof onComplete === 'function') onComplete();
            accordion.classList.remove('active');
        }, 400);
    },

    /** Cortina de círculo "hoja creciendo" del invernadero: se abre un círculo
        de follaje, se cambia la vista en el pico y se cierra de nuevo. */
    _triggerLeaf(onComplete) {
        const leaf = document.querySelector('.invernadero-leaf-transition');
        if (!leaf) {
            if (typeof onComplete === 'function') onComplete();
            return;
        }
        leaf.classList.add('active');
        setTimeout(() => {
            if (typeof onComplete === 'function') onComplete();
            leaf.classList.remove('active');
        }, 320);
    },

    /** Dispara la cortina heráldica del sello: se abre un círculo desde un
        vértice superior y ejecuta onComplete en el punto medio, luego se limpia. */
    _triggerHeraldico(onComplete) {
        const container = document.querySelector('.heraldic-transition');
        if (!container) {
            if (typeof onComplete === 'function') onComplete();
            return;
        }

        container.classList.add('active');
        this._panCastilloZona();

        setTimeout(() => {
            if (typeof onComplete === 'function') onComplete();
        }, 260);

        setTimeout(() => {
            container.classList.remove('active');
        }, 620);
    },

    /* ------------------------------------------------------------
       Aplicación
       ------------------------------------------------------------ */

    /** Aplica un estilo visual y (por defecto) lo guarda como preferencia. */
    set(id, guardar = true) {
        const estilo = this.STYLES.some(s => s.id === id) ? id : 'clasico';
        const r = document.documentElement;
        r.setAttribute('data-estilo', estilo);

        if (estilo === 'clasico') {
            this._limpiarFondo();
            // El color de acento vuelve a manos de la personalización (js/colores.js).
            if (typeof Colores !== 'undefined') Colores.aplicar();
        } else {
            // El estilo define su propia paleta: libera las variables inline de
            // la personalización para que las del skin (estilos.css) manden.
            this._limpiarAccentoInline();
            this._ponFondo();
        }

        if (guardar) {
            try { localStorage.setItem(this.KEY, estilo); } catch (e) { /* almacenamiento no disponible */ }
        }
        document.dispatchEvent(new CustomEvent('estilos:cambio', { detail: { estilo } }));
        this._aplicarTextos();
        if (guardar) this._mostrarRecarga();
        return estilo;
    },

    /** Aplica los textos temáticos del skin activo sobre botones, enlaces y
        placeholders (coincidencia exacta e idempotente). */
    _aplicarTextos() {
        const mapa = this.TEXTOS_ESTILO[this.activo()];
        if (!mapa || !document.body) return;
        const normalizar = t => (t || '').replace(/\s+/g, ' ').trim();
        document.querySelectorAll('button, a').forEach(el => {
            const txt = normalizar(el.textContent);
            if (mapa[txt]) el.textContent = mapa[txt];
        });
        document.querySelectorAll('input, textarea').forEach(el => {
            const ph = el.getAttribute('placeholder');
            if (ph && mapa[ph]) el.setAttribute('placeholder', mapa[ph]);
        });
    },

    /** Aviso con botón de recarga al cambiar de estilo en caliente: evita que
        algunos elementos se queden con aspecto raro hasta el próximo full-load. */
    _mostrarRecarga() {
        if (document.querySelector('.estilos-recarga-overlay')) return;
        if (!document.body) {
            document.addEventListener('DOMContentLoaded', () => this._mostrarRecarga(), { once: true });
            return;
        }
        const acento = { clasico: '#3fb950', cyber: '#00f0ff', medieval: '#A9834B', saiyan: '#facc15', noir: '#e11d48', alchemy: '#10b981', minimal: '#2563eb', invernadero: '#3E6B4F' }[this.activo()] || '#3fb950';
        const ov = document.createElement('div');
        ov.className = 'estilos-recarga-overlay';
        ov.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.72);backdrop-filter:blur(6px);z-index:999999;display:flex;align-items:center;justify-content:center;padding:1rem;overflow:auto;';
        ov.innerHTML = `
            <div class="estilos-recarga-card" style="background:#161b22;border:2px solid ${acento};box-shadow:0 0 45px rgba(0,0,0,0.7);padding:30px 34px;max-width:430px;width:100%;max-height:calc(100vh - 2rem);overflow:auto;text-align:center;flex-shrink:0;margin:auto;">
                <div style="font-size:2.2rem;line-height:1;">⚡</div>
                <h3 style="margin:12px 0 8px;font-size:1.3rem;font-weight:900;color:#ffffff;">Estilo aplicado</h3>
                <p style="margin:0 0 20px;font-size:0.92rem;color:#8b949e;line-height:1.5;">Para que el nuevo estilo se aplique por completo y no se vea raro, recarga la página.</p>
                <div style="display:flex;gap:10px;justify-content:center;flex-wrap:wrap;">
                    <button type="button" class="est-recargar-btn" style="background:${acento};color:#0b1220;border:none;padding:11px 22px;font-weight:800;font-size:0.95rem;cursor:pointer;">Recargar ahora</button>
                    <button type="button" class="est-quedarme-btn" style="background:transparent;color:#c9d1d9;border:1px solid #30363d;padding:10px 18px;font-weight:600;font-size:0.9rem;cursor:pointer;">Quedarme aquí</button>
                </div>
            </div>`;
        document.body.appendChild(ov);

        const cerrar = () => { ov.remove(); };
        ov.querySelector('.est-recargar-btn').addEventListener('click', () => window.location.reload(), { once: true });
        ov.querySelector('.est-quedarme-btn').addEventListener('click', cerrar, { once: true });
        document.addEventListener('keydown', e => { if (e.key === 'Escape') cerrar(); }, { once: true });
    },

    /** Re-aplica los textos cuando se re-renderiza contenido dinámico
        (posts, modales, resultados de búsqueda...). */
    _vigilarTextos() {
        if (this._texVigila) return;
        this._texVigila = true;
        this._aplicarTextos();
        if (!('MutationObserver' in window)) return;
        if (!this.TEXTOS_ESTILO[this.activo()]) return;
        let timer = null;
        new MutationObserver(() => {
            clearTimeout(timer);
            timer = setTimeout(() => this._aplicarTextos(), 350);
        }).observe(document.body, { childList: true, subtree: true });
    },

    /** Aplica el estilo guardado al cargar la página (sin volver a guardar). */
    init() {
        this.set(this.guardado(), false);
        if (document.body) {
            this._vigilarTextos();
        } else {
            document.addEventListener('DOMContentLoaded', () => this._vigilarTextos(), { once: true });
        }
    }
};

/* Se aplica en cuanto se carga el script, igual que Tema y Colores (evita parpadeo). */
Estilos.init();