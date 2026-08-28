/**
 * MAPS Connect - Mensajes
 * Las conversaciones y los mensajes se renderizan solo cuando el backend
 * (/mensajes y /mensajes/{id}) devuelve datos.
 */

const Mensajes = {
    state: {
        conversaciones: [],
        seleccionada: null
    },

    async init() {
        Layout.init('mensajes.html');
        Layout.setPageTitle('Mensajes');

        this.bindEventos();
        await this.loadConversaciones();
    },

    bindEventos() {
        const list = document.getElementById('conversacion-list');
        list?.addEventListener('click', e => {
            const item = e.target.closest('.chat-item');
            if (item && item.dataset.id) this.seleccionar(item.dataset.id);
        });

        const search = document.getElementById('chat-search');
        search?.addEventListener('input', () => this.renderConversaciones());

        document.getElementById('enviar-mensaje')?.addEventListener('click', () => this.enviar());
        document.getElementById('mensaje-input')?.addEventListener('keydown', e => {
            if (e.key === 'Enter') this.enviar();
        });
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
            const initials = this.initials(this.nombreConv(c));
            const profCls = this.esProf(c) ? ' prof' : '';
            const tiempo = this.field(c, 'tiempo') || this.field(c, 'fechaRelativa');
            const preview = this.field(c, 'preview');
            return `
            <div class="chat-item${active}" data-id="${this.esc(id)}">
                <div class="avatar${profCls}">${this.esc(initials)}</div>
                <div class="chat-item-info">
                    <div class="chat-item-header">
                        <strong>${this.esc(this.nombreConv(c))}</strong>
                        ${tiempo ? `<span>${this.esc(tiempo)}</span>` : ''}
                    </div>
                    <p class="chat-item-preview">${this.esc(preview)}</p>
                </div>
            </div>`;
        }).join('');
    },

    async seleccionar(id) {
        this.state.seleccionada = id;
        this.renderConversaciones();

        // Cabecera provisional con el dato de la lista; el detalle viene del backend.
        const conv = this.state.conversaciones.find(c => String(this.field(c, 'id')) === String(id));
        if (conv) this.renderCabecera(conv, true);

        this.setEntradaHabilitada(false);
        this.renderMensajes([]);

        let data;
        try {
            data = await API.request(`/mensajes/${encodeURIComponent(id)}`);
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
    },

    renderCabecera(data, fromList) {
        const host = document.getElementById('chat-header');
        if (!host) return;
        const nombre = this.nombreConv(data);
        const initials = this.initials(nombre);
        const profCls = this.esProf(data) ? ' prof' : '';
        const sub = this.field(data, 'subtitulo') || (fromList ? '' : '');

        host.innerHTML = `
            <div class="chat-header-user">
                <div class="avatar${profCls}">${this.esc(initials)}</div>
                <div>
                    <h3>${this.esc(nombre)}</h3>
                    ${sub ? `<span>${this.esc(sub)}</span>` : ''}
                </div>
            </div>
            <div>
                <button class="btn-clean" type="button">Ver Perfil Académico</button>
            </div>
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
        return `
        <div class="message ${cls}">
            ${this.esc(texto)}
            ${tiempo ? `<span class="message-time">${this.esc(tiempo)}</span>` : ''}
        </div>`;
    },

    async enviar() {
        const input = document.getElementById('mensaje-input');
        if (!input) return;
        const texto = input.value.trim();
        if (!texto || !this.state.seleccionada) return;

        try {
            await API.request(`/mensajes/${encodeURIComponent(this.state.seleccionada)}`, {
                method: 'POST',
                body: JSON.stringify({ texto })
            });
            input.value = '';
            // Refrescar la conversación para obtener el mensaje persistido.
            await this.seleccionar(this.state.seleccionada);
        } catch (e) {
            // El endpoint aún no existe en el backend. No se limpia el input.
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

    initials(name) {
        if (!name) return '?';
        const parts = String(name).trim().split(/\s+/);
        const first = parts[0] ? parts[0][0] : '';
        const second = parts[1] ? parts[1][0] : '';
        return (first + second).toUpperCase() || '?';
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
    }
};

document.addEventListener('DOMContentLoaded', () => Mensajes.init());
