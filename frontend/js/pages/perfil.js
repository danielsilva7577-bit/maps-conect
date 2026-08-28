/**
 * MAPS Connect - Mi Perfil
 * La cabecera usa la sesión (Auth) como dato real; el detalle académico
 * (matrícula, carrera, certificados, stats, actividad) se renderiza solo
 * cuando el backend (/estudiantes/perfil) devuelve datos.
 */

const Perfil = {
    async init() {
        Layout.init('perfil.html');
        Layout.setPageTitle('Mi Perfil');

        // La cabecera puede pintarse con la sesión real del usuario.
        this.renderHeader(Auth.getUser());

        try {
            const data = await API.request('/estudiantes/perfil');
            this.renderProfile(data || {});
        } catch (e) {
            // El endpoint se implementará en el backend. La UI mantiene estados vacíos.
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

        const initials = this.initials(nombre || email);

        host.innerHTML = `
            <div class="profile-avatar">${this.esc(initials)}</div>
            <div>
                <h2 class="profile-name">${this.esc(nombre)}</h2>
                ${email ? `<p class="profile-sub">${this.esc(email)}</p>` : ''}
            </div>
        `;
    },

    renderProfile(data) {
        // Cabecera con datos completos si el backend los trae.
        if (data.nombre || data.matricula || data.carrera) {
            this.renderHeaderData(data);
        }

        // Propósito
        const purposeBox = document.getElementById('profile-purpose');
        const purposeText = document.getElementById('profile-purpose-text');
        if (purposeBox && purposeText) {
            if (data.proposito) {
                purposeText.textContent = data.proposito;
                purposeBox.hidden = false;
            } else {
                purposeBox.hidden = true;
            }
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
        const initials = this.initials(nombre);

        const subParts = [];
        if (data.matricula) subParts.push(`Matrícula: <strong>${this.esc(data.matricula)}</strong>`);
        if (data.carrera) subParts.push(`<strong>${this.esc(data.carrera)}</strong>`);
        const sub1 = subParts.length ? `<p class="profile-sub">${subParts.join(' • ')}</p>` : '';

        const subParts2 = [];
        if (data.semestre) subParts2.push(`Semestre Vigente: <strong>${this.esc(data.semestre)}° Semestre</strong>`);
        if (data.campus) subParts2.push(this.esc(data.campus));
        const sub2 = subParts2.length ? `<p class="profile-sub" style="margin-top:2px;">${subParts2.join(' • ')}</p>` : '';

        host.innerHTML = `
            <div class="profile-avatar">${this.esc(initials)}</div>
            <div>
                <h2 class="profile-name">${this.esc(nombre)}</h2>
                ${sub1}
                ${sub2}
            </div>
        `;
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

    initials(name) {
        if (!name) return '?';
        const parts = String(name).trim().split(/\s+/);
        const first = parts[0] ? parts[0][0] : '';
        const second = parts[1] ? parts[1][0] : '';
        return (first + second).toUpperCase() || '?';
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

document.addEventListener('DOMContentLoaded', () => Perfil.init());
