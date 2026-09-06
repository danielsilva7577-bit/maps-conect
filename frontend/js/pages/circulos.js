/**
 * Círculos de estudio.
 * Contrato esperado de GET /circulos:
 * { sesiones, misGrupos } con sesiones [{ id, titulo, descripcion, materia,
 * modalidad, ubicacion, fecha (yyyy-MM-dd), horaInicio (HH:mm), duracionMin,
 * cupoMax, estado, organizador, organizadorId, inscritos, inscrito,
 * organizadorYo }] y misGrupos [{ id, nombre, descripcion, miembros }].
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('circulos.html');
    Layout.setPageTitle('Círculos de Estudio');

    const content = document.getElementById('main-content');
    if (!content) return;
    content.innerHTML = '<div class="circulos-container"><p class="loading-message">Cargando círculos de estudio…</p></div>';
    await loadCircles(content);
});

async function loadCircles(content, search = '') {
    let sessions = [];
    let groups = [];
    try {
        const data = await API.request(`/circulos${search ? `?busqueda=${encodeURIComponent(search)}` : ''}`);
        sessions = Array.isArray(data.sesiones) ? data.sesiones : [];
        groups = Array.isArray(data.misGrupos) ? data.misGrupos : [];
    } catch (error) {
        Utils.toast(error.message || 'No se pudieron cargar los círculos.', 'error');
    }
    renderCircles(content, sessions, groups, search);
}

function renderCircles(content, sessions, groups, search = '') {
    content.innerHTML = `
        <div class="circulos-container">
            <header class="module-heading"><h1>Círculos de estudio</h1><p>Encuentra sesiones de repaso y comunidades para aprender en conjunto.</p></header>
            <form class="top-action-bar" id="circle-search-form">
                <label class="visually-hidden" for="circle-search">Buscar círculo o sesión</label>
                <input class="search-input" id="circle-search" name="busqueda" type="search" value="${escapeHtml(search)}" placeholder="Buscar por materia, título u organizador...">
                <button class="btn-solid" type="button" id="create-sesion">+ Crear sesión de repaso</button>
            </form>
            <div class="circulos-grid">
                <section><h2 class="section-title">Próximas sesiones de repaso</h2>${sessions.length ? sessions.map(renderSession).join('') : renderEmptySessions()}</section>
                <aside><h2 class="section-title">Tus grupos permanentes</h2>${groups.length ? `<section class="card">${groups.map(renderGroup).join('')}</section>` : ''}<section class="card rules-card"><h2>Normas del círculo</h2><p>Los círculos son espacios colaborativos. Comparte material libre de plagio y mantén el respeto en las salas de videollamada.</p></section></aside>
            </div>
            ${renderSesionModal()}
        </div>
    `;

    document.getElementById('circle-search-form')?.addEventListener('submit', event => {
        event.preventDefault();
        loadCircles(content, new FormData(event.currentTarget).get('busqueda')?.trim());
    });
    document.getElementById('create-sesion')?.addEventListener('click', abrirModalSesion);

    document.getElementById('sesion-modal-cancel')?.addEventListener('click', cerrarModalSesion);
    document.getElementById('sesion-modal')?.addEventListener('click', e => {
        if (e.target === document.getElementById('sesion-modal')) cerrarModalSesion();
    });
    document.getElementById('sesion-form')?.addEventListener('submit', enviarFormSesion);

    // Delegación para acciones dentro de tarjetas que se renderizan dinámicamente.
    content.addEventListener('click', e => {
        const target = e.target.closest('button[data-action]');
        if (!target) return;
        const id = Number(target.dataset.id);
        switch (target.dataset.action) {
            case 'unirse-sesion':
                cambiarInscripcion(id, true, content);
                break;
            case 'dejar-sesion':
                cambiarInscripcion(id, false, content);
                break;
            case 'ver-grupo':
                Utils.toast('El detalle del grupo se habilita próximamente.', 'info');
                break;
        }
    });
}

function renderSession(session) {
    const llena = session.estado === 'CERRADA' || Number(session.inscritos) >= Number(session.cupoMax);
    const badges = [
        `<span class="badge badge-time">${escapeHtml(fechaLegible(session.fecha))}</span>`,
        `<span class="badge badge-hora">${escapeHtml(horaRango(session.horaInicio, session.duracionMin))}</span>`,
        session.materia ? `<span class="badge badge-materia">${escapeHtml(session.materia)}</span>` : '',
        llena ? '<span class="badge badge-cupo">Cupo lleno</span>' : ''
    ].filter(Boolean).join('');

    const info = [
        `Organiza: <strong>${escapeHtml(session.organizador || 'Docente')}</strong>`,
        session.modalidad ? `Modalidad: <strong>${escapeHtml(session.modalidad)}</strong>` : '',
        session.ubicacion ? `Ubicación: <strong>${escapeHtml(session.ubicacion)}</strong>` : ''
    ].filter(Boolean).join(' · ');

    const acciones = session.organizadorYo
        ? '<span class="organizer-label">Tú organizas esta sesión</span>'
        : (llena
            ? '<button class="btn-clean" type="button" disabled>Cupo lleno</button>'
            : (session.inscrito
                ? '<button class="btn-clean" type="button" data-action="dejar-sesion" data-id="' + session.id + '">Salir de la sesión</button>'
                : '<button class="btn-solid" type="button" data-action="unirse-sesion" data-id="' + session.id + '">Unirme</button>'));

    return `
        <article class="card session-card">
            <div class="session-header"><div class="badges-wrap">${badges}</div></div>
            <h2 class="session-title">${escapeHtml(session.titulo || 'Sesión de repaso')}</h2>
            <p class="session-info">${info}</p>
            ${session.descripcion ? `<p class="session-desc">${escapeHtml(session.descripcion)}</p>` : ''}
            <div class="session-footer">
                <span class="attendees-count">${Number(session.inscritos)} de ${Number(session.cupoMax)} confirmados</span>
                <div class="session-actions">${acciones}</div>
            </div>
        </article>
    `;
}

function renderGroup(group) {
    return `<article class="group-item"><span class="group-name">${escapeHtml(group.nombre || 'Grupo de estudio')}</span>${group.descripcion ? `<span class="group-description">${escapeHtml(group.descripcion)}</span>` : ''}<div class="group-meta">${Number.isFinite(Number(group.miembros)) ? `<span>${Number(group.miembros)} miembros</span>` : '<span></span>'}<button class="group-link" type="button" data-action="ver-grupo">Ver grupo →</button></div></article>`;
}

function renderSesionModal() {
    return `
        <div class="sesion-modal-overlay" id="sesion-modal" hidden>
            <div class="sesion-modal" role="dialog" aria-modal="true">
                <h3>Nueva sesión de repaso</h3>
                <p class="modal-sub">Organiza una sesión abierta y tus compañeros podrán unirse.</p>
                <form id="sesion-form">
                    <label for="s-titulo">Título *</label>
                    <input class="sesion-input" id="s-titulo" type="text" maxlength="200" required placeholder="Ej. Repaso de SQL para el parcial">
                    <label for="s-materia">Materia *</label>
                    <input class="sesion-input" id="s-materia" type="text" maxlength="200" required placeholder="Ej. Bases de datos">
                    <label for="s-descripcion">Descripción</label>
                    <textarea class="sesion-textarea" id="s-descripcion" maxlength="2000" placeholder="¿Qué repasarán? (opcional)"></textarea>
                    <div class="sesion-field-row">
                        <div><label for="s-fecha">Fecha *</label><input class="sesion-input" id="s-fecha" type="date" required></div>
                        <div><label for="s-hora">Hora de inicio *</label><input class="sesion-input" id="s-hora" type="time" required></div>
                    </div>
                    <div class="sesion-field-row">
                        <div><label for="s-duracion">Duración (min)</label><input class="sesion-input" id="s-duracion" type="number" min="15" max="480" step="15" value="90"></div>
                        <div><label for="s-cupo">Cupo máximo</label><input class="sesion-input" id="s-cupo" type="number" min="1" max="300" value="30"></div>
                    </div>
                    <label for="s-modalidad">Modalidad</label>
                    <select class="sesion-input" id="s-modalidad">
                        <option value="Grupal">Grupal</option>
                        <option value="En línea">En línea</option>
                        <option value="Individual">Individual</option>
                    </select>
                    <label for="s-ubicacion">Ubicación / plataforma</label>
                    <input class="sesion-input" id="s-ubicacion" type="text" maxlength="200" placeholder="Ej. Biblioteca Central, Sala 2 o Google Meet">
                    <div class="modal-msg" id="sesion-modal-msg"></div>
                    <div class="modal-actions">
                        <button class="btn-clean" type="button" id="sesion-modal-cancel">Cancelar</button>
                        <button class="btn-solid" type="submit">Crear sesión</button>
                    </div>
                </form>
            </div>
        </div>
    `;
}

function abrirModalSesion() {
    const overlay = document.getElementById('sesion-modal');
    if (!overlay) return;
    const fecha = document.getElementById('s-fecha');
    if (fecha) {
        fecha.min = new Date().toISOString().slice(0, 10);
        fecha.value = fecha.min;
    }
    const msg = document.getElementById('sesion-modal-msg');
    if (msg) msg.textContent = '';
    overlay.hidden = false;
}

function cerrarModalSesion() {
    const overlay = document.getElementById('sesion-modal');
    if (overlay) overlay.hidden = true;
}

async function enviarFormSesion(event) {
    event.preventDefault();
    const msg = document.getElementById('sesion-modal-msg');
    const btn = event.currentTarget.querySelector('button[type="submit"]');
    if (btn) btn.disabled = true;

    const payload = {
        titulo: document.getElementById('s-titulo').value.trim(),
        materia: document.getElementById('s-materia').value.trim(),
        descripcion: document.getElementById('s-descripcion').value.trim(),
        fecha: document.getElementById('s-fecha').value,
        horaInicio: document.getElementById('s-hora').value,
        duracionMin: Number(document.getElementById('s-duracion').value) || 90,
        cupoMax: Number(document.getElementById('s-cupo').value) || 30,
        modalidad: document.getElementById('s-modalidad').value,
        ubicacion: document.getElementById('s-ubicacion').value.trim()
    };

    try {
        await API.request('/circulos/sesiones', { method: 'POST', body: JSON.stringify(payload) });
        cerrarModalSesion();
        Utils.toast('Sesión de repaso creada.', 'success');
        const content = document.getElementById('main-content');
        if (content) await loadCircles(content);
    } catch (error) {
        if (msg) msg.textContent = error.message || 'No se pudo crear la sesión.';
    } finally {
        if (btn) btn.disabled = false;
    }
}

async function cambiarInscripcion(id, unirse, content) {
    try {
        const data = await API.request(`/circulos/sesiones/${id}/inscribirse`, { method: unirse ? 'POST' : 'DELETE' });
        Utils.toast(unirse ? `Asistencia confirmada a "${data.titulo}".` : 'Te has dado de baja de la sesión.', 'success');
    } catch (error) {
        Utils.toast(error.message || 'No se pudo actualizar tu inscripción.', 'error');
    }
    if (content) await loadCircles(content);
}

function fechaLegible(iso) {
    if (!iso) return '';
    const d = new Date(`${iso}T12:00:00`);
    if (isNaN(d.getTime())) return iso;
    const texto = d.toLocaleDateString('es-MX', { weekday: 'long', day: 'numeric', month: 'long' });
    return texto.charAt(0).toUpperCase() + texto.slice(1);
}

function horaRango(inicio, duracionMin) {
    if (!inicio) return '';
    const [h, m] = inicio.split(':').map(Number);
    const total = h * 60 + m + (Number(duracionMin) || 90);
    const eh = Math.floor(total / 60) % 24;
    const em = total % 60;
    const fin = `${String(eh).padStart(2, '0')}:${String(em).padStart(2, '0')}`;
    return `${inicio} - ${fin}`;
}

function renderEmptySessions() {
    return '<section class="card empty-circles"><h2>Aún no hay sesiones programadas</h2><p>Organiza una sesión de repaso o espera a que un compañero cree una.</p></section>';
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}