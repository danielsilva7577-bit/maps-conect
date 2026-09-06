/**
 * MAPS Connect - Mensajes
 * Las conversaciones y los mensajes se renderizan solo cuando el backend
 * (/mensajes y /mensajes/{id}) devuelve datos.
 */

const Mensajes = {
    state: {
        conversaciones: [],
        seleccionada: null,
        eventSource: null,
        adjunto: null
    },

    async init() {
        Layout.init('mensajes.html');
        Layout.setPageTitle('Mensajes');

        this.bindEventos();
        await this.loadConversaciones();

        // Si llegamos desde una notificación, abre la conversación indicada.
        const params = new URLSearchParams(window.location.search);
        const conv = params.get('conv');
        if (conv && this.state.conversaciones.some(c => String(this.field(c, 'id')) === String(conv))) {
            this.seleccionar(conv);
        }
    },

    bindEventos() {
        const list = document.getElementById('conversacion-list');
        list?.addEventListener('click', e => {
            const item = e.target.closest('.chat-item');
            if (item && item.dataset.id) this.seleccionar(item.dataset.id);
        });

        const search = document.getElementById('chat-search');
        search?.addEventListener('input', () => this.renderConversaciones());

        const body = document.getElementById('chat-body');
        body?.addEventListener('click', e => {
            const btn = e.target.closest('.adjunto-descargar');
            if (!btn) return;
            this.descargarAdjunto(btn.dataset.id, btn.dataset.nombre, btn.dataset.tipo);
        });

        document.getElementById('enviar-mensaje')?.addEventListener('click', () => this.enviar());
        document.getElementById('mensaje-input')?.addEventListener('keydown', e => {
            if (e.key === 'Enter') this.enviar();
        });
        document.getElementById('btn-adjuntar')?.addEventListener('click', () => {
            document.getElementById('adjunto-input')?.click();
        });

        const fileInput = document.getElementById('adjunto-input');
        fileInput?.addEventListener('change', () => {
            const archivo = fileInput.files && fileInput.files[0];
            if (!archivo) return;
            this.state.adjunto = archivo;
            this.renderAdjuntoPendiente();
            fileInput.value = '';
        });

        document.getElementById('adjunto-quitar')?.addEventListener('click',
            () => this.limpiarAdjunto());
    },

    renderAdjuntoPendiente() {
        const chip = document.getElementById('adjunto-pendiente');
        const nombre = document.getElementById('adjunto-pendiente-nombre');
        const archivo = this.state.adjunto;
        if (!chip || !nombre || !archivo) return;
        nombre.textContent = `${archivo.name} (${this.formatBytes(archivo.size)})`;
        chip.hidden = false;
    },

    limpiarAdjunto() {
        this.state.adjunto = null;
        const chip = document.getElementById('adjunto-pendiente');
        if (chip) chip.hidden = true;
    },

    async loadConversaciones() {
        try {
            const res = await API.request('/mensajes');
            this.state.conversaciones = this.toArray(res);
        } catch (e) {
            this.state.conversaciones = [];
        }
        this.renderConversaciones();
    },

    renderConversaciones() {
        const host = document.getElementById('conversacion-list');
        if (!host) return;

        const q = (document.getElementById('chat-search')?.value || '').trim().toLowerCase();
        const items = this.state.conversaciones.filter(c => {
            const nombre = this.nombreConv(c).toLowerCase();
            const preview = this.field(c, 'preview').toLowerCase();
            return !q || nombre.includes(q) || preview.includes(q);
        });

        if (!items.length) { host.innerHTML = ''; return; }

        host.innerHTML = items.map(c => {
            const id = this.field(c, 'id');
            const active = this.state.seleccionada && String(this.state.seleccionada) === String(id) ? ' active' : '';
            const profCls = this.esProf(c) ? ' prof' : '';
            const tiempo = this.field(c, 'tiempo') || this.field(c, 'fechaRelativa');
            const preview = this.field(c, 'preview');
            const nombre = this.nombreConv(c);
            return `
            <div class="chat-item${active}" data-id="${this.esc(id)}">
                ${Utils.avatarHtml(nombre, c.foto, `avatar${profCls}`, nombre)}
                <div class="chat-item-info">
                    <div class="chat-item-header">
                        <strong>${this.esc(nombre)}</strong>
                        ${tiempo ? `<span>${this.esc(tiempo)}</span>` : ''}
                    </div>
                    <p class="chat-item-preview">${this.esc(preview)}</p>
                </div>
            </div>`;
        }).join('');
    },

    async seleccionar(id) {
        this.state.seleccionada = id;
        this.cerrarStream();
        this.renderConversaciones();

        // Cabecera provisional con el dato de la lista; el detalle viene del backend.
        const conv = this.state.conversaciones.find(c => String(this.field(c, 'id')) === String(id));
        if (conv) this.renderCabecera(conv, true);

        this.setEntradaHabilitada(false);
        this.renderMensajes([]);

        let data;
        try {
            const res = await API.request(`/mensajes/${encodeURIComponent(id)}`);
            data = res && res.data != null ? res.data : res;
        } catch (e) {
            // El endpoint se implementará en el backend. La UI mantiene estados vacíos.
            data = null;
        }

        if (!data) return;

        // Refresca cabecera con el detalle completo si viene.
        this.renderCabecera(data, false);
        const mensajes = Array.isArray(data.mensajes) ? data.mensajes : (Array.isArray(data) ? data : []);
        this.renderMensajes(mensajes);
        this.setEntradaHabilitada(true);

        // Marca la conversación como leída para limpiar el badge de la campana.
        this.marcarLeido(id);

        // Conexión en tiempo real (SSE) a la conversación abierta.
        this.abrirStream(id);
    },

    cerrarStream() {
        if (this.state.eventSource) {
            this.state.eventSource.close();
            this.state.eventSource = null;
        }
    },

    abrirStream(id) {
        const token = localStorage.getItem('token');
        if (!token) return;

        const source = new EventSource(`${API_BASE_URL}/mensajes/${encodeURIComponent(id)}/stream?token=${encodeURIComponent(token)}`);
        source.addEventListener('mensaje', () => {
            if (String(this.state.seleccionada) !== String(id)) return;
            this.refrescarConversacion(id);
            this.marcarLeido(id);
            this.loadConversaciones();
            if (window.Layout && Layout.refreshNotificaciones) Layout.refreshNotificaciones();
        });
        // Sin cierre manual: EventSource reconecta automáticamente ante fallos transitorios.
        this.state.eventSource = source;
    },

    async refrescarConversacion(id) {
        if (String(this.state.seleccionada) !== String(id)) return;
        try {
            const res = await API.request(`/mensajes/${encodeURIComponent(id)}`);
            const data = res && res.data != null ? res.data : res;
            if (!data) return;
            this.renderCabecera(data, false);
            const mensajes = Array.isArray(data.mensajes) ? data.mensajes : [];
            this.renderMensajes(mensajes);
        } catch (e) {
            // Se ignora; el stream sigue activo y reintentará en el siguiente evento.
        }
    },

    renderCabecera(data, fromList) {
        const host = document.getElementById('chat-header');
        if (!host) return;
        const nombre = this.nombreConv(data);
        const profCls = this.esProf(data) ? ' prof' : '';
        const sub = this.field(data, 'subtitulo') || (fromList ? '' : '');
        const meId = Auth.getUser() && Auth.getUser().id;
        const idUsuario = data.idUsuario;
        const verPerfil = (idUsuario && String(idUsuario) !== String(meId))
            ? `<a class="btn-clean" href="usuario.html?id=${encodeURIComponent(idUsuario)}">Ver Perfil Académico</a>`
            : '';

        host.innerHTML = `
            <div class="chat-header-user">
                ${Utils.avatarHtml(nombre, data.foto, `avatar${profCls}`, nombre)}
                <div>
                    <h3>${this.esc(nombre)}</h3>
                    ${sub ? `<span>${this.esc(sub)}</span>` : ''}
                </div>
            </div>
            <div>${verPerfil}</div>
        `;
    },

    renderMensajes(mensajes) {
        const host = document.getElementById('chat-body');
        const empty = document.getElementById('chat-empty');
        if (!host) return;

        if (!mensajes.length) {
            host.innerHTML = '';
            if (empty) host.appendChild(empty);
            return;
        }

        if (empty) empty.remove();
        host.innerHTML = mensajes.map(m => this.messageEl(m)).join('');
        host.scrollTop = host.scrollHeight;
    },

    messageEl(m) {
        const texto = this.field(m, 'texto') || this.field(m, 'contenido');
        const tiempo = this.field(m, 'tiempo') || this.field(m, 'fechaRelativa');
        const enviado = m.enviado === true || m.enviado === 'true' || m.propio === true;
        const cls = enviado ? 'sent' : 'received';
        const adjunto = m.adjuntoNombre
            ? `
            <div class="message-attachment">
                <div class="attachment-info">
                    <span class="attachment-icon">${this.iconoAdjunto(m.adjuntoNombre)}</span>
                    <span>
                        <strong class="attachment-nombre">${this.esc(m.adjuntoNombre)}</strong>
                        <span class="attachment-peso">${m.adjuntoTamano ? this.formatBytes(m.adjuntoTamano) : ''}</span>
                    </span>
                </div>
                <button type="button" class="adjunto-descargar"
                    data-id="${m.id}" data-nombre="${this.esc(m.adjuntoNombre)}"
                    data-tipo="${this.esc(m.adjuntoTipo || '')}">
                    Descargar
                </button>
            </div>`
            : '';
        return `
        <div class="message ${cls}">
            ${adjunto}
            ${texto ? `<p class="message-texto">${this.esc(texto)}</p>` : ''}
            ${tiempo ? `<span class="message-time">${this.esc(tiempo)}</span>` : ''}
        </div>`;
    },

    async enviar() {
        const input = document.getElementById('mensaje-input');
        if (!input) return;
        const texto = input.value.trim();
        if ((!texto && !this.state.adjunto) || !this.state.seleccionada) return;

        try {
            if (this.state.adjunto) {
                await this.enviarAdjunto(texto);
            } else {
                await API.request(`/mensajes/${encodeURIComponent(this.state.seleccionada)}`, {
                    method: 'POST',
                    body: JSON.stringify({ texto })
                });
            }
            input.value = '';
            this.limpiarAdjunto();
            await this.seleccionar(this.state.seleccionada);
        } catch (e) {
            const msg = (e && e.data && e.data.message) || (e && e.message) || 'No se pudo enviar el mensaje';
            Utils.toast(msg, 'error');
        }
    },

    async enviarAdjunto(texto) {
        const token = localStorage.getItem('token');
        const formData = new FormData();
        formData.append('archivo', this.state.adjunto);
        if (texto) formData.append('texto', texto);

        const response = await fetch(`${API_BASE_URL}/mensajes/${encodeURIComponent(this.state.seleccionada)}/adjunto`, {
            method: 'POST',
            headers: token ? { Authorization: `Bearer ${token}` } : {},
            body: formData
        });
        const body = await response.json().catch(() => null);
        if (!response.ok) {
            const error = new Error((body && body.message) || `Error ${response.status}`);
            error.status = response.status;
            error.data = body;
            throw error;
        }
        return (body && body.data !== undefined) ? body.data : body;
    },

    marcarLeido(id) {
        if (!id) return;
        API.request(`/mensajes/${encodeURIComponent(id)}/leido`, { method: 'POST' }).catch(() => {
            // Si falla, el siguiente evento o selección lo reintentará.
        });
        if (window.Layout && Layout.refreshNotificaciones) Layout.refreshNotificaciones();
    },

    async descargarAdjunto(id, nombre, tipo) {
        const token = localStorage.getItem('token');
        try {
            const response = await fetch(`${API_BASE_URL}/mensajes/${encodeURIComponent(id)}/adjunto`, {
                headers: token ? { Authorization: `Bearer ${token}` } : {}
            });
            if (!response.ok) {
                Utils.toast('No se pudo descargar el archivo', 'error');
                return;
            }
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = nombre || 'adjunto';
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);
        } catch (e) {
            Utils.toast('No se pudo descargar el archivo', 'error');
        }
    },

    setEntradaHabilitada(on) {
        const input = document.getElementById('mensaje-input');
        const btn = document.getElementById('enviar-mensaje');
        if (input) input.disabled = !on;
        if (btn) btn.disabled = !on;
    },

    // ---- helpers ----
    nombreConv(c) {
        if (!c) return '';
        return this.field(c, 'nombre') || this.field(c, 'nombreCompleto') || (c.autor && (c.autor.nombre || c.autor.nombreCompleto)) || '';
    },

    esProf(c) {
        if (!c) return false;
        if (c.esProf === true || c.esProfesor === true || c.rol === 'DOCENTE' || c.rol === 'PROFESOR') return true;
        return false;
    },

    field(obj, key) {
        if (!obj) return '';
        return obj[key] != null ? String(obj[key]) : '';
    },

    toArray(res) {
        if (!res) return [];
        if (Array.isArray(res)) return res;
        if (Array.isArray(res.data)) return res.data;
        if (Array.isArray(res.contenido)) return res.contenido;
        return [];
    },

    esc(str) {
        return String(str == null ? '' : str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    formatBytes(bytes) {
        const n = Number(bytes);
        if (!isFinite(n) || n <= 0) return '';
        if (n < 1024) return `${n} B`;
        if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
        return `${(n / (1024 * 1024)).toFixed(1)} MB`;
    },

    iconoAdjunto(nombre) {
        const ext = String(nombre || '').split('.').pop().toLowerCase();
        if (['pdf'].includes(ext)) return 'PDF';
        if (['doc', 'docx'].includes(ext)) return 'DOC';
        if (['xls', 'xlsx', 'csv'].includes(ext)) return 'XLS';
        if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(ext)) return 'IMG';
        if (['zip', 'rar', '7z'].includes(ext)) return 'ZIP';
        return 'FILE';
    }
};

document.addEventListener('DOMContentLoaded', () => Mensajes.init());
