/**
 * Directorio de Semestre Empresarial.
 * Contrato esperado de GET /empresarial:
 * { empresas: [{ id, nombre, sector, calificacion,
 *   totalResenas, descripcion, experienciaDestacada }] }
 * Reseñas: GET/POST/PUT/DELETE /empresarial/{id}/resenas.
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('empresarial.html');
    Layout.setPageTitle('Semestre Empresarial');

    const content = document.getElementById('main-content');
    if (!content) return;

    content.innerHTML = '<div class="empresarial-container"><p class="loading-message">Cargando empresas vinculadas…</p></div>';
    content.addEventListener('click', event => {
        const target = event.target.closest('button[data-action]');
        if (!target) return;
        if (target.dataset.action === 'experiencias' || target.dataset.action === 'resenar') {
            const id = target.dataset.companyId;
            if (id) abrirModalEmpresa(id, target.dataset.action === 'resenar');
        } else if (target.dataset.action === 'requisitos') {
            Utils.toast('Los requisitos de vinculación se habilitan cuando el backend de convenios esté integrado.', 'info');
        }
    });
    await loadCompanies(content);
});

let empresasActuales = [];

async function loadCompanies(content, filters = {}) {
    const params = new URLSearchParams();
    if (filters.busqueda) params.set('busqueda', filters.busqueda);
    if (filters.calificacionMinima) params.set('calificacionMinima', filters.calificacionMinima);

    let response = {};
    try {
        response = await API.request(`/empresarial${params.size ? `?${params}` : ''}`);
    } catch {
        // El controlador todavía no expone datos; se conserva el estado vacío.
    }

    const companies = Array.isArray(response) ? response : (Array.isArray(response.empresas) ? response.empresas : []);
    const carreraFiltrada = response && !Array.isArray(response) ? response.carreraFiltrada || '' : '';
    empresasActuales = companies;
    renderCompanies(content, companies, filters.sector || '', carreraFiltrada);
}

function renderCompanies(content, companies, sectorSeleccionado = '', carreraFiltrada = '') {
    const sectores = [...new Set((companies || [])
        .map(c => (c.sector || '').trim())
        .filter(Boolean))]
        .sort((a, b) => a.localeCompare(b, 'es'));
    const sectorOptions = sectores
        .map(s => `<option value="${escapeHtml(s)}"${s === sectorSeleccionado ? ' selected' : ''}>${escapeHtml(s)}</option>`)
        .join('');

    const visibles = sectorSeleccionado
        ? companies.filter(c => (c.sector || '').trim() === sectorSeleccionado)
        : companies;

    content.innerHTML = `
        <div class="empresarial-container">
            <header class="module-heading"><h1>Semestre Empresarial</h1><p>Explora empresas vinculadas y experiencias compartidas por la comunidad.</p>${carreraFiltrada ? `<p class="filter-note">Mostrando empresas afines a tu carrera: <strong>${escapeHtml(carreraFiltrada)}</strong></p>` : ''}</header>
            <form class="filter-bar" id="company-filter-form">
                <label class="visually-hidden" for="company-search">Buscar empresa</label>
                <input class="input-search" id="company-search" name="busqueda" type="search" placeholder="Buscar por empresa, tecnología o proyecto...">
                <select class="select-filter" name="sector" aria-label="Filtrar por sector"><option value="">Todos los sectores</option>${sectorOptions}</select>
                <select class="select-filter" name="calificacionMinima" aria-label="Filtrar por calificación"><option value="">Calificación: todas</option><option value="4.5">4.5 estrellas o más</option><option value="4.0">4.0 estrellas o más</option></select>
                <button class="btn-solid" type="submit">Buscar</button>
            </form>
            <section id="company-results" aria-live="polite">${visibles.length ? visibles.map(renderCompany).join('') : renderEmptyCompanies()}</section>
        </div>
    `;

    document.getElementById('company-filter-form')?.addEventListener('submit', event => {
        event.preventDefault();
        const formData = new FormData(event.currentTarget);
        loadCompanies(content, Object.fromEntries(formData.entries()));
    });
}

function renderCompany(company) {
    const rating = Number(company.calificacion);
    const hasRating = Number.isFinite(rating) && rating > 0;
    const location = [company.ciudad, company.estado].filter(Boolean).join(', ');
    const technologies = Array.isArray(company.tecnologias) ? company.tecnologias : [];
    const experience = company.experienciaDestacada;

    const tags = [];
    if (company.sector) tags.push(`<span class="badge badge-blue">Sector: ${escapeHtml(company.sector)}</span>`);
    if (company.convenioActivo) tags.push('<span class="badge badge-green">Convenio oficial activo</span>');
    tags.push(...technologies.map(item => `<span class="badge badge-blue">${escapeHtml(item)}</span>`));

    return `
        <article class="card company-card">
            <header class="company-header">
                <div><h2 class="company-name">${escapeHtml(company.nombre || 'Empresa')}</h2>${location ? `<span class="company-location">${escapeHtml(location)}</span>` : ''}</div>
                ${hasRating ? `<div class="rating-box"><span class="stars" aria-label="${rating} de 5 estrellas">${renderStars(rating)}</span><strong class="rating-number">${rating.toFixed(1)}</strong>${Number.isFinite(Number(company.totalResenas)) ? `<span class="rating-count">(${Number(company.totalResenas)} reseñas)</span>` : ''}</div>` : ''}
            </header>
            ${company.descripcion ? `<p class="company-desc"><strong>¿Qué hacen?</strong> ${escapeHtml(company.descripcion)}</p>` : ''}
            ${tags.length ? `<div class="tags-group">${tags.join('')}</div>` : ''}
            ${experience ? renderExperience(experience) : ''}
            <footer class="company-footer"><button class="btn-clean" type="button" data-action="experiencias" data-company-id="${escapeHtml(company.id ?? '')}">Ver experiencias${Number.isFinite(Number(company.totalResenas)) ? ` (${Number(company.totalResenas)})` : ''}</button><button class="btn-solid" type="button" data-action="resenar" data-company-id="${escapeHtml(company.id ?? '')}">Compartir tu experiencia</button></footer>
        </article>
    `;
}

function renderExperience(experience) {
    const author = [experience.autor, experience.semestre && `${experience.semestre}.º semestre`].filter(Boolean).join(' · ');
    return `<div class="experience-box">${author ? `<strong class="experience-author">${escapeHtml(author)}</strong>` : ''}${experience.texto ? `<p class="experience-text"><em>“${escapeHtml(experience.texto)}”</em></p>` : ''}</div>`;
}

function renderEmptyCompanies() {
    return '<section class="card empty-companies"><h2>Aún no hay empresas disponibles</h2><p>Las empresas vinculadas y las experiencias de estudiantes aparecerán aquí cuando se registren.</p></section>';
}

function renderStars(rating) {
    const filled = Math.round(rating);
    return '★'.repeat(Math.min(5, filled)) + '☆'.repeat(Math.max(0, 5 - filled));
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}

// ------- Modal de experiencias y reseñas -------

let modalBackdrop = null;
let modalEmpresaId = null;
let modalEmpresa = null;
let calificacionSeleccionada = 0;
let resenaEnviandose = false;

function abrirModalEmpresa(companyId, enfocarFormulario) {
    calificacionSeleccionada = 0;
    modalEmpresaId = String(companyId);
    modalEmpresa = empresasActuales.find(empresa => String(empresa.id) === modalEmpresaId) || null;

    if (modalBackdrop) modalBackdrop.remove();

    const rating = Number(modalEmpresa?.calificacion);
    const headerSub = Number.isFinite(rating) && rating > 0
        ? `Calificación promedio ${rating.toFixed(1)} ★ · ${modalEmpresa.totalResenas ?? 0} reseñas`
        : 'Aún sin calificaciones';

    modalBackdrop = document.createElement('div');
    modalBackdrop.className = 'empresa-modal-backdrop';
    modalBackdrop.innerHTML = `
        <div class="empresa-modal" role="dialog" aria-modal="true" aria-label="Experiencias de ${escapeHtml(modalEmpresa?.nombre || 'la empresa')}">
            <header class="empresa-modal-header">
                <div>
                    <h2 class="empresa-modal-title">${escapeHtml(modalEmpresa?.nombre || 'Empresa')}</h2>
                    <span class="empresa-modal-sub">${escapeHtml(headerSub)}</span>
                </div>
                <button class="empresa-modal-close" type="button" aria-label="Cerrar">&times;</button>
            </header>
            <div class="empresa-modal-body">
                <div id="experiencias-list"><p class="loading-message">Cargando experiencias…</p></div>
                <div id="resena-form-section"></div>
            </div>
            <footer class="empresa-modal-footer"><button class="btn-clean" type="button" data-modal-close>Cerrar</button></footer>
        </div>
    `;
    document.body.appendChild(modalBackdrop);
    document.body.classList.add('empresa-modal-open');

    modalBackdrop.addEventListener('click', event => {
        if (event.target === modalBackdrop || event.target.closest('[data-modal-close]')) {
            cerrarModalEmpresa();
            return;
        }

        const btn = event.target.closest('button[data-action="editar-resena"], button[data-action="eliminar-resena"]');
        if (!btn) return;

        if (btn.dataset.action === 'editar-resena') {
            abrirEdicionResena();
            return;
        }

        if (Number(btn.dataset.step) === 0) {
            btn.dataset.step = '1';
            btn.textContent = '¿Seguro?';
            setTimeout(() => {
                btn.dataset.step = '0';
                btn.textContent = 'Eliminar';
            }, 3500);
            return;
        }
        btn.disabled = true;
        eliminarMiResena(btn);
    });

    cargarExperiencias();
    if (enfocarFormulario) {
        setTimeout(() => document.getElementById('resena-form-section')?.scrollIntoView({ behavior: 'smooth', block: 'center' }), 150);
    }
}

function cerrarModalEmpresa() {
    modalBackdrop?.remove();
    modalBackdrop = null;
    modalEmpresaId = null;
    modalEmpresa = null;
    document.body.classList.remove('empresa-modal-open');
}

let modalResenas = [];
let modalMiResena = null;

async function cargarExperiencias() {
    const listContainer = modalBackdrop?.querySelector('#experiencias-list');
    const formContainer = modalBackdrop?.querySelector('#resena-form-section');
    if (!listContainer || !modalEmpresaId) return;

    listContainer.innerHTML = '<p class="loading-message">Cargando experiencias…</p>';
    formContainer.innerHTML = '';

    let reviews = [];
    try {
        const response = await API.request(`/empresarial/${modalEmpresaId}/resenas`);
        reviews = Array.isArray(response) ? response : [];
    } catch {
        listContainer.innerHTML = '<p class="loading-message">No se pudieron cargar las experiencias. Inténtalo de nuevo.</p>';
        return;
    }

    modalResenas = reviews;
    modalMiResena = reviews.find(review => review.miResena) || null;

    listContainer.innerHTML = reviews.length
        ? reviews.map(renderReview).join('')
        : '<p class="empty-experiences">Aún no hay experiencias compartidas en esta empresa. Sé la primera persona en compartir la tuya.</p>';

    formContainer.innerHTML = modalMiResena
        ? `<div class="experiencias-section"><p class="my-experience-note">Ya compartiste tu experiencia en esta empresa. Puedes <button type="button" class="link-action" data-action="editar-resena">editar</button> o <button type="button" class="link-action" data-action="eliminar-resena" data-step="0">eliminar</button> tu aporte.</p><div id="resena-edit-wrap"></div></div>`
        : renderResenaForm();
    if (!modalMiResena) vincularFormulario('crear', null);
}

function renderReview(review) {
    const destacada = review.miResena ? ' review-mine' : '';
    const fecha = formatFecha(review.fecha);
    const acciones = review.miResena
        ? `<div class="review-actions"><button class="btn-clean" type="button" data-action="editar-resena">Editar</button><button class="btn-clean btn-danger" type="button" data-action="eliminar-resena" data-step="0">Eliminar</button></div>`
        : '';
    return `
        <article class="review-item${destacada}">
            <header class="review-head">
                <div class="review-stars" aria-label="${review.calificacion} de 5 estrellas">${renderStars(review.calificacion)}</div>
                <div class="review-meta"><strong class="review-author">${escapeHtml(review.autor || 'Estudiante')}</strong>${fecha ? `<span class="review-date">${fecha}</span>` : ''}${review.miResena ? '<span class="badge badge-green">Tu experiencia</span>' : ''}</div>
            </header>
            ${review.proyectoDesarrollado ? `<p class="review-field"><strong>Proyecto desarrollado:</strong> ${escapeHtml(review.proyectoDesarrollado)}</p>` : ''}
            ${review.aprendizajes ? `<p class="review-field"><strong>¿Qué aprendió?</strong> ${escapeHtml(review.aprendizajes)}</p>` : ''}
            ${review.recomendaciones ? `<p class="review-field"><strong>Recomendaciones:</strong> ${escapeHtml(review.recomendaciones)}</p>` : ''}
            ${acciones}
        </article>
    `;
}

function abrirEdicionResena() {
    const wrap = modalBackdrop?.querySelector('#resena-edit-wrap');
    if (!wrap || !modalMiResena) return;
    const base = modalMiResena;
    wrap.innerHTML = renderResenaForm(base, 'editar');
    vincularFormulario('editar', base);
    wrap.scrollIntoView({ behavior: 'smooth', block: 'neutral' });
}

function cancelarEdicionResena() {
    const wrap = modalBackdrop?.querySelector('#resena-edit-wrap');
    if (wrap) wrap.innerHTML = '';
}

function renderResenaForm(valores = null, modo = 'crear') {
    const stars = [1, 2, 3, 4, 5].map(value => `<button type="button" class="star-btn${valores?.calificacion && Number(valores.calificacion) >= value ? ' is-selected' : ''}" data-value="${value}" aria-label="${value} estrellas" aria-pressed="${valores?.calificacion && Number(valores.calificacion) >= value ? 'true' : 'false'}">★</button>`).join('');
    const titulo = modo === 'editar' ? 'Edita tu experiencia' : 'Comparte tu experiencia';
    const textoSubmit = modo === 'editar' ? 'Guardar cambios' : 'Compartir experiencia';
    return `
        <div class="experiencias-section resena-form-wrap">
            <h3 class="resena-title">${titulo}</h3>
            <form class="resena-form" id="${modo === 'editar' ? 'resena-form-editar' : 'resena-form'}" novalidate>
                <div class="field-row">
                    <span class="resena-label">Tu calificación</span>
                    <div class="star-picker" role="radiogroup" aria-label="Calificación">
                        ${stars}
                    </div>
                    <span class="star-picker-label" id="star-picker-label">${valores?.calificacion ? (String(valores.calificacion) === '1' ? '1 estrella' : `${valores.calificacion} estrellas`) : 'Selecciona de 1 a 5 estrellas'}</span>
                </div>
                <div class="field-row">
                    <label class="resena-label" for="resena-proyecto">Proyecto desarrollado</label>
                    <textarea class="resena-input" id="resena-proyecto" rows="2" maxlength="2000" placeholder="Describe brevemente el proyecto o actividad que desarrollaste...">${escapeHtml(valores?.proyectoDesarrollado || '')}</textarea>
                </div>
                <div class="field-row">
                    <label class="resena-label" for="resena-aprendizajes">¿Qué aprendiste?</label>
                    <textarea class="resena-input" id="resena-aprendizajes" rows="3" maxlength="2000" placeholder="Comparte los aprendizajes más valiosos de tu experiencia...">${escapeHtml(valores?.aprendizajes || '')}</textarea>
                </div>
                <div class="field-row">
                    <label class="resena-label" for="resena-recomendaciones">Recomendaciones para otros estudiantes</label>
                    <textarea class="resena-input" id="resena-recomendaciones" rows="2" maxlength="2000" placeholder="Consejos para quien viva el Semestre Empresarial aquí...">${escapeHtml(valores?.recomendaciones || '')}</textarea>
                </div>
                <div class="field-row resena-actions">
                    ${modo === 'editar' ? '<button class="btn-clean" type="button" id="resena-cancelar">Cancelar</button>' : ''}
                    <button class="btn-solid" type="submit" id="resena-submit">${textoSubmit}</button>
                </div>
            </form>
        </div>
    `;
}

function vincularFormulario(modo, valores) {
    const formId = modo === 'editar' ? '#resena-form-editar' : '#resena-form';
    const form = modalBackdrop?.querySelector(formId);
    if (!form) return;

    if (modo === 'editar') {
        calificacionSeleccionada = Number(valores?.calificacion) || 0;
    } else {
        calificacionSeleccionada = 0;
    }

    form.querySelectorAll('.star-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            calificacionSeleccionada = Number(btn.dataset.value);
            form.querySelectorAll('.star-btn').forEach(star => {
                const activo = Number(star.dataset.value) <= calificacionSeleccionada;
                star.classList.toggle('is-selected', activo);
                star.setAttribute('aria-pressed', String(activo));
            });
            const label = form.querySelector('#star-picker-label');
            if (label) {
                label.textContent = calificacionSeleccionada === 1
                    ? '1 estrella'
                    : `${calificacionSeleccionada} estrellas`;
            }
        });
    });

    form.querySelector('#resena-cancelar')?.addEventListener('click', () => cancelarEdicionResena());

    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (resenaEnviandose) return;
        if (!calificacionSeleccionada) {
            Utils.toast('Selecciona una calificación de 1 a 5 estrellas.', 'error');
            return;
        }

        resenaEnviandose = true;
        const submitBtn = form.querySelector('#resena-submit');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;

        const body = {
            calificacion: calificacionSeleccionada,
            proyectoDesarrollado: form.querySelector('#resena-proyecto').value.trim(),
            aprendizajes: form.querySelector('#resena-aprendizajes').value.trim(),
            recomendaciones: form.querySelector('#resena-recomendaciones').value.trim()
        };

        try {
            await API.request(`/empresarial/${modalEmpresaId}/resenas`, {
                method: modo === 'editar' ? 'PUT' : 'POST',
                body: JSON.stringify(body)
            });
            Utils.toast(modo === 'editar'
                ? 'Tu experiencia fue actualizada correctamente.'
                : 'Experiencia compartida correctamente. Gracias por aportar a la comunidad.', 'success');
            await cargarExperiencias();
            recargarEmpresas();
        } catch (error) {
            Utils.toast(error.message || 'No se pudo guardar tu experiencia. Inténtalo de nuevo.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
            resenaEnviandose = false;
        }
    });
}

async function eliminarMiResena(btn) {
    try {
        await API.request(`/empresarial/${modalEmpresaId}/resenas`, { method: 'DELETE' });
        Utils.toast('Tu experiencia fue eliminada.', 'success');
        await cargarExperiencias();
        recargarEmpresas();
    } catch (error) {
        btn.disabled = false;
        btn.dataset.step = '0';
        btn.textContent = 'Eliminar';
        Utils.toast(error.message || 'No se pudo eliminar tu experiencia.', 'error');
    }
}

async function recargarEmpresas() {
    const content = document.getElementById('main-content');
    if (content) await loadCompanies(content);
}

function formatFecha(value) {
    if (!value) return '';
    const cleaned = String(value).replace('T', ' ');
    const match = cleaned.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (!match) return '';
    return `${match[3]}/${match[2]}/${match[1]}`;
}