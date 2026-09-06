/**
 * Mi Ruta MAPS - Certificados del modelo académico.
 * Contrato esperado de GET /estudiantes/ruta:
 * { estudiante: { id, matricula, semestre, carrera },
 *   ruta: [{ id, nombre, descripcion, semestreMin, semestreMax, materias[], enRuta }],
 *   catalogo: [ { id, nombre, descripcion, semestreMin, semestreMax, materias[], enRuta } ] }
 * ASIGNACION: POST /certificados/asignar { idEstudiante, idCertificado }.
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('certificados.html');
    Layout.setPageTitle('Mi Ruta MAPS');

    const content = document.getElementById('main-content');
    if (!content) return;

    const user = Auth.getUser();
    if (!user || user.rol === 'PROFESOR') {
        renderVistaProfesor(content);
        return;
    }

    content.innerHTML = '<div class="ruta-container"><p class="loading-message">Cargando tu ruta MAPS…</p></div>';
    content.addEventListener('click', handlerAgregar);

    let data = {};
    try {
        data = await API.request('/estudiantes/ruta');
    } catch (error) {
        const sinPerfil = /completa tu perfil|perfil de estudiante aún no está completo/i.test(error?.message || '');
        renderError(content, sinPerfil);
        return;
    }

    renderRuta(content, data || {});
});

function renderVistaProfesor(content) {
    content.innerHTML = `
        <div class="ruta-container">
            <header class="module-heading"><h1>Mi Ruta MAPS</h1><p>Panel de la trayectoria de certificados de los estudiantes.</p></header>
            <section class="card empty-state-box">
                <h2>Vista exclusiva para estudiantes</h2>
                <p>La ruta MAPS agrupa los certificados y sus materias por semestre sugerido. Como docente, puedes orientar a tus estudiantes desde tu vista de docencia.</p>
                <a class="btn-clean" href="inicio.html">Volver a inicio</a>
            </section>
        </div>
    `;
}

function renderError(content, sinPerfil) {
    content.innerHTML = `
        <div class="ruta-container">
            <header class="module-heading"><h1>Mi Ruta MAPS</h1><p>Explora y arma la ruta de certificados de tu carrera.</p></header>
            <section class="card empty-state-box">
                <h2>${sinPerfil ? 'Completa tu perfil de estudiante' : 'No se pudo cargar tu ruta'}</h2>
                <p>${sinPerfil
                    ? 'Para ver tu ruta MAPS primero debes completar tu perfil de estudiante y elegir tu carrera.'
                    : 'Ocurrió un problema al consultar tu ruta. Inténtalo de nuevo en un momento.'}</p>
                ${sinPerfil ? '<a class="btn-solid" href="onboarding.html">Completar mi perfil</a>' : ''}
            </section>
        </div>
    `;
}

let estadoRuta = { estudiante: null, ruta: [], catalogo: [] };

function renderRuta(content, data) {
    estadoRuta = {
        estudiante: data.estudiante || null,
        ruta: Array.isArray(data.ruta) ? data.ruta : [],
        catalogo: Array.isArray(data.catalogo) ? data.catalogo : []
    };

    const estudiante = estadoRuta.estudiante;
    const semestreActual = Number(estudiante?.semestre) || 0;
    const maxCertificados = 3;
    const enRuta = estadoRuta.ruta.length;

    content.innerHTML = `
        <div class="ruta-container">
            <header class="module-heading">
                <h1>Mi Ruta MAPS</h1>
                <p>Tu plan de certificados ${estudiante?.carrera ? `en <strong>${escapeHtml(estudiante.carrera)}</strong>` : ''}, ordenado por el semestre sugerido de sus materias.</p>
            </header>
            ${estudiante ? renderBanner(estudiante, semestreActual, enRuta, maxCertificados) : ''}
            <section id="ruta-section" class="ruta-section" aria-labelledby="ruta-title">
                <h2 class="ruta-section-title" id="ruta-title">Tu ruta ${enRuta ? `<small>${enRuta} de ${maxCertificados} certificados</small>` : ''}</h2>
                ${enRuta ? estadoRuta.ruta.map(renderRutaBlock).join('') : renderRutaVacia()}
            </section>
            <section class="ruta-section" aria-labelledby="catalogo-title">
                <h2 class="ruta-section-title" id="catalogo-title">Catálogo de certificados</h2>
                ${estadoRuta.catalogo.map(cert => renderCatalogoBlock(cert, semestreActual, enRuta, maxCertificados)).join('')}
            </section>
            <p class="ruta-footer-note">Sugerencia: arma tu ruta con hasta ${maxCertificados} certificados según los intereses de tu carrera.</p>
        </div>
    `;
}

function renderBanner(estudiante, semestreActual, enRuta, maxCertificados) {
    const bloque = semestreActual
        ? `Semestre vigente: <strong>${semestreActual}°</strong>`
        : 'Sin semestre asignado';
    return `
        <section class="card ruta-banner" aria-labelledby="ruta-banner-title">
            <h2 id="ruta-banner-title">${escapeHtml(estudiante.matricula || 'Estudiante')}</h2>
            <p>${bloque} • Certificados seleccionados: <strong>${enRuta} de ${maxCertificados}</strong></p>
        </section>
    `;
}

function renderRutaVacia() {
    return `
        <section class="card empty-state-box">
            <h2>Aún no tienes certificados en tu ruta</h2>
            <p>Explora el catálogo de abajo y añade hasta 3 certificados relacionados con tu carrera. Seguirán su semestre sugerido en el plan de estudios.</p>
        </section>
    `;
}

function renderRutaBlock(cert) {
    return renderCertificadoCard(cert, true);
}

function renderCatalogoBlock(cert, semestreActual, enRuta, maxCertificados) {
    const lleno = enRuta >= maxCertificados && !cert.enRuta;
    return renderCertificadoCard(cert, false, lleno);
}

function renderCertificadoCard(cert, enRuta, lleno) {
    const rango = rangoSemestres(cert);
    const estado = estadoCertificado(cert, enRuta);
    const materias = Array.isArray(cert.materias) ? cert.materias : [];
    const chips = materias.map(materia =>
        `<span class="materia-chip">${escapeHtml(materia.nombre || 'Materia')}${Number.isFinite(Number(materia.semestre)) ? ` · S.${materia.semestre}` : ''}</span>`
    ).join('');

    const cuerpo = `
        <div class="ruta-block-head">
            <div>
                <h3 class="ruta-block-title">${escapeHtml(cert.nombre || 'Certificado')}</h3>
                ${rango ? `<span class="ruta-block-range">${escapeHtml(rango)}</span>` : ''}
            </div>
            <div class="ruta-badges">${estado.badges}</div>
        </div>
        ${cert.descripcion ? `<p class="ruta-block-desc">${escapeHtml(cert.descripcion)}</p>` : ''}
        ${chips ? `<div class="ruta-materias">${chips}</div>` : ''}
    `;

    const acciones = enRuta
        ? `<div class="ruta-actions"><button class="btn-clean" type="button" disabled>En tu ruta</button></div>`
        : `<div class="ruta-actions"><button class="btn-solid" type="button" data-action="agregar" data-cert-id="${escapeHtml(cert.id ?? '')}"${lleno ? ' disabled' : ''}>${lleno ? 'Ruta completa' : 'Añadir a mi ruta'}</button></div>`;

    return `
        <article class="card ruta-block">
            ${cuerpo}
            ${acciones}
        </article>
    `;
}

function rangoSemestres(cert) {
    const min = Number.isFinite(Number(cert.semestreMin)) ? Number(cert.semestreMin) : null;
    const max = Number.isFinite(Number(cert.semestreMax)) ? Number(cert.semestreMax) : null;
    if (!min && !max) return '';
    if (!min || !max) return `Semestre ${min || max}`;
    return min === max ? `Semestre ${min}` : `Semestres ${min} – ${max}`;
}

function estadoCertificado(cert, enRuta) {
    const semestreActual = Number(estadoRuta.estudiante?.semestre) || 0;
    const min = Number.isFinite(Number(cert.semestreMin)) ? Number(cert.semestreMin) : null;
    const max = Number.isFinite(Number(cert.semestreMax)) ? Number(cert.semestreMax) : null;

    let badge = '';
    if (enRuta) {
        badge = '<span class="badge-ruta">En tu ruta</span>';
    }

    if (min == null && max == null) {
        badge += ' <span class="badge-proximo">Sin semestre asociado</span>';
        return { badges: badge };
    }

    if (max != null && semestreActual > max) {
        badge += ' <span class="badge-cursado">Cursado</span>';
    } else if (semestreActual >= (min ?? 1) && (max == null || semestreActual <= max)) {
        badge += ' <span class="badge-encurso">En curso</span>';
    } else {
        badge += ' <span class="badge-proximo">Próximo</span>';
    }
    return { badges: badge };
}

async function handlerAgregar(event) {
    const btn = event.target.closest('button[data-action="agregar"]');
    if (!btn) return;

    const estudianteId = estadoRuta.estudiante?.id;
    const certificadoId = btn.dataset.certId;
    if (!estudianteId || !certificadoId) return;

    btn.disabled = true;
    try {
        await API.request('/certificados/asignar', {
            method: 'POST',
            body: JSON.stringify({ idEstudiante: Number(estudianteId), idCertificado: Number(certificadoId) })
        });
        Utils.toast('Certificado añadido a tu ruta MAPS.', 'success');
        const content = document.getElementById('main-content');
        if (content) {
            content.innerHTML = '<div class="ruta-container"><p class="loading-message">Actualizando tu ruta…</p></div>';
            const data = await API.request('/estudiantes/ruta');
            renderRuta(content, data || {});
        }
    } catch (error) {
        btn.disabled = false;
        Utils.toast(error.message || 'No se pudo añadir el certificado.', 'error');
    }
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}