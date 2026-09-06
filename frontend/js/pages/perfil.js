/**
 * MAPS Connect - Mi Perfil
 * La cabecera usa la sesión (Auth) como dato real; el detalle académico
 * (matrícula, carrera, certificados, stats, actividad) se renderiza solo
 * cuando el backend (/estudiantes/perfil) devuelve datos.
 */

const Perfil = {
    state: {
        proposito: '',
        semestre: null
    },

    async init() {
        Layout.init('perfil.html');
        Layout.setPageTitle('Mi Perfil');

        // La cabecera puede pintarse con la sesión real del usuario.
        this.renderHeader(Auth.getUser());
        this.bindEventos();

        try {
            const data = await API.request('/estudiantes/perfil');
            this.renderProfile(data || {});
        } catch (e) {
            // El endpoint se implementará en el backend. La UI mantiene estados vacíos.
        }
    },

    bindEventos() {
        const btn = document.getElementById('btn-editar-proposito');
        btn?.addEventListener('click', () => this.abrirModalProposito());

        const btnSemestre = document.getElementById('btn-cambiar-semestre');
        btnSemestre?.addEventListener('click', () => this.abrirModalSemestre());

        const btnFoto = document.getElementById('btn-cambiar-foto');
        const input = document.getElementById('foto-input');
        btnFoto?.addEventListener('click', () => input?.click());
        input?.addEventListener('change', () => this.cambiarFoto(input));

        // Delegación para los botones Seguir/Siguiendo de "Descubrir personas".
        document.getElementById('discover-list')?.addEventListener('click', e => {
            const followBtn = e.target.closest('.follow-btn');
            if (!followBtn) return;
            e.preventDefault();
            const id = followBtn.getAttribute('data-id');
            if (!id) return;
            Utils.alternarSeguir(followBtn, id);
        });
    },

    async cambiarFoto(input) {
        const archivo = input && input.files && input.files[0];
        if (!archivo) return;
        try {
            const dataUrl = await Utils.fotoDataUrl(archivo);
            await API.request('/usuarios/foto', {
                method: 'PUT',
                body: JSON.stringify({ foto: dataUrl })
            });
            Auth.foto(dataUrl);
            this.renderHeader(Auth.getUser());
            Utils.toast('Foto de perfil actualizada.', 'success');
        } catch (e) {
            Utils.toast(e.message || 'No se pudo actualizar la foto.', 'error');
        } finally {
            input.value = '';
        }
    },

    renderHeader(user) {
        const host = document.getElementById('profile-header-info');
        if (!host) return;

        const nombre = (user && user.nombre) || '';
        const email = (user && user.email) || '';
        if (!nombre && !email) {
            host.innerHTML = '';
            return;
        }

        const foto = (user && user.foto) || null;

        host.innerHTML = `
            <div>${Utils.avatarHtml(nombre, foto, 'avatar profile-avatar', nombre)}</div>
            <div>
                <h2 class="profile-name">${this.esc(nombre)}</h2>
                ${email ? `<p class="profile-sub">${this.esc(email)}</p>` : ''}
            </div>
        `;
    },

    renderProfile(data) {
        // Cabecera con datos completos si el backend los trae.
        if (data.foto) Auth.foto(data.foto);
        if (data.nombre || data.matricula || data.carrera) {
            this.renderHeaderData(data);
        }

        // Red
        this.renderRed(data);

        // Propósito
        const purposeBox = document.getElementById('profile-purpose');
        const purposeText = document.getElementById('profile-purpose-text');
        if (purposeBox && purposeText) {
            if (data.proposito) {
                this.state.proposito = data.proposito;
                purposeText.textContent = data.proposito;
                purposeBox.hidden = false;
            } else {
                this.state.proposito = '';
                purposeBox.hidden = true;
            }
        }

        if (data.semestre != null) {
            this.state.semestre = Number(data.semestre);
        }

        // Certificados
        const certs = Array.isArray(data.certificados) ? data.certificados : [];
        this.renderCertificados(certs);

        // Stats
        this.renderStats(data.stats || {});

        // Actividad
        const acts = Array.isArray(data.actividad) ? data.actividad : [];
        this.renderActividad(acts);
    },

    renderHeaderData(data) {
        const host = document.getElementById('profile-header-info');
        if (!host) return;

        const nombre = data.nombre || (Auth.getUser() && Auth.getUser().nombre) || '';
        const foto = data.foto || null;

        const subParts = [];
        if (data.matricula) subParts.push(`Matrícula: <strong>${this.esc(data.matricula)}</strong>`);
        if (data.carrera) subParts.push(`<strong>${this.esc(data.carrera)}</strong>`);
        const sub1 = subParts.length ? `<p class="profile-sub">${subParts.join(' • ')}</p>` : '';

        const subParts2 = [];
        if (data.semestre) subParts2.push(`Semestre Vigente: <strong>${this.esc(data.semestre)}° Semestre</strong>`);
        if (data.campus) subParts2.push(this.esc(data.campus));
        const sub2 = subParts2.length ? `<p class="profile-sub" style="margin-top:2px;">${subParts2.join(' • ')}</p>` : '';

        host.innerHTML = `
            <div>${Utils.avatarHtml(nombre, foto, 'avatar profile-avatar', nombre)}</div>
            <div>
                <h2 class="profile-name">${this.esc(nombre)}</h2>
                ${sub1}
                ${sub2}
            </div>
        `;
    },

    renderRed(data) {
        const card = document.getElementById('profile-red');
        if (!card) return;
        const siguiendo = data.siguiendo;
        const seguidores = data.seguidores;
        if (siguiendo == null && seguidores == null) {
            card.hidden = true;
            return;
        }
        card.hidden = false;

        const stats = document.getElementById('red-stats');
        if (stats) {
            stats.innerHTML = `
                <div class="red-stat"><span class="number">${this.esc(seguidores != null ? seguidores : '—')}</span><span class="label">Seguidores</span></div>
                <div class="red-stat"><span class="number">${this.esc(siguiendo != null ? siguiendo : '—')}</span><span class="label">Siguiendo</span></div>
            `;
        }

        this.cargarDescubrir();
    },

    async cargarDescubrir() {
        const host = document.getElementById('discover-list');
        if (!host) return;
        try {
            const res = await API.request('/usuarios/descubrir');
            const lista = Array.isArray(res) ? res : (res && Array.isArray(res.data) ? res.data : []);
            this.renderDescubrir(lista);
        } catch (e) {
            host.innerHTML = '<p class="discover-empty">No se pudo cargar la red en este momento.</p>';
        }
    },

    renderDescubrir(lista) {
        const host = document.getElementById('discover-list');
        if (!host) return;
        if (!lista.length) {
            host.innerHTML = '<p class="discover-empty">Ya sigues a todas las personas de tu comunidad.</p>';
            return;
        }

        host.innerHTML = lista.map(p => {
            const esDocente = p.rol === 'PROFESOR' || p.rol === 'profesor';
            const rol = esDocente ? 'Docente' : (p.carrera ? p.carrera : 'Estudiante');
            const seguidores = Number(p.seguidores) || 0;
            const loSigo = p.loSigo === true || p.loSigo === 'true';
            return `
            <div class="discover-person">
                <a class="user-link" href="usuario.html?id=${this.esc(p.id)}" aria-label="Ver perfil de ${this.esc(p.nombre)}">${Utils.avatarHtml(p.nombre, p.foto, 'avatar', p.nombre)}</a>
                <div class="discover-info">
                    <a class="user-link" href="usuario.html?id=${this.esc(p.id)}"><strong>${this.esc(p.nombre)}</strong></a>
                    <span>${this.esc(rol)} • ${seguidores} seguidores</span>
                </div>
                <button class="follow-btn${loSigo ? ' siguiendo' : ''}" type="button"
                    data-id="${this.esc(p.id)}" data-siguiendo="${loSigo ? 'true' : 'false'}">${loSigo ? 'Siguiendo' : 'Seguir'}</button>
            </div>`;
        }).join('');
    },

    renderCertificados(certs) {
        const host = document.getElementById('profile-certificados');
        if (!host) return;
        if (!certs.length) { host.innerHTML = ''; return; }
        host.innerHTML = certs.map(c => this.certCard(c)).join('');
    },

    certCard(c) {
        const titulo = c.titulo || '';
        const desc = c.descripcion || '';
        const estado = c.estado || '';
        const semestre = c.semestre ? `${this.esc(c.semestre)}° Semestre` : '';

        let badge = '';
        if (estado === 'acreditado') {
            badge = `<span class="badge badge-green">✓ Acreditado${semestre ? ' (' + semestre + ')' : ''}</span>`;
        } else if (estado === 'encurso' || estado === 'en-curso') {
            badge = `<span class="badge badge-green">En Curso${semestre ? ' (' + semestre + ')' : ''}</span>`;
        } else if (estado === 'futuro' || estado === 'bloque') {
            badge = `<span class="badge badge-gray">Bloque Futuro${semestre ? ' (' + semestre + ')' : ''}</span>`;
        } else if (estado) {
            badge = `<span class="badge badge-gray">${this.esc(estado)}</span>`;
        }

        const activeCls = (estado === 'encurso' || estado === 'en-curso') ? ' active' : '';

        return `
        <div class="certificate-item${activeCls}">
            <div class="cert-header">
                <span class="cert-title">${this.esc(titulo)}</span>
                ${badge}
            </div>
            ${desc ? `<p class="cert-desc">${this.esc(desc)}</p>` : ''}
        </div>`;
    },

    renderStats(stats) {
        const host = document.getElementById('profile-stats');
        if (!host) return;
        const items = [
            { n: stats.dudasResueltas, l: 'Dudas Resueltas' },
            { n: stats.apuntesCompartidos, l: 'Apuntes Compartidos' },
            { n: stats.votos, l: 'Votos de Utilidad' },
            { n: stats.reputacion, l: 'Reputación Académica' }
        ];
        // Solo se pinta si al menos hay un valor con dato.
        const hasData = items.some(it => it.n !== undefined && it.n !== null && it.n !== '');
        if (!hasData) { host.innerHTML = ''; return; }
        host.innerHTML = items.map(it => `
            <div class="stat-box">
                <span class="number">${this.esc(it.n !== undefined && it.n !== null && it.n !== '' ? it.n : '—')}</span>
                <span class="label">${this.esc(it.l)}</span>
            </div>
        `).join('');
    },

    renderActividad(acts) {
        const host = document.getElementById('profile-actividad');
        if (!host) return;
        if (!acts.length) { host.innerHTML = ''; return; }
        host.innerHTML = acts.map(a => `
            <li class="activity-item">
                <strong>${this.esc(a.titulo || '')}</strong>
                <span>${this.esc(a.detalle || a.descripcion || '')}</span>
            </li>
        `).join('');
    },

    abrirModalProposito() {
        let modal = document.getElementById('proposito-modal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'proposito-modal';
            modal.className = 'proposito-modal-overlay';
            document.body.appendChild(modal);
        }

        modal.innerHTML = `
            <div class="proposito-modal" role="dialog" aria-modal="true" aria-label="Editar propósito de vida">
                <h3>Propósito de Vida</h3>
                <p class="proposito-modal-sub">Describe tu propósito personal. Aparecerá en tu identidad y Ruta MAPS.</p>
                <textarea id="proposito-input" maxlength="1000" rows="4" placeholder="Escribe tu propósito de vida...">${this.esc(this.state.proposito)}</textarea>
                <div class="proposito-modal-msg" id="proposito-msg"></div>
                <div class="proposito-modal-actions">
                    <button class="btn-clean" type="button" id="proposito-cancel">Cancelar</button>
                    <button class="btn-solid" type="button" id="proposito-save">Guardar</button>
                </div>
            </div>`;

        modal.addEventListener('click', e => {
            if (e.target === modal) this.cerrarModalProposito();
        });
        document.getElementById('proposito-cancel').addEventListener('click', () => this.cerrarModalProposito());
        document.getElementById('proposito-save').addEventListener('click', () => this.guardarProposito());
        document.getElementById('proposito-input').focus();

        const input = document.getElementById('proposito-input');
        input.addEventListener('keydown', e => {
            if (e.key === 'Escape') this.cerrarModalProposito();
        });
    },

    async guardarProposito() {
        const input = document.getElementById('proposito-input');
        const msg = document.getElementById('proposito-msg');
        if (!input) return;

        const proposito = input.value.trim();
        const btn = document.getElementById('proposito-save');
        btn.disabled = true;
        btn.textContent = 'Guardando...';

        try {
            await API.request('/estudiantes/proposito', {
                method: 'PUT',
                body: JSON.stringify({ proposito })
            });

            this.state.proposito = proposito;
            const purposeBox = document.getElementById('profile-purpose');
            const purposeText = document.getElementById('profile-purpose-text');
            if (purposeText) purposeText.textContent = proposito;
            if (purposeBox) purposeBox.hidden = !proposito;

            this.cerrarModalProposito();
            Utils.toast(proposito ? 'Propósito de vida actualizado.' : 'Propósito de vida eliminado.', 'success');
        } catch (e) {
            if (msg) msg.textContent = e.message || 'No se pudo guardar el propósito.';
            btn.disabled = false;
            btn.textContent = 'Guardar';
        }
    },

    cerrarModalProposito() {
        const modal = document.getElementById('proposito-modal');
        if (modal) modal.remove();
    },

    abrirModalSemestre() {
        let modal = document.getElementById('semestre-modal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'semestre-modal';
            modal.className = 'proposito-modal-overlay';
            document.body.appendChild(modal);
        }

        const actual = this.state.semestre != null ? Number(this.state.semestre) : '';
        const opciones = Array.from({ length: 12 }, (_, i) => i + 1)
            .map(n => `<option value="${n}"${String(n) === String(actual) ? ' selected' : ''}>${n}° Semestre</option>`)
            .join('');

        modal.innerHTML = `
            <div class="proposito-modal" role="dialog" aria-modal="true" aria-label="Cambiar semestre">
                <h3>Cambiar de Semestre</h3>
                <p class="proposito-modal-sub">Al avanzar de semestre se actualizarán tus materias del plan de estudios y la información de la comunidad corresponderá a tu nuevo semestre.</p>
                <label for="semestre-select" style="display:block;margin-bottom:6px;font-weight:bold;">Semestre actualmente vigente</label>
                <select id="semestre-select" class="input-search">${opciones}</select>
                <div class="proposito-modal-msg" id="semestre-msg"></div>
                <div class="proposito-modal-actions">
                    <button class="btn-clean" type="button" id="semestre-cancel">Cancelar</button>
                    <button class="btn-solid" type="button" id="semestre-save">Guardar</button>
                </div>
            </div>`;

        modal.addEventListener('click', e => {
            if (e.target === modal) this.cerrarModalSemestre();
        });
        document.getElementById('semestre-cancel').addEventListener('click', () => this.cerrarModalSemestre());
        document.getElementById('semestre-save').addEventListener('click', () => this.guardarSemestre());
    },

    async guardarSemestre() {
        const select = document.getElementById('semestre-select');
        const msg = document.getElementById('semestre-msg');
        if (!select) return;

        const semestre = Number(select.value);
        const btn = document.getElementById('semestre-save');
        btn.disabled = true;
        btn.textContent = 'Guardando...';

        try {
            await API.request('/estudiantes/semestre', {
                method: 'PUT',
                body: JSON.stringify({ semestre })
            });
            this.cerrarModalSemestre();
            this.state.semestre = semestre;
            Utils.toast(`Avanzaste al ${semestre}° semestre.`, 'success');

            try {
                const data = await API.request('/estudiantes/perfil');
                this.renderProfile(data || {});
            } catch (e) {
                // La cabecera ya se re-renderiza si es posible; el resto se mantiene.
            }
        } catch (e) {
            if (msg) msg.textContent = e.message || 'No se pudo actualizar el semestre.';
            btn.disabled = false;
            btn.textContent = 'Guardar';
        }
    },

    cerrarModalSemestre() {
        const modal = document.getElementById('semestre-modal');
        if (modal) modal.remove();
    },

    esc(str) {
        return String(str == null ? '' : str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },
};

document.addEventListener('DOMContentLoaded', () => Perfil.init());
