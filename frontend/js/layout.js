/**
 * MAPS Connect - Layout compartido (menú lateral plegable)
 */

const NAV_ITEMS = [
    { href: 'inicio.html', label: 'Inicio' },
    { href: 'perfil.html', label: 'Mi Perfil' },
    { href: 'certificados.html', label: 'Mi Ruta MAPS' },
    { href: 'comunidad.html', label: 'Comunidad y Recursos' },
    { href: 'mensajes.html', label: 'Mensajes' },
    { href: 'empresarial.html', label: 'Semestre Empresarial' },
    { href: 'circulos.html', label: 'Círculos de Estudio' },
    { href: 'ajustes.html', label: 'Ajustes' }
];

/**
 * Nombres personalizados del menú según el skin activo (data-estilo).
 * Los textos provienen de los prototipos entregados (mockups).
 */
const ETIQUETAS_ESTILO = {
    observatorio: {
        'inicio.html': 'Inicio',
        'perfil.html': 'Mi perfil',
        'certificados.html': 'Ruta MAPS',
        'comunidad.html': 'Comunidad',
        'mensajes.html': 'Mensajes',
        'empresarial.html': 'Aliados',
        'circulos.html': 'Círculos de estudio',
        'ajustes.html': 'Ajustes',
        'admin.html': 'Observatorio institucional',
        'docente.html': 'Observatorio docente'
    },
    biblioteca: {
        'inicio.html': 'Gran sal\u00f3n',
        'perfil.html': 'Rinc\u00f3n del lector',
        'certificados.html': 'Sala de los atlas',
        'comunidad.html': 'Estanter\u00eda compartida',
        'mensajes.html': 'Escritorio del escriba',
        'empresarial.html': 'Archivo de gremios',
        'circulos.html': 'Sala de lectura',
        'ajustes.html': 'Despacho del bibliotecario',
        'admin.html': '\u00cdndice institucional',
        'docente.html': 'Archivo docente'
    },
    saiyan: {
        'inicio.html': '01. Gran Dojo (Inicio)',
        'perfil.html': '02. Mi Perfil Saiyan',
        'certificados.html': '03. Ruta de Combate',
        'comunidad.html': '04. Comunidad Z-Fighters',
        'mensajes.html': '05. Telepatía / Chat',
        'empresarial.html': '06. Torneo Empresarial',
        'circulos.html': '07. Círculos de Ki',
        'ajustes.html': '08. Ajustes de Ki',
        'admin.html': '09. Mando Superior (Admin)',
        'docente.html': '09. Dojo Docente'
    },
    medieval: {
        'inicio.html': 'La Gran Aula (Inicio)',
        'perfil.html': 'Mi Pergamino (Perfil)',
        'certificados.html': 'Mapa del Hechizo (Ruta)',
        'comunidad.html': 'La Cámara de Sabios',
        'mensajes.html': 'Búhos y Pergaminos',
        'empresarial.html': 'Cámara del Tesoro',
        'circulos.html': 'Círculos de Magia',
        'ajustes.html': 'Cámara de los Consejos',
        'admin.html': 'Consejo de Administración',
        'docente.html': 'Gran Maestro (Docente)'
    },
    noir: {
        'inicio.html': '01. Comandancia (Inicio)',
        'perfil.html': '02. Expediente Personal',
        'certificados.html': '03. Pistas MAPS',
        'comunidad.html': '04. Archivo Central',
        'mensajes.html': '05. Notas Cifradas',
        'empresarial.html': '06. Sindicatos y Alianzas',
        'circulos.html': '07. Rondas de Estudio',
        'ajustes.html': '08. Ajustes de Caso',
        'admin.html': '09. Mando Superior (Admin)',
        'docente.html': '09. Comisaría Docente'
    },
    alchemy: {
        'inicio.html': '01. Círculo (Inicio)',
        'perfil.html': '02. Sello Personal',
        'certificados.html': '03. Leyes MAPS',
        'comunidad.html': '04. Códice Global',
        'mensajes.html': '05. Runas de Enlace',
        'empresarial.html': '06. Gremios Transmutadores',
        'circulos.html': '07. Círculos Esotéricos',
        'ajustes.html': '08. Sello y Ajustes',
        'admin.html': '09. Bóveda Alquímica (Admin)',
        'docente.html': '09. Gremio Docente'
    },
    minimal: {
        'inicio.html': '01. Panel Principal',
        'perfil.html': '02. Ficha Personal',
        'certificados.html': '03. Ruta Académica',
        'comunidad.html': '04. Red Global',
        'mensajes.html': '05. Mensajería',
        'empresarial.html': '06. Alianzas',
        'circulos.html': '07. Círculos de Estudio',
        'ajustes.html': '08. Configuración',
        'admin.html': '09. Control Institucional',
        'docente.html': '09. Asesoría Docente'
    },
    invernadero: {
        'inicio.html': 'Invernadero (Inicio)',
        'perfil.html': 'Mi Semillero (Perfil)',
        'certificados.html': 'Ruta de Cosecha (MAPS)',
        'comunidad.html': 'Comunidad de Cultivo',
        'mensajes.html': 'Búsquedas y Pollen',
        'empresarial.html': 'Invernadero Empresarial',
        'circulos.html': 'Círculos de Siembra',
        'ajustes.html': 'Riego y Ajustes',
        'admin.html': 'Rama de Administración',
        'docente.html': 'Brote Docente'
    },
    minimalista: {
        'inicio.html': 'Inicio',
        'perfil.html': 'Mi perfil',
        'certificados.html': 'Mi ruta MAPS',
        'comunidad.html': 'Comunidad',
        'mensajes.html': 'Mensajes',
        'empresarial.html': 'Empresarial',
        'circulos.html': 'Círculos de estudio',
        'ajustes.html': 'Ajustes',
        'admin.html': 'Administración',
        'docente.html': 'Docente'
    }
};

const Layout = {
    async init(currentPage) {
        if (!Auth.requireAuth()) return;
        if (await Auth.redirectIfPerfilIncompleto()) return;

        this.paginaActual = currentPage;
        this.renderSidebar(currentPage);
        this.renderTopbar();
        this.initNotificaciones();
        this.bindToggle();
        this.bindGlobalSearch();
        // Re-renderiza el menú al cambiar de skin (ajustes) sin recargar.
        document.addEventListener('estilos:cambio', () => this.renderSidebar(this.paginaActual));
    },

    /** Etiqueta del item según el skin activo; cae al nombre base si no aplica. */
    _etiqueta(item) {
        const estilo = document.documentElement.getAttribute('data-estilo') || 'clasico';
        const tabla = ETIQUETAS_ESTILO[estilo] || {};
        return tabla[item.href] || item.label;
    },

    renderSidebar(currentPage) {
        const sidebar = document.getElementById('sidebar');
        if (!sidebar) return;

        const user = Auth.getUser();
        const items = [...NAV_ITEMS];
        const rol = String(user?.rol || '').toUpperCase();
        if (rol === 'ADMINISTRADOR') {
            items.push({ href: 'admin.html', label: 'Panel Administrativo' });
        } else if (rol === 'PROFESOR') {
            items.push({ href: 'docente.html', label: 'Gestión Docente' });
        }
        const navLinks = items.map(item => {
            const active = item.href === currentPage ? 'active' : '';
            return `<li><a href="${item.href}" class="${active}">${Utils.esc(this._etiqueta(item))}</a></li>`;
        }).join('');

        sidebar.innerHTML = `
            <div class="sidebar-header">
                <h1>MAPS Connect</h1>
                <div class="sidebar-user">
                    ${Utils.avatarHtml(user?.nombre, user?.foto, 'avatar sidebar-avatar', user?.nombre || 'Usuario')}
                    <small>${user?.nombre || 'Usuario'}</small>
                </div>
            </div>
            <ul class="sidebar-nav">${navLinks}</ul>
            <div class="sidebar-footer">
                <button class="btn btn-sm btn-outline" id="btn-logout">Cerrar sesión</button>
            </div>
        `;

        document.getElementById('btn-logout')?.addEventListener('click', () => Auth.logout());
    },

    renderTopbar() {
        const topbar = document.getElementById('topbar');
        if (!topbar) return;

        topbar.innerHTML = `
            <button class="menu-backdrop" id="menu-backdrop" type="button" aria-label="Cerrar menú"></button>
            <button class="menu-toggle" id="sidebar-toggle" type="button"
                aria-label="Abrir menú" aria-controls="sidebar" aria-expanded="false">
                <span aria-hidden="true">☰</span>
            </button>
            <div class="global-search" role="search">
                <input class="global-search-input" id="global-search" type="search"
                    placeholder="Buscar personas, materias, apuntes, dudas... (Ctrl+K)" aria-label="Buscar en toda la plataforma" autocomplete="off">
                <div class="global-search-dropdown" id="global-search-dd" hidden></div>
            </div>
            <span id="topbar-title" class="visually-hidden"></span>
            <div class="notif-bell" id="notif-bell">
                <button type="button" class="notif-toggle" id="notif-toggle" aria-label="Notificaciones" aria-expanded="false">
                    <span aria-hidden="true">🔔</span>
                    <span class="notif-badge" id="notif-badge" hidden>0</span>
                </button>
                <div class="notif-panel" id="notif-panel" hidden>
                    <div class="notif-panel-head">
                        <strong>Notificaciones</strong>
                        <button type="button" class="notif-marcar-todas" id="notif-marcar-todas">Marcar todas leídas</button>
                    </div>
                    <div class="notif-list" id="notif-list"></div>
                </div>
            </div>
        `;
    },

    /**
     * Buscador global unificado: consulta GET /busqueda/global?q= y despliega
     * personas, materias, apuntes/recursos y dudas académicas en un menú unificado.
     */
    bindGlobalSearch() {
        const input = document.getElementById('global-search');
        const dd = document.getElementById('global-search-dd');
        if (!input || !dd) return;

        // Atajo de teclado: Ctrl+K o / para enfocar el buscador
        window.addEventListener('keydown', e => {
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
                e.preventDefault();
                input.focus();
                input.select();
            } else if (e.key === '/' && !['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)) {
                e.preventDefault();
                input.focus();
                input.select();
            }
        });

        let timer = null;
        input.addEventListener('input', () => {
            clearTimeout(timer);
            const q = input.value.trim();
            if (q.length < 2) {
                dd.hidden = true;
                dd.innerHTML = '';
                return;
            }
            timer = setTimeout(() => this.buscarGlobal(q, dd), 220);
        });

        input.addEventListener('focus', () => {
            if (input.value.trim().length >= 2 && dd.innerHTML) dd.hidden = false;
        });

        input.addEventListener('keydown', e => {
            if (e.key === 'Escape') {
                dd.hidden = true;
                input.blur();
            }
        });

        document.addEventListener('click', e => {
            if (!e.target.closest('.global-search')) dd.hidden = true;
        });
    },

    async buscarGlobal(q, dd) {
        let res;
        try {
            res = await API.request(`/busqueda/global?q=${encodeURIComponent(q)}`);
        } catch (e) {
            dd.hidden = true;
            return;
        }

        const data = (res && typeof res === 'object') ? (res.data || res) : {};
        const personas = Array.isArray(data.personas) ? data.personas : [];
        const materias = Array.isArray(data.materias) ? data.materias : [];
        const recursos = Array.isArray(data.recursos) ? data.recursos : [];
        const foro = Array.isArray(data.foro) ? data.foro : [];

        const totalResultados = personas.length + materias.length + recursos.length + foro.length;
        if (!totalResultados) {
            dd.innerHTML = `<div class="global-search-empty">Sin coincidencias para "${Utils.esc(q)}"</div>`;
            dd.hidden = false;
            return;
        }

        let html = '';

        // 1. Personas y Docentes
        if (personas.length) {
            html += `<div class="global-search-category">Personas y Docentes (${personas.length})</div>`;
            html += personas.map(p => {
                const sub = p.subtitulo || (p.rol === 'PROFESOR' ? 'Docente' : 'Estudiante');
                return `
                <div class="global-search-item" data-href="${Utils.esc(p.enlace || `usuario.html?id=${p.id}`)}" data-id="${Utils.esc(p.id)}">
                    ${Utils.avatarHtml(p.nombre, p.foto, 'avatar', p.nombre)}
                    <div class="global-search-info">
                        <strong>${Utils.esc(p.nombre)}</strong>
                        <span>${Utils.esc(sub)}</span>
                    </div>
                    <button class="global-search-chat" type="button" aria-label="Abrir chat">Chat</button>
                </div>`;
            }).join('');
        }

        // 2. Materias
        if (materias.length) {
            html += `<div class="global-search-category">Materias Académicas (${materias.length})</div>`;
            html += materias.map(m => `
                <div class="global-search-item" data-href="${Utils.esc(m.enlace || 'comunidad.html#foro-dudas')}">
                    <div class="global-search-icon">📘</div>
                    <div class="global-search-info">
                        <strong>${Utils.esc(m.nombre)}</strong>
                        <span>Ver publicaciones y recursos de esta materia</span>
                    </div>
                </div>
            `).join('');
        }

        // 3. Recursos / Apuntes
        if (recursos.length) {
            html += `<div class="global-search-category">Apuntes y Recursos (${recursos.length})</div>`;
            html += recursos.map(r => `
                <div class="global-search-item" data-href="${Utils.esc(r.enlace || 'comunidad.html#apuntes')}">
                    <div class="global-search-icon">📄</div>
                    <div class="global-search-info">
                        <strong>${Utils.esc(r.titulo)}</strong>
                        <span>${Utils.esc(r.materia || 'General')} • Subido por ${Utils.esc(r.autor || 'Estudiante')}</span>
                    </div>
                </div>
            `).join('');
        }

        // 4. Foro / Dudas
        if (foro.length) {
            html += `<div class="global-search-category">Dudas de la Comunidad (${foro.length})</div>`;
            html += foro.map(f => `
                <div class="global-search-item" data-href="${Utils.esc(f.enlace || 'comunidad.html#foro-dudas')}">
                    <div class="global-search-icon">❓</div>
                    <div class="global-search-info">
                        <strong>${Utils.esc(f.titulo)}</strong>
                        <span>${Utils.esc(f.materia || 'General')} • Preguntó ${Utils.esc(f.autor || 'Estudiante')}</span>
                    </div>
                </div>
            `).join('');
        }

        dd.innerHTML = html;
        dd.hidden = false;

        dd.querySelectorAll('.global-search-item').forEach(item => {
            item.addEventListener('click', e => {
                if (e.target.closest('.global-search-chat')) {
                    const id = item.dataset.id;
                    API.request(`/mensajes/nuevo/${encodeURIComponent(id)}`, { method: 'POST' })
                        .then(() => { window.location.href = 'mensajes.html'; })
                        .catch(() => { window.location.href = 'mensajes.html'; });
                    return;
                }
                const href = item.dataset.href;
                if (href) window.location.href = href;
            });
        });
    },

    bindToggle() {
        const toggle = document.getElementById('sidebar-toggle');
        const sidebar = document.getElementById('sidebar');
        const layout = document.querySelector('.app-layout');
        const backdrop = document.getElementById('menu-backdrop');

        const setMenuState = isOpen => {
            sidebar?.classList.toggle('open', isOpen);
            layout?.classList.toggle('menu-open', isOpen);
            toggle?.classList.toggle('open', isOpen);
            toggle.setAttribute('aria-expanded', String(isOpen));
            toggle.setAttribute('aria-label', isOpen ? 'Cerrar menú' : 'Abrir menú');
        };

        toggle?.addEventListener('click', () => setMenuState(!sidebar?.classList.contains('open')));
        backdrop?.addEventListener('click', () => setMenuState(false));
        document.addEventListener('keydown', event => {
            if (event.key === 'Escape' && sidebar?.classList.contains('open')) setMenuState(false);
        });
    },

    setPageTitle(title) {
        const el = document.getElementById('topbar-title');
        if (el) el.textContent = title;
        document.title = `${title} | MAPS Connect`;
    },

    // ---------- Notificaciones (campana global) ----------

    initNotificaciones() {
        const bell = document.getElementById('notif-bell');
        if (!bell) return;

        this.notif = this.notif || { 
            refreshedAt: 0, 
            panelAbierto: false, 
            eventSource: null,
            reconnectAttempts: 0,
            reconnectTimer: null
        };

        document.getElementById('notif-toggle')?.addEventListener('click', e => {
            e.stopPropagation();
            this.notif.panelAbierto = !this.notif.panelAbierto;
            document.getElementById('notif-panel').hidden = !this.notif.panelAbierto;
            document.getElementById('notif-toggle').setAttribute('aria-expanded', String(this.notif.panelAbierto));
            if (this.notif.panelAbierto) this.refreshNotificaciones();
        });

        document.addEventListener('click', e => {
            if (this.notif.panelAbierto && !e.target.closest('#notif-bell')) {
                this.notif.panelAbierto = false;
                document.getElementById('notif-panel').hidden = true;
                document.getElementById('notif-toggle').setAttribute('aria-expanded', 'false');
            }
        });

        document.getElementById('notif-marcar-todas')?.addEventListener('click', async () => {
            try {
                await API.request('/notificaciones/marcar-todas', { method: 'POST' });
            } catch (e) {
                // Se ignora; el siguiente refresco regulara el badge.
            }
            this.refreshNotificaciones();
        });

        // Canal SSE en vivo: toast + refresco inmediato al llegar una notificación.
        // Incluye reconexión automática con backoff exponiencial (2s→4s→8s→…→60s).
        const token = Auth.getToken();
        if (token) {
            this._conectarNotificacionesSSE(token);
        }

        // Registro de Service Worker para notificaciones nativas en segundo plano
        if ('serviceWorker' in navigator) {
            const swPath = window.location.pathname.startsWith('/api')
                ? '/api/sw.js'
                : (window.location.pathname.includes('/pages/') ? '../sw.js' : './sw.js');
            navigator.serviceWorker.register(swPath).catch(() => {});
        }

        // Solicitud no intrusiva de permisos para notificaciones del navegador
        if ('Notification' in window && Notification.permission === 'default') {
            const pedirPermiso = () => {
                Notification.requestPermission().catch(() => {});
                document.removeEventListener('click', pedirPermiso);
            };
            document.addEventListener('click', pedirPermiso, { once: true });
        }

        this.refreshNotificaciones();
        setInterval(() => this.refreshNotificaciones(), 5000);
    },

    /**
     * Abre el canal SSE de notificaciones con reconexión automática.
     * Si una conexión previa existe, se cierra antes de abrir una nueva
     * para evitar fugas de listeners y emitters duplicados.
     */
    _conectarNotificacionesSSE(token) {
        // Cierra conexión SSE anterior si existe
        if (this.notif.eventSource) {
            this.notif.eventSource.close();
            this.notif.eventSource = null;
        }

        // Cancela timeout de reconexión pendiente (si lo hay)
        if (this.notif.reconnectTimer) {
            clearTimeout(this.notif.reconnectTimer);
            this.notif.reconnectTimer = null;
        }

        const source = new EventSource(`${API_BASE_URL}/notificaciones/stream?token=${encodeURIComponent(token)}`);

        source.addEventListener('mensaje', e => {
            try {
                const dato = JSON.parse(e.data);
                this.notifRecibida(dato);
            } catch (err) {
                // Evento malformado; se ignora.
            }
        });
        ['foro', 'duda', 'sesion', 'asesoria', 'general'].forEach(tipo => {
            source.addEventListener(tipo, e => {
                try {
                    const dato = JSON.parse(e.data);
                    this.notifRecibida(dato);
                } catch (err) {
                    // Evento malformado; se ignora.
                }
            });
        });

        // Reconexión con backoff exponiencial (2s, 4s, 8s, 16s, 32s → máx 60s)
        source.onerror = () => {
            source.close();

            // No reconectar si el usuario cerró sesión o el token fue borrado
            if (!Auth.getToken()) return;

            const intento = (this.notif.reconnectAttempts || 0) + 1;
            this.notif.reconnectAttempts = intento;
            const delay = Math.min(2000 * Math.pow(2, intento - 1), 60000);

            this.notif.reconnectTimer = setTimeout(() => {
                this.notif.reconnectTimer = null;
                this._conectarNotificacionesSSE(token);
            }, delay);
        };

        // Reinicia el contador de intentos al reconectar exitosamente
        source.onopen = () => {
            this.notif.reconnectAttempts = 0;
        };

        this.notif.eventSource = source;
    },

    /** Cierra el canal SSE de notificaciones (llamar al hacer logout). */
    _cerrarNotificacionesSSE() {
        if (this.notif && this.notif.eventSource) {
            this.notif.eventSource.close();
            this.notif.eventSource = null;
        }
        if (this.notif && this.notif.reconnectTimer) {
            clearTimeout(this.notif.reconnectTimer);
            this.notif.reconnectTimer = null;
        }
    },

    notifRecibida(notif) {
        if (!notif || !notif.tipo) return;

        // En la página de mensajes el chat abierto ya se actualiza por su propio stream;
        // no mostramos toast para no duplicar.
        const enMensajes = window.location.pathname.includes('mensajes.html');

        if (notif.tipo === 'mensaje') {
            if (!enMensajes) {
                const nombre = notif.emisorNombre || 'Alguien';
                const texto = (notif.mensaje && (notif.mensaje.texto || notif.mensaje.contenido)) || '';
                const preview = texto ? `: ${texto}` : ' te envió un adjunto';
                Utils.toast(`Nuevo mensaje de ${nombre}${preview}`, 'info');
            } else {
                window.dispatchEvent(new CustomEvent('nuevo-mensaje-global', { detail: notif }));
            }
        } else {
            // Otras áreas: foro, dudas, sesiones, asesorías, etc.
            const etiquetas = {
                foro: 'Foro',
                duda: 'Duda',
                sesion: 'Sesión',
                asesoria: 'Asesoría',
                general: 'Notificación'
            };
            const etiqueta = etiquetas[notif.tipo] || 'Notificación';
            const preview = notif.preview ? `: ${notif.preview}` : '';
            Utils.toast(`${etiqueta} • ${notif.titulo || 'Novedad'}${preview}`, 'info');
        }
        this.dispararPushNotification(notif);
        this.refreshNotificaciones();
    },

    /** Emite notificación nativa del navegador/sistema si la pestaña está oculta/minimizada */
    dispararPushNotification(notif) {
        if (!('Notification' in window) || Notification.permission !== 'granted') return;
        if (!document.hidden) return;

        let titulo = 'MAPS Connect';
        let cuerpo = 'Tienes una nueva notificación';
        let enlace = notif.enlace || 'inicio.html';

        if (notif.tipo === 'mensaje') {
            titulo = `Mensaje de ${notif.emisorNombre || 'Usuario'}`;
            cuerpo = (notif.mensaje && (notif.mensaje.texto || notif.mensaje.contenido)) || 'Te ha enviado un archivo adjunto';
            enlace = `mensajes.html?conv=${notif.conversacionId || ''}`;
        } else {
            titulo = notif.titulo || 'Actualización en MAPS Connect';
            cuerpo = notif.preview || 'Revisa las novedades en la plataforma';
        }

        try {
            if (navigator.serviceWorker && navigator.serviceWorker.controller) {
                navigator.serviceWorker.ready.then(reg => {
                    reg.showNotification(titulo, {
                        body: cuerpo,
                        icon: '/img/icon-192.png',
                        data: { enlace }
                    });
                });
            } else {
                const n = new Notification(titulo, {
                    body: cuerpo,
                    icon: '/img/icon-192.png'
                });
                n.onclick = () => {
                    window.focus();
                    if (enlace) window.location.href = enlace;
                    n.close();
                };
            }
        } catch (_) {}
    },

    /** Recarga badge + panel. Útil también para llamarlo desde otras páginas. */
    async refreshNotificaciones() {
        const badge = document.getElementById('notif-badge');
        const list = document.getElementById('notif-list');
        if (!badge || !list) return;

        let resumen;
        try {
            resumen = await API.request('/notificaciones/resumen');
        } catch (e) {
            return;
        }

        const noLeidos = Number(resumen?.noLeidos) || 0;
        if (noLeidos > 0) {
            badge.hidden = false;
            badge.textContent = noLeidos > 99 ? '99+' : String(noLeidos);
        } else {
            badge.hidden = true;
        }

        const items = Array.isArray(resumen?.items) ? resumen.items : [];
        if (!items.length) {
            list.innerHTML = '<div class="notif-vacio">Sin notificaciones.</div>';
            return;
        }

        list.innerHTML = items.map(item => {
            const unread = Number(item.noLeidos) || 0;
            const sub = [unread > 0 ? `${unread} nuevo${unread === 1 ? '' : 's'}` : 'Sin novedades', item.tiempo].filter(Boolean).join(' • ');
            return `
            <a class="notif-item" href="${Utils.esc(item.enlace || 'pages/mensajes.html')}">
                ${Utils.avatarHtml(item.nombre, item.foto, 'avatar' + (item.esProf ? ' prof' : ''), item.nombre)}
                <div class="notif-item-info">
                    <strong>${Utils.esc(item.nombre)}</strong>
                    <span class="notif-preview">${Utils.esc(item.preview || '')}</span>
                    <span>${Utils.esc(sub)}</span>
                </div>
                ${unread > 0 ? `<span class="notif-item-badge">${unread}</span>` : ''}
            </a>`;
        }).join('');
    }
};
