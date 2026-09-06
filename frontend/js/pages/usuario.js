/**
 * MAPS Connect - Perfil público de otro usuario (alumno o docente).
 * Contrato esperado de GET /usuarios/{id}:
 *  { id, nombre, foto, rol, esYo, reputacion, seguidores, siguiendo, loSigo }
 *  ESTUDIANTE: { carrera, semestre, proposito, certificados[{titulo,descripcion}],
 *                dudas, respuestas, aportes[{titulo,detalle,tipo,meta}] }
 *  PROFESOR:   { especialidad, biografia, horarioAsesorias, disponibleChat,
 *                materias[], respuestas, aportes[{titulo,detalle,tipo,meta}] }
 */

const UsuarioPub = {
    data: null,

    async init() {
        Layout.init('usuario.html');
        Layout.setPageTitle('Perfil');

        const root = document.getElementById('usuario-root');
        if (!root) return;

        const id = new URLSearchParams(window.location.search).get('id');
        if (!id) {
            this.renderError(root, 'No se indicó un usuario.');
            return;
        }

        try {
            const data = await API.request(`/usuarios/${encodeURIComponent(id)}`);
            if (!data) {
                this.renderError(root, 'No se pudo cargar el perfil.');
                return;
            }
            this.data = data;
            this.render();
        } catch (e) {
            this.renderError(root, (e && e.message) || 'No se pudo cargar el perfil.');
        }
    },

    num(v) {
        const n = Number(v);
        return Number.isFinite(n) ? n : null;
    },

    /* ---------- Render principal ---------- */

    render() {
        const d = this.data;
        const esDocente = d.rol === 'PROFESOR';

        document.getElementById('usuario-nombre').textContent = d.nombre || 'Usuario';
        const trigger = document.getElementById('avatarTrigger');
        trigger.innerHTML = Utils.avatarHtml(d.nombre, d.foto, 'profile-avatar trigger', d.nombre);

        const sub = document.getElementById('usuario-sub');
        const sub2 = document.getElementById('usuario-sub2');
        if (esDocente) {
            sub.innerHTML = d.especialidad
                ? `Docente &bull; <strong>${Utils.esc(d.especialidad)}</strong>`
                : 'Docente';
            sub2.innerHTML = `Reputaci&oacute;n: <strong>${this.neto(d.reputacion)}</strong>`;
        } else {
            sub.innerHTML = d.carrera
                ? `Estudiante &bull; <strong>${Utils.esc(d.carrera)}</strong>`
                : 'Estudiante';
            sub2.innerHTML = (d.semestre
                    ? `Semestre Vigente: <strong>${this.neto(d.semestre)}&deg; Semestre</strong> &bull; `
                    : '')
                + `Reputaci&oacute;n: <strong>${this.neto(d.reputacion)}</strong>`;
        }

        this.renderActions(d);
        this.renderIzquierda(d);
        this.renderDerecha(d);
    },

    neto(v) {
        const n = this.num(v);
        return n == null ? '—' : n;
    },

    renderActions(d) {
        const actions = document.getElementById('usuario-actions');
        if (!actions) return;

        if (d.esYo) {
            actions.innerHTML = '<a class="btn-clean" href="perfil.html">Este es tu perfil</a>';
            return;
        }

        const loSigo = d.loSigo === true || d.loSigo === 'true';
        actions.innerHTML = `
            <button class="follow-btn${loSigo ? ' siguiendo' : ''}" type="button" id="usuario-follow" data-siguiendo="${loSigo}">${loSigo ? 'Siguiendo' : 'Seguir'}</button>
            <button class="btn-clean" type="button" id="usuario-invitar">Invitar a C&iacute;rculo</button>
            <button class="btn-solid" type="button" id="usuario-mensaje">Enviar Mensaje</button>
        `;

        document.getElementById('usuario-follow')?.addEventListener('click', async e => {
            const siguiente = await Utils.alternarSeguir(e.target, d.id);
            const box = document.getElementById('stat-seg');
            if (box) {
                const base = this.num(box.dataset.base);
                if (base != null) box.textContent = String(base + (siguiente ? 1 : -1));
            }
        });

        document.getElementById('usuario-invitar')?.addEventListener('click', () => {
            Utils.toast('La invitación a círculos de estudio se habilita próximamente.', 'info');
        });

        document.getElementById('usuario-mensaje')?.addEventListener('click', async () => {
            try {
                await API.request(`/mensajes/nuevo/${encodeURIComponent(d.id)}`, { method: 'POST' });
            } catch (e) {
                // La conversación suele existir; se navega igual.
            }
            window.location.href = 'mensajes.html';
        });
    },

    renderIzquierda(d) {
        const host = document.getElementById('col-izquierda');
        if (!host) return;

        if (d.rol === 'ESTUDIANTE') {
            const certs = Array.isArray(d.certificados) ? d.certificados : [];
            const proposito = d.proposito
                ? `<div class="purpose-box"><strong>Prop&oacute;sito Declarado</strong><p>${Utils.esc(d.proposito)}</p></div>`
                : '';
            const listaCerts = certs.length ? certs.map((c, i) => `
                <div class="certificate-item${i === 0 ? ' active' : ''}">
                    <div class="cert-header">
                        <span class="cert-title">${Utils.esc(c.titulo || 'Certificado MAPS')}</span>
                        <span class="badge badge-green">En Curso</span>
                    </div>
                    <p class="cert-desc">${Utils.esc(c.descripcion)}</p>
                </div>`).join('')
                : '<p class="empty-note">Aún no ha seleccionado certificados MAPS.</p>';

            host.innerHTML = `
                <h3 class="section-heading">Ruta Acad&eacute;mica MAPS</h3>
                ${proposito}
                <h4 class="subsection-heading">Certificados en Curso y Obtenidos</h4>
                ${listaCerts}`;
            return;
        }

        if (d.rol === 'PROFESOR') {
            const bio = d.biografia
                ? `<div class="purpose-box"><strong>Sobre ${Utils.esc(d.nombre)}</strong><p>${Utils.esc(d.biografia)}</p></div>`
                : '';
            const materias = (Array.isArray(d.materias) && d.materias.length)
                ? `<li><strong>Materias asignadas:</strong><span style="display:block;margin-top:4px;">${d.materias.map(m => `<span class="materia-tag">${Utils.esc(m)}</span>`).join('')}</span></li>`
                : '';

            host.innerHTML = `
                <h3 class="section-heading">Perfil Docente</h3>
                ${bio}
                <ul class="prof-detail-list">
                    ${d.especialidad ? `<li><strong>Especialidad:</strong> ${Utils.esc(d.especialidad)}</li>` : ''}
                    ${materias}
                    ${d.horarioAsesorias ? `<li><strong>Horario de asesor&iacute;as:</strong> ${Utils.esc(d.horarioAsesorias)}</li>` : ''}
                    <li><strong>Disponible en chat:</strong> <span class="${d.disponibleChat ? 'ok' : 'off'}">${d.disponibleChat ? 'S&iacute;' : 'No'}</span></li>
                </ul>`;
            return;
        }

        host.innerHTML = '<p class="empty-note">Perfil sin información académica adicional.</p>';
    },

    renderDerecha(d) {
        const esDocente = d.rol === 'PROFESOR';
        const stats = esDocente ? [
            { key: 'seg', label: 'Seguidores', val: d.seguidores },
            { key: 'sig', label: 'Siguiendo', val: d.siguiendo },
            { key: 'res', label: 'Respuestas Aportadas', val: d.respuestas },
            { key: 'mat', label: 'Materias', val: Array.isArray(d.materias) ? d.materias.length : 0 }
        ] : [
            { key: 'seg', label: 'Seguidores', val: d.seguidores },
            { key: 'sig', label: 'Siguiendo', val: d.siguiendo },
            { key: 'dud', label: 'Dudas en el Foro', val: d.dudas },
            { key: 'res', label: 'Respuestas Aportadas', val: d.respuestas }
        ];

        const statsHost = document.getElementById('usuario-stats');
        if (statsHost) {
            statsHost.innerHTML = stats.map(s => `
                <div class="stat-box">
                    <span class="number" id="stat-${s.key}"${s.key === 'seg' ? ` data-base="${this.neto(s.val)}"` : ''}>${this.neto(s.val)}</span>
                    <span class="label">${Utils.esc(s.label)}</span>
                </div>`).join('');
        }

        const aportes = Array.isArray(d.aportes) ? d.aportes : [];
        const aportesHost = document.getElementById('usuario-aportes');
        if (aportesHost) {
            aportesHost.innerHTML = aportes.length ? aportes.map(a => `
                <div class="public-item">
                    <strong>${Utils.esc(a.titulo || 'Aporte')}</strong>
                    <p>${Utils.esc(a.detalle)}</p>
                    <div class="item-meta">
                        <span>${a.tipo === 'respuesta' ? 'Respuesta en foro' : 'Duda publicada'}</span>
                        <span>${Utils.esc(a.meta)}</span>
                    </div>
                </div>`).join('')
                : '<p class="empty-note">Aún no hay aportaciones públicas.</p>';
        }
    },

    /* ---------- Modal para ampliar avatar ---------- */

    renderError(root, message) {
        root.innerHTML = `<div class="card empty-state"><h2>Perfil no disponible</h2><p>${Utils.esc(message)}</p><a class="side-link" href="inicio.html">Volver al inicio →</a></div>`;
    }
};

document.addEventListener('DOMContentLoaded', () => {

    const backdrop = document.getElementById('avatarModal');
    const trigger = document.getElementById('avatarTrigger');
    const closeBtn = document.getElementById('closeModalBtn');
    const expanded = document.getElementById('avatarExpanded');

    function abrirModal() {
        const d = UsuarioPub.data;
        if (!d || !backdrop || !expanded) return;
        if (d.foto) {
            expanded.innerHTML = `<img class="avatar-expanded-box" src="${Utils.esc(d.foto)}" alt="Foto de ${Utils.esc(d.nombre)}">`;
        } else {
            expanded.innerHTML = `<span class="avatar-expanded-box">${Utils.esc(Utils.iniciales(d.nombre))}</span>`;
        }
        backdrop.classList.add('show');
    }

    function cerrarModal() {
        if (backdrop) backdrop.classList.remove('show');
    }

    trigger?.addEventListener('click', abrirModal);
    closeBtn?.addEventListener('click', cerrarModal);
    backdrop?.addEventListener('click', e => { if (e.target === backdrop) cerrarModal(); });
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && backdrop.classList.contains('show')) cerrarModal();
    });

    UsuarioPub.init();
});