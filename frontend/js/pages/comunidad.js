/**
 * MAPS Connect - Comunidad y Recursos (Foro + Apuntes + Tips)
 * Las tarjetas solo se renderizan cuando el backend devuelve datos.
 */

const Comunidad = {
    state: {
        foro: [],
        apuntes: [],
        tips: [],
        materiasModal: [],
        loaded: new Set(),
        activeTab: 'foro-dudas'
    },

    async init() {
        Layout.init('comunidad.html');
        Layout.setPageTitle('Comunidad y Recursos');

        this.bindTabs();
        this.bindFilters();
        this.bindActionButton();
        this.bindDocument();

        // Permite enlazar directo a una pestaña: comunidad.html#foro-dudas
        const hash = window.location.hash.replace('#', '');
        const startTab = ['foro-dudas', 'apuntes', 'tips'].includes(hash) ? hash : 'foro-dudas';
        const btn = document.querySelector(`.tab-button[data-tab="${startTab}"]`);
        this.switchTab(startTab, btn ? btn.dataset.action : '+ Preguntar Duda');

        await this.cargarMateriasSelect();
    },

    async cargarMateriasSelect() {
        const select = document.getElementById('hub-materia');
        let materias = [];
        let esSemestre = false;
        let carrera = '';
        try {
            const res = await API.request('/inicio');
            const data = res && typeof res === 'object' ? res : {};
            materias = Array.isArray(data.materias) ? data.materias : [];
            esSemestre = materias.length > 0;
            carrera = (data.perfil && data.perfil.carrera) ? data.perfil.carrera : '';
        } catch (e) {
            materias = [];
        }
        if (!materias.length) {
            // Maestro o estudiante sin onboarding: usa todo el catálogo.
            try {
                const res = await API.request('/materias');
                materias = Array.isArray(res) ? res : (res && Array.isArray(res.data) ? res.data : []);
            } catch (e) {
                materias = [];
            }
        }
        this.state.materiasModal = materias;

        const note = document.getElementById('career-note');
        if (note) {
            if (carrera) {
                note.hidden = false;
                note.innerHTML = `Mostrando solo contenido de tu carrera: <strong>${this.esc(carrera)}</strong>`;
            } else {
                note.hidden = true;
            }
        }

        if (select) {
            const actual = select.value;
            const etiqueta = esSemestre ? 'Todas las materias del semestre' : 'Todas las materias';
            select.innerHTML = `<option value="">${this.esc(etiqueta)}</option>` + materias.map(m => {
                const nombre = this.materiaNombre(m);
                return `<option value="${this.esc(nombre)}">${this.esc(nombre)}</option>`;
            }).join('');
            select.value = actual;
        }

        this.refrescarSelectsMateria();
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

    bindActionButton() {
        const btn = document.getElementById('context-action-btn');
        btn?.addEventListener('click', () => {
            const tabId = this.state.activeTab || 'foro-dudas';
            this.abrirModal(tabId);
        });
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
            'foro-dudas': {key: 'foro', loader: () => this.loadForo()},
            'apuntes': {key: 'apuntes', loader: () => this.loadApuntes()},
            'tips': {key: 'tips', loader: () => this.loadTips()}
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

    bindVotes() {
        const panel = document.getElementById('tips');
        if (!panel) return;
        panel.querySelectorAll('.vote-btn').forEach(btn => {
            btn.addEventListener('click', () => this.votarTip(btn.getAttribute('data-id'), btn.getAttribute('data-votado') === 'true'));
        });
    },

    bindDocument() {
        document.addEventListener('click', e => {
            const followBtn = e.target.closest('.follow-btn');
            if (followBtn) {
                e.preventDefault();
                const id = followBtn.getAttribute('data-id');
                if (id) Utils.alternarSeguir(followBtn, id);
                return;
            }

            const downBtn = e.target.closest('.btn-solid[data-href]');
            if (downBtn) {
                e.preventDefault();
                window.open(downBtn.getAttribute('data-href'), '_blank', 'noopener');
                return;
            }

            const recursoBtn = e.target.closest('.apunte-descargar');
            if (recursoBtn) {
                e.preventDefault();
                this.descargarRecurso(recursoBtn.getAttribute('data-id'), recursoBtn.getAttribute('data-nombre'));
                return;
            }

            const foroBtn = e.target.closest('.foro-action');
            if (foroBtn) {
                e.preventDefault();
                const accion = foroBtn.getAttribute('data-action');
                if (accion === 'voto') {
                    Utils.toast('Los votos del foro se habilitan cuando el backend de publicaciones esté integrado.', 'info');
                } else if (accion === 'guardar') {
                    Utils.toast('El guardado se habilita cuando el backend de comunidad esté integrado.', 'info');
                } else {
                    Utils.toast('Para aportar una respuesta, abre la publicación cuando el detalle esté disponible.', 'info');
                }
            }
        });
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
        this.bindVotes();
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

        const autorObj = item.autor && typeof item.autor === 'object' ? item.autor : null;
        const autorId = item.autorId || (autorObj ? autorObj.id : null);
        const autorFoto = item.autorFoto || (autorObj ? autorObj.foto : null);
        const loSigo = item.siguiendo === true || item.siguiendo === 'true';
        const meId = Auth.getUser() && Auth.getUser().id;
        const clickeable = autorId && String(autorId) !== String(meId);
        const hrefAutor = `usuario.html?id=${encodeURIComponent(autorId)}`;
        const avatarWrap = clickeable
            ? `<a class="user-link" href="${hrefAutor}" aria-label="Ver perfil de ${this.esc(autor)}">${Utils.avatarHtml(autor, autorFoto, 'hub-avatar', autor)}</a>`
            : Utils.avatarHtml(autor, autorFoto, 'hub-avatar', autor);
        const nombreWrap = clickeable
            ? `<a class="user-link" href="${hrefAutor}"><strong>${this.esc(autor)}</strong></a>`
            : `<strong>${this.esc(autor)}</strong>`;
        const followBtn = (autorId && String(autorId) !== String(meId))
            ? `<button class="follow-btn${loSigo ? ' siguiendo' : ''}" type="button" data-id="${this.esc(autorId)}" data-siguiendo="${loSigo ? 'true' : 'false'}">${loSigo ? 'Siguiendo' : 'Seguir'}</button>`
            : '';

        const badges = [
            materia ? `<span class="hub-badge badge-materia">${this.esc(materia)}</span>` : '',
            resuelto ? `<span class="hub-badge badge-solved">Solución Aceptada</span>` : ''
        ].join('');

        const solucionBox = (resuelto && solucion)
            ? `<div class="solved-box"><strong>Solución validada por el autor:</strong><p>${this.esc(solucion)}</p></div>`
            : '';

        const accionRespuesta = resuelto
            ? `<button class="btn-link foro-action" data-action="respuesta">${respuestas} Respuestas</button>`
            : `<button class="btn-link foro-action" data-action="respuesta" style="color:#005a2b;font-weight:bold;">Aportar Respuesta</button>`;

        return `
        <article class="card">
            <header class="post-header">
                <div class="foro-autor">
                    ${avatarWrap}
                    <div>
                        <div class="foro-autor-line">${nombreWrap}${followBtn}</div>
                        <span class="post-meta">${tiempo ? this.esc(tiempo) : 'Recientemente'}</span>
                    </div>
                </div>
                <div style="display:flex;gap:6px;flex-wrap:wrap;">${badges}</div>
            </header>
            <h3 class="post-title">${this.esc(titulo)}</h3>
            <p class="post-desc">${this.esc(desc)}</p>
            ${solucionBox}
            <footer class="post-footer">
                <div class="post-actions">
                    <button class="btn-link foro-action" data-action="voto">${votos} Votos</button>
                    ${accionRespuesta}
                    <button class="btn-link foro-action" data-action="guardar">Guardar</button>
                </div>
                <span>${resuelto ? 'Última aportación reciente' : 'Esperando solución'}</span>
            </footer>
        </article>`;
    },

    apunteCard(item) {
        const id = this.field(item, 'id');
        const titulo = this.field(item, 'titulo');
        const materia = this.materiaNombre(item);
        const autor = this.autorNombre(item);
        const tipo = this.field(item, 'tipo') || 'PDF';
        const tamanoBytes = Number(item.adjuntoTamano);
        const tamano = (item.adjuntoTamano != null && Number.isFinite(tamanoBytes))
            ? this.formatBytes(tamanoBytes)
            : (this.field(item, 'tamano') || this.field(item, 'tamanio') || '');
        const descargas = this.num(item, 'descargas');
        const rating = this.field(item, 'rating') || '';
        const url = this.field(item, 'url') || this.field(item, 'archivoUrl') || '#';
        const interno = item.interno === true || item.interno === 'true' || url.startsWith('archivo:');
        const esLink = tipo.toUpperCase() === 'LINK';
        const adjuntoNombre = this.field(item, 'adjuntoNombre');

        const infoExtra = [
            adjuntoNombre ? `Archivo: ${this.esc(adjuntoNombre)}` : '',
            tamano ? `Tamaño: ${this.esc(tamano)}` : '',
            `${descargas} descargas`,
            rating ? `${this.esc(rating)}` : ''
        ].filter(Boolean).join(' • ');

        const accion = interno
            ? `<button class="btn-solid apunte-descargar" type="button" data-id="${this.esc(id)}" data-nombre="${this.esc(adjuntoNombre || '')}">${esLink ? 'Abrir Enlace' : 'Descargar Documento'}</button>`
            : `<button class="btn-solid" type="button" data-href="${this.esc(url)}">${esLink ? 'Abrir Enlace' : 'Descargar Documento'}</button>`;

        return `
        <article class="card apunte-card">
            <div class="apunte-info">
                <div class="file-icon">${this.esc(tipo)}</div>
                <div class="apunte-details">
                    <h4>${this.esc(titulo)}</h4>
                    <p>Materia: <strong>${this.esc(materia)}</strong> • Subido por: <strong>${this.esc(autor)}</strong></p>
                    <p style="margin-top:4px;color:#888;">${infoExtra}</p>
                </div>
            </div>
            ${accion}
        </article>`;
    },

    tipCard(item) {
        const id = this.field(item, 'id');
        const titulo = this.field(item, 'titulo');
        const texto = this.field(item, 'texto') || this.field(item, 'descripcion');
        const materia = this.materiaNombre(item);
        const autor = this.autorNombre(item);
        const votos = this.num(item, 'votos');
        const verificado = item.verificado === true || item.verificado === 'true';
        const itemVotado = item.votado === true || item.votado === 'true';

        const verified = verificado
            ? `<span class="hub-badge badge-verified">Verificado por Docente</span>`
            : '';

        return `
        <article class="card tip-card" data-tip-id="${this.esc(id)}">
            <div>
                <div class="tip-head">
                    <span class="hub-badge badge-materia">${this.esc(materia)}</span>
                    ${verified}
                </div>
                <h4 class="tip-title">${this.esc(titulo)}</h4>
                <p class="tip-text">${this.esc(texto)}</p>
            </div>
            <div class="post-footer">
                <span>
                    <button class="vote-btn${itemVotado ? ' votado' : ''}" type="button" data-id="${this.esc(id)}" data-votado="${itemVotado}">${votos} votos</button>
                </span>
                <span style="font-size:0.78rem;color:#888;">Por: ${this.esc(autor)}</span>
            </div>
        </article>`;
    },

    // ---- modal de acción contextual (compartir tip, etc.) ----
    async abrirModal(tabId) {
        const modal = document.getElementById('modal-root');
        if (!modal) return;

        // Garantiza que las materias estén cargadas antes de abrir el modal.
        await this.cargarMateriasSelect();

        let body = '';
        let titulo = 'Acción';
        const sub = 'Completa la información solicitada.';
        let enviarLabel = 'Publicar';

        if (tabId === 'tips') {
            titulo = 'Compartir Tip Académico';
            body = this.modalFormTips();
        } else if (tabId === 'foro-dudas') {
            titulo = 'Preguntar una Duda';
            enviarLabel = 'Publicar Duda';
            body = [
                '<label for="modal-du-titulo">Título de tu duda</label>',
                '<input type="text" id="modal-du-titulo" class="input-search" maxlength="200" placeholder="Ej. Diferencia entre 1FN y 2FN">',
                '<label for="modal-du-texto">Tu duda</label>',
                '<textarea id="modal-du-texto" maxlength="4000" placeholder="Escribe tu duda académica de manera clara y concreta..."></textarea>',
                '<label for="modal-du-materia">Materia</label>',
                '<select id="modal-du-materia" class="select-materia"></select>'
            ].join('');
        } else {
            titulo = 'Subir Apunte';
            body = [
                '<label for="modal-ap-nombre">Nombre del apunte</label>',
                '<input type="text" id="modal-ap-nombre" class="input-search" maxlength="200" placeholder="Ej. Guía de repaso: estructuras de datos">',
                '<label for="modal-ap-desc">Descripción (opcional)</label>',
                '<textarea id="modal-ap-desc" maxlength="2000" placeholder="Describe brevemente el contenido del apunte..."></textarea>',
                '<label for="modal-ap-materia">Materia</label>',
                '<select id="modal-ap-materia" class="select-materia"></select>',
                '<label for="modal-ap-tipo">Tipo de archivo</label>',
                '<select id="modal-ap-tipo" class="select-materia">' +
                '<option value="PDF">PDF</option>' +
                '<option value="DOC">DOC / Word</option>' +
                '<option value="PPT">Presentación</option>' +
                '<option value="LINK">Enlace externo</option>' +
                '</select>',
                '<label for="modal-ap-archivo">Sube tu documento (opcional)</label>',
                '<div class="file-pick-row">' +
                '<input type="file" id="modal-ap-archivo" class="input-search" ' +
                'accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.csv,.txt,.zip,.png,.jpg,.jpeg,.gif">' +
                '<span id="modal-ap-archivo-nombre" class="file-pick-nombre"></span>' +
                '</div>',
                '<label for="modal-ap-url">…o enlace de descarga externo (opcional)</label>',
                '<input type="url" id="modal-ap-url" class="input-search" maxlength="500" placeholder="https://...">'
            ].join('');
        }

        modal.innerHTML = `
            <div class="hub-modal-overlay" id="hub-modal">
                <div class="hub-modal" role="dialog" aria-modal="true">
                    <h3>${this.esc(titulo)}</h3>
                    <p class="modal-sub">${this.esc(sub)}</p>
                    <form id="hub-modal-form">
                        ${body}
                        <div class="modal-msg" id="modal-msg"></div>
                        <div class="modal-actions">
                            <button class="btn-clean" type="button" id="modal-cancel">Cancelar</button>
                            <button class="btn-solid" type="submit">${this.esc(enviarLabel)}</button>
                        </div>
                    </form>
                </div>
            </div>`;

        const overlay = document.getElementById('hub-modal');
        overlay?.addEventListener('click', e => {
            if (e.target === overlay) this.cerrarModal();
        });
        document.getElementById('modal-cancel')?.addEventListener('click', () => this.cerrarModal());
        document.getElementById('hub-modal-form')?.addEventListener('submit', e => {
            e.preventDefault();
            this.enviarFormularioAccion(tabId);
        });

        this.llenarSelectMateriaModal(tabId);

        if (tabId === 'foro-dudas') {
            const pendiente = this.state.pendienteDuda;
            if (pendiente) {
                const tInput = document.getElementById('modal-du-titulo');
                const cInput = document.getElementById('modal-du-texto');
                const mSelect = document.getElementById('modal-du-materia');
                if (tInput) tInput.value = pendiente.titulo || '';
                if (cInput) cInput.value = pendiente.contenido || '';
                if (mSelect && pendiente.idMateria) mSelect.value = String(pendiente.idMateria);
                document.getElementById('modal-du-titulo')?.focus();
            }
        } else if (tabId === 'tips') {
            document.getElementById('modal-tip-contenido')?.focus();
        } else if (tabId === 'apuntes') {
            const archivoInput = document.getElementById('modal-ap-archivo');
            archivoInput?.addEventListener('change', () => {
                const nombre = document.getElementById('modal-ap-archivo-nombre');
                const f = archivoInput.files && archivoInput.files[0];
                if (!nombre) return;
                nombre.textContent = f ? `${f.name} (${this.formatBytes(f.size)})` : '';
            });
        }
    },

    selectIdMateria(tabId) {
        return tabId === 'tips' ? 'modal-tip-materia' : (tabId === 'foro-dudas' ? 'modal-du-materia' : 'modal-ap-materia');
    },

    refrescarSelectsMateria() {
        ['tips', 'foro-dudas', 'apuntes'].forEach(tabId => {
            if (document.getElementById(this.selectIdMateria(tabId))) {
                this.llenarSelectMateriaModal(tabId);
            }
        });
    },

    llenarSelectMateriaModal(tabId) {
        const select = document.getElementById(this.selectIdMateria(tabId));
        if (!select) return;
        const opciones = this.state.materiasModal || [];
        if (!opciones.length) {
            select.innerHTML = '<option value="">General</option>';
            return;
        }
        select.innerHTML = '<option value="">General</option>' + opciones.map(m => {
            const id = m.id != null ? m.id : m;
            const nombre = this.materiaNombre(m);
            return `<option value="${id}">${this.esc(nombre)}</option>`;
        }).join('');
    },

    modalFormTips() {
        return [
            '<label for="modal-tip-contenido">Contenido del tip</label>',
            '<textarea id="modal-tip-contenido" maxlength="4000" placeholder="Comparte un consejo o truco académico útil para la comunidad..."></textarea>',
            '<label for="modal-tip-materia">Materia</label>',
            '<select id="modal-tip-materia" class="select-materia"></select>'
        ].join('');
    },

    cerrarModal() {
        const overlay = document.getElementById('hub-modal');
        if (overlay) overlay.remove();
    },

    mensajeModal(texto, tipo) {
        const msg = document.getElementById('modal-msg');
        if (msg) {
            msg.textContent = texto;
            msg.className = 'modal-msg ' + (tipo || '');
        }
    },

    async enviarFormularioAccion(tabId) {
        if (tabId === 'tips') {
            await this.crearTip();
        } else if (tabId === 'foro-dudas') {
            await this.crearDuda();
        } else {
            await this.crearApunte();
        }
    },

    async crearTip() {
        const contenido = (document.getElementById('modal-tip-contenido')?.value || '').trim();
        const select = document.getElementById('modal-tip-materia');
        const idMateria = select && select.value ? Number(select.value) : null;

        if (!contenido) {
            this.mensajeModal('El contenido del tip no puede estar vacío.', 'err');
            return;
        }

        try {
            await API.request('/tips', {
                method: 'POST',
                body: JSON.stringify({contenido, idMateria})
            });
            this.mensajeModal('¡Tip publicado correctamente!', 'ok');
            this.cerrarModal();
            this.state.loaded.delete('tips');
            this.state.tips = [];
            await this.loadTips();
        } catch (e) {
            this.mensajeModal(e.message || 'No se pudo publicar el tip.', 'err');
        }
    },

    async crearDuda() {
        const titulo = (document.getElementById('modal-du-titulo')?.value || '').trim();
        const contenido = (document.getElementById('modal-du-texto')?.value || '').trim();
        const select = document.getElementById('modal-du-materia');
        const idMateria = select && select.value ? Number(select.value) : null;

        if (!titulo) {
            this.mensajeModal('Escribe un título claro para tu duda.', 'err');
            return;
        }
        if (!contenido) {
            this.mensajeModal('Describe tu duda para que la comunidad pueda ayudarte.', 'err');
            return;
        }

        const pendiente = {titulo, contenido, idMateria};

        try {
            await API.request('/foro', {
                method: 'POST',
                body: JSON.stringify(pendiente)
            });
            this.state.pendienteDuda = null;
            this.cerrarModal();
            this.state.loaded.delete('foro');
            this.state.publicaciones = [];
            await this.loadForo();
        } catch (e) {
            if (e.status === 409 && e.data && e.data.publicacion) {
                this.renderDuplicado(e.data, pendiente);
            } else {
                this.mensajeModal(e.message || 'No se pudo publicar la duda.', 'err');
            }
        }
    },

    renderDuplicado(dup, pendiente) {
        const pub = dup.publicacion || {};
        const respuestas = dup.respuestas || [];
        const tips = dup.tips || [];
        const recursos = dup.recursos || [];
        const similitud = Math.round((Number(dup.similitud) || 0) * 100);

        const respuestasHtml = respuestas.length
            ? `<h4 class="dup-section-title">Respuestas existentes (${respuestas.length})</h4>` +
            respuestas.map(r => {
                const esSolucion = r.esSolucion === true || r.esSolucion === 'true';
                return `
                  <div class="hub-respuesta">
                      <div class="respuesta-head">
                          <span class="respuesta-autor">${this.esc(this.autorNombre(r))}</span>
                          ${esSolucion ? '<span class="hub-badge badge-solucion">Solución de la comunidad</span>' : ''}
                      </div>
                      <p class="respuesta-texto">${this.esc(r.contenido)}</p>
                  </div>`;
            }).join('')
            : '<p class="dup-vacio">Aún no tiene respuestas.</p>';

        const tipsHtml = tips.length
            ? tips.map(t => this.tipCard(t)).join('')
            : '<p class="dup-vacio">No hay tips relacionados disponibles.</p>';

        const recursosHtml = recursos.length
            ? recursos.map(r => this.apunteCard(r)).join('')
            : '<p class="dup-vacio">No hay recursos relacionados disponibles.</p>';

        const contenido = (this.field(pub, 'descripcion') || this.field(pub, 'contenido')).slice(0, 320);

        const modal = document.getElementById('hub-modal');
        if (!modal) return;

        modal.className = 'hub-modal-overlay dup-overlay';
        modal.innerHTML = `
            <div class="hub-modal dup-modal">
            <div class="dup-notice">
                <strong class="dup-titulo">Esta duda ya fue preguntada en la comunidad</strong>
                <p>Encontramos una conversación muy parecida (coincidencia de ${similitud}%). Únete a ella para recibir ayuda más rápido o revisa su contenido antes de publicar de nuevo.</p>
            </div>
            <div class="dup-question">
                <div class="tip-head">
                    <span class="hub-badge badge-materia">${this.esc(this.materiaNombre(pub))}</span>
                </div>
                <h4 class="tip-title">${this.esc(pub.titulo)}</h4>
                <p class="tip-text">${this.esc(contenido)}</p>
                <p class="dup-por">Preguntado por: ${this.esc(this.autorNombre(pub))}</p>
            </div>
            <div class="dup-sections">
                <div class="dup-col">
                    <h4 class="dup-section-title">Respuestas</h4>
                    <div class="dup-respuestas">${respuestasHtml}</div>
                </div>
                <div class="dup-col">
                    <h4 class="dup-section-title">Tips y recursos relacionados</h4>
                    <div class="dup-tips">${tipsHtml}</div>
                    <div class="dup-recursos">${recursosHtml}</div>
                </div>
            </div>
            <p class="modal-sub" style="margin-top:12px;">¿Quieres publicar tu duda de todos modos o ajustar tu pregunta?</p>
            <div class="modal-msg" id="dup-msg"></div>
            <div class="modal-actions">
                <button class="btn-clean" type="button" id="dup-cerrar">Cancelar</button>
                <button class="btn-clean" type="button" id="dup-editar">Editar mi pregunta</button>
                <button class="btn-solid" type="button" id="dup-publicar">Publicar de todos modos</button>
            </div>
        </div>`;

        document.getElementById('dup-cerrar')?.addEventListener('click', () => this.cerrarModal());
        document.getElementById('dup-editar')?.addEventListener('click', () => this.reabrirFormularioDuda(pendiente));
        document.getElementById('dup-publicar')?.addEventListener('click', () => this.publicarDudaForzada(pendiente));
    },

    reabrirFormularioDuda(pendiente) {
        this.state.pendienteDuda = pendiente;
        this.cerrarModal();
        this.abrirModal('foro-dudas');
    },

    async publicarDudaForzada(pendiente) {
        try {
            await API.request('/foro', {
                method: 'POST',
                body: JSON.stringify({...pendiente, ignorarDuplicado: true})
            });
            this.state.pendienteDuda = null;
            this.cerrarModal();
            this.state.loaded.delete('foro');
            this.state.publicaciones = [];
            await this.loadForo();
        } catch (e) {
            const msg = document.getElementById('dup-msg');
            if (msg) {
                msg.textContent = e.message || 'No se pudo publicar la duda.';
                msg.className = 'modal-msg err';
            }
        }
    },

    async crearApunte() {
        const titulo = (document.getElementById('modal-ap-nombre')?.value || '').trim();
        const descripcion = (document.getElementById('modal-ap-desc')?.value || '').trim();
        const url = (document.getElementById('modal-ap-url')?.value || '').trim();
        const tipo = document.getElementById('modal-ap-tipo')?.value || 'PDF';
        const select = document.getElementById('modal-ap-materia');
        const idMateria = select && select.value ? Number(select.value) : null;
        const archivoInput = document.getElementById('modal-ap-archivo');
        const archivo = (archivoInput && archivoInput.files && archivoInput.files[0]) || null;

        if (!titulo) {
            this.mensajeModal('Escribe el nombre del apunte.', 'err');
            return;
        }
        if (!idMateria) {
            this.mensajeModal('Selecciona la materia del apunte.', 'err');
            return;
        }
        if (!archivo && !url) {
            this.mensajeModal('Sube un archivo o agrega una URL de descarga.', 'err');
            return;
        }

        try {
            if (archivo) {
                await this.subirRecurso({titulo, descripcion, idMateria, archivo});
            } else {
                await API.request('/recursos', {
                    method: 'POST',
                    body: JSON.stringify({titulo, descripcion, idMateria, url, tipo})
                });
            }
            this.cerrarModal();
            this.state.loaded.delete('apuntes');
            this.state.apuntes = [];
            await this.loadApuntes();
            Utils.toast('Apunte publicado correctamente.', 'success');
        } catch (e) {
            this.mensajeModal(e.message || 'No se pudo publicar el apunte.', 'err');
        }
    },

    async subirRecurso({titulo, descripcion, idMateria, archivo}) {
        const token = localStorage.getItem('token');
        const formData = new FormData();
        formData.append('titulo', titulo);
        if (descripcion) formData.append('descripcion', descripcion);
        formData.append('idMateria', String(idMateria));
        formData.append('archivo', archivo);

        const response = await fetch(`${API_BASE_URL}/recursos/upload`, {
            method: 'POST',
            headers: token ? {Authorization: `Bearer ${token}`} : {},
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

    async descargarRecurso(id, nombre) {
        const token = localStorage.getItem('token');
        try {
            const response = await fetch(`${API_BASE_URL}/recursos/${encodeURIComponent(id)}/archivo`, {
                headers: token ? {Authorization: `Bearer ${token}`} : {}
            });
            if (!response.ok) {
                Utils.toast('No se pudo descargar el documento', 'error');
                return;
            }
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = nombre || 'recurso-descarga';
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);
        } catch (e) {
            Utils.toast('No se pudo descargar el documento', 'error');
        }
    },

    async votarTip(id, votado) {
        if (!id) return;
        try {
            if (votado) {
                await API.request(`/tips/${id}/voto`, {method: 'DELETE'});
            } else {
                await API.request(`/tips/${id}/voto`, {method: 'POST'});
            }
            this.state.loaded.delete('tips');
            this.state.tips = [];
            await this.loadTips();
        } catch (e) {
            // se ignora; se mantiene el estado actual
        }
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
        if (item.nombre) return item.nombre;
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

    formatBytes(bytes) {
        const n = Number(bytes);
        if (!isFinite(n) || n <= 0) return '';
        if (n < 1024) return `${n} B`;
        if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
        return `${(n / (1024 * 1024)).toFixed(1)} MB`;
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
