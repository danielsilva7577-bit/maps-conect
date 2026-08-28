/**
 * MAPS Connect - Comunidad y Recursos (Foro + Apuntes + Tips)
 * Las tarjetas solo se renderizan cuando el backend devuelve datos.
 */

const Comunidad = {
    state: {
        foro: [],
        apuntes: [],
        tips: [],
        loaded: new Set(),
        activeTab: 'foro-dudas'
    },

    init() {
        Layout.init('comunidad.html');
        Layout.setPageTitle('Comunidad y Recursos');

        this.bindTabs();
        this.bindFilters();

        // Permite enlazar directo a una pestaña: comunidad.html#foro-dudas
        const hash = window.location.hash.replace('#', '');
        const startTab = ['foro-dudas', 'apuntes', 'tips'].includes(hash) ? hash : 'foro-dudas';
        const btn = document.querySelector(`.tab-button[data-tab="${startTab}"]`);
        this.switchTab(startTab, btn ? btn.dataset.action : '+ Preguntar Duda');
    },

    bindTabs() {
        const tabs = document.querySelectorAll('.tab-button');
        tabs.forEach(btn => {
            btn.addEventListener('click', () => {
                this.switchTab(btn.dataset.tab, btn.dataset.action);
            });
        });
    },

    bindFilters() {
        const search = document.getElementById('hub-search');
        const materia = document.getElementById('hub-materia');
        search?.addEventListener('input', () => this.renderActive());
        materia?.addEventListener('change', () => this.renderActive());
    },

    switchTab(tabId, actionText) {
        this.state.activeTab = tabId;

        document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
        document.querySelectorAll('.tab-button').forEach(b => b.classList.remove('active'));

        document.getElementById(tabId)?.classList.add('active');
        document.querySelector(`.tab-button[data-tab="${tabId}"]`)?.classList.add('active');

        const actionBtn = document.getElementById('context-action-btn');
        if (actionBtn) actionBtn.textContent = actionText;

        this.loadTab(tabId);
    },

    async loadTab(tabId) {
        const map = {
            'foro-dudas': { key: 'foro', loader: () => this.loadForo() },
            'apuntes': { key: 'apuntes', loader: () => this.loadApuntes() },
            'tips': { key: 'tips', loader: () => this.loadTips() }
        };
        const conf = map[tabId];
        if (!conf) return;
        if (this.state.loaded.has(conf.key)) {
            this.renderActive();
            return;
        }
        await conf.loader();
    },

    async loadForo() {
        try {
            const res = await API.request('/foro');
            this.state.foro = this.toArray(res);
            this.state.loaded.add('foro');
        } catch (e) {
            this.state.foro = [];
        }
        this.renderActive();
    },

    async loadApuntes() {
        try {
            const res = await API.request('/recursos');
            this.state.apuntes = this.toArray(res);
            this.state.loaded.add('apuntes');
        } catch (e) {
            this.state.apuntes = [];
        }
        this.renderActive();
    },

    async loadTips() {
        try {
            const res = await API.request('/tips');
            this.state.tips = this.toArray(res);
            this.state.loaded.add('tips');
        } catch (e) {
            this.state.tips = [];
        }
        this.renderActive();
    },

    renderActive() {
        const q = (document.getElementById('hub-search')?.value || '').trim().toLowerCase();
        const m = document.getElementById('hub-materia')?.value || '';

        const match = item => {
            const text = `${this.field(item, 'titulo')} ${this.field(item, 'descripcion')} ${this.field(item, 'texto')}`.toLowerCase();
            const materia = this.materiaNombre(item).toLowerCase();
            return (!q || text.includes(q)) && (!m || materia === m);
        };

        switch (this.state.activeTab) {
            case 'foro-dudas':
                this.renderForo(this.state.foro.filter(match), 'foro-dudas');
                break;
            case 'apuntes':
                this.renderApuntes(this.state.apuntes.filter(match), 'apuntes');
                break;
            case 'tips':
                this.renderTips(this.state.tips.filter(match), 'tips');
                break;
        }
    },

    renderForo(items, panelId) {
        const panel = document.getElementById(panelId);
        if (!panel) return;
        panel.innerHTML = '';
        if (!items.length) return;
        panel.innerHTML = items.map(item => this.foroCard(item)).join('');
    },

    renderApuntes(items, panelId) {
        const panel = document.getElementById(panelId);
        if (!panel) return;
        panel.innerHTML = '';
        if (!items.length) return;
        panel.innerHTML = items.map(item => this.apunteCard(item)).join('');
    },

    renderTips(items, panelId) {
        const panel = document.getElementById(panelId);
        if (!panel) return;
        panel.innerHTML = '';
        if (!items.length) return;
        panel.innerHTML = items.map(item => this.tipCard(item)).join('');
    },

    foroCard(item) {
        const titulo = this.field(item, 'titulo');
        const desc = this.field(item, 'descripcion');
        const materia = this.materiaNombre(item);
        const autor = this.autorNombre(item);
        const tiempo = this.field(item, 'tiempo') || this.field(item, 'haceTiempo') || '';
        const votos = this.num(item, 'votos');
        const respuestas = this.num(item, 'respuestas') || this.num(item, 'numRespuestas');
        const resuelto = item.resuelto === true || item.resuelto === 'true';
        const solucion = this.field(item, 'solucion');

        const badges = [
            materia ? `<span class="hub-badge badge-materia">${this.esc(materia)}</span>` : '',
            resuelto ? `<span class="hub-badge badge-solved">✓ Solución Aceptada</span>` : ''
        ].join('');

        const solucionBox = (resuelto && solucion)
            ? `<div class="solved-box"><strong>Solución validada por el autor:</strong><p>${this.esc(solucion)}</p></div>`
            : '';

        const metaTiempo = tiempo ? `Por: ${this.esc(autor)} • ${this.esc(tiempo)}` : `Por: ${this.esc(autor)}`;
        const accionRespuesta = resuelto
            ? `<button class="btn-link">💬 ${respuestas} Respuestas</button>`
            : `<button class="btn-link" style="color:#005a2b;font-weight:bold;">✍ Aportar Respuesta</button>`;

        return `
        <article class="card">
            <header class="post-header">
                <div style="display:flex;gap:6px;flex-wrap:wrap;">${badges}</div>
                <span class="post-meta">${this.esc(metaTiempo)}</span>
            </header>
            <h3 class="post-title">${this.esc(titulo)}</h3>
            <p class="post-desc">${this.esc(desc)}</p>
            ${solucionBox}
            <footer class="post-footer">
                <div class="post-actions">
                    <button class="btn-link">▲ ${votos} Votos</button>
                    ${accionRespuesta}
                    <button class="btn-link">🔖 Guardar</button>
                </div>
                <span>${resuelto ? 'Última aportación reciente' : 'Esperando solución'}</span>
            </footer>
        </article>`;
    },

    apunteCard(item) {
        const titulo = this.field(item, 'titulo');
        const materia = this.materiaNombre(item);
        const autor = this.autorNombre(item);
        const tipo = this.field(item, 'tipo') || 'PDF';
        const tamano = this.field(item, 'tamano') || this.field(item, 'tamanio') || '';
        const descargas = this.num(item, 'descargas');
        const rating = this.field(item, 'rating') || '';
        const url = this.field(item, 'url') || this.field(item, 'archivoUrl') || '#';

        return `
        <article class="card apunte-card">
            <div class="apunte-info">
                <div class="file-icon">${this.esc(tipo)}</div>
                <div class="apunte-details">
                    <h4>${this.esc(titulo)}</h4>
                    <p>Materia: <strong>${this.esc(materia)}</strong> • Subido por: <strong>${this.esc(autor)}</strong></p>
                    <p style="margin-top:4px;color:#888;">${tamano ? `Tamaño: ${this.esc(tamano)} • ` : ''}${descargas} descargas${rating ? ` • ${this.esc(rating)} ★` : ''}</p>
                </div>
            </div>
            <button class="btn-solid" type="button" data-href="${this.esc(url)}">⬇ Descargar Documento</button>
        </article>`;
    },

    tipCard(item) {
        const titulo = this.field(item, 'titulo');
        const texto = this.field(item, 'texto') || this.field(item, 'descripcion');
        const materia = this.materiaNombre(item);
        const autor = this.autorNombre(item);
        const votos = this.num(item, 'votos');
        const verificado = item.verificado === true || item.verificado === 'true';

        const verified = verificado
            ? `<span class="hub-badge badge-verified">✓ Verificado por Docente</span>`
            : '';

        return `
        <article class="card tip-card">
            <div>
                <div class="tip-head">
                    <span class="hub-badge badge-materia">${this.esc(materia)}</span>
                    ${verified}
                </div>
                <h4 class="tip-title">${this.esc(titulo)}</h4>
                <p class="tip-text">${this.esc(texto)}</p>
            </div>
            <div class="post-footer">
                <span class="vote-badge">▲ ${votos} votos de utilidad</span>
                <span style="font-size:0.78rem;color:#888;">Por: ${this.esc(autor)}</span>
            </div>
        </article>`;
    },

    // ---- helpers ----
    field(obj, key) {
        if (!obj) return '';
        return obj[key] != null ? String(obj[key]) : '';
    },

    num(obj, key) {
        const v = obj && obj[key];
        const n = Number(v);
        return Number.isFinite(n) ? n : 0;
    },

    materiaNombre(item) {
        if (!item) return '';
        if (typeof item.materia === 'string') return item.materia;
        if (item.materia && typeof item.materia === 'object') return item.materia.nombre || item.materia.nom || '';
        if (item.materiaNombre) return item.materiaNombre;
        return 'General';
    },

    autorNombre(item) {
        if (!item) return 'Anónimo';
        if (typeof item.autor === 'string') return item.autor;
        if (item.autor && typeof item.autor === 'object') return item.autor.nombre || item.autor.nom || 'Anónimo';
        if (item.autorNombre) return item.autorNombre;
        return 'Anónimo';
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

document.addEventListener('DOMContentLoaded', () => Comunidad.init());
