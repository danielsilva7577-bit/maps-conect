/**
 * Panel de inicio.
 * Contrato esperado de GET /inicio:
 * { perfil, materias, publicaciones, circuloActual, asesoria, empresaDestacada }
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('inicio.html');
    Layout.setPageTitle('Inicio');

    const content = document.getElementById('main-content');
    if (!content) return;

    content.innerHTML = '<div class="inicio-container"><p class="loading-message">Cargando tu información académica…</p></div>';

    let dashboard = {};
    try {
        dashboard = await API.request('/inicio');
    } catch {
        // El endpoint se implementará en el backend. La UI mantiene estados vacíos.
    }

    renderDashboard(content, dashboard || {});

    mostrarBienvenida();
});

function mostrarBienvenida() {
    if (document.documentElement.getAttribute('data-estilo') === 'observatorio') {
        const mostrar = () => window.Observatorio?.mostrarBienvenida?.();
        if (window.Observatorio?.mostrarBienvenida) {
            mostrar();
        } else {
            // El módulo del observatorio se carga de forma diferida desde
            // estilos.js; espera su evento sin bloquear el dashboard.
            document.addEventListener('observatorio:listo', mostrar, { once: true });
        }
        return;
    }

    let pendiente = false;
    try {
        pendiente =
            localStorage.getItem('bienvenida-pendiente') === '1' ||
            sessionStorage.getItem('bienvenida-pendiente') === '1';
    } catch (e) {}
    if (!pendiente) return;

    try {
        sessionStorage.removeItem('bienvenida-pendiente');
        localStorage.removeItem('bienvenida-pendiente');
    } catch (e) {}

    const nombre = (Auth.getUser() && Auth.getUser().nombre) || '';

    const overlay = document.createElement('div');
    overlay.className = 'welcome-overlay';
    overlay.id = 'welcomeOverlay';
    overlay.setAttribute('aria-hidden', 'true');
    overlay.innerHTML = `
        <div class="welcome-popup">
            <div class="popup-tag">
                <span class="pulse-mini"></span>
                <span>MAPS CONNECT // ACCESS GRANTED</span>
            </div>
            <h1 class="popup-title">BIENVENIDO</h1>
            <div class="popup-username" id="popupUserName">${escapeHtml(nombre || 'Usuario').toUpperCase()}</div>
            <p class="popup-status">SESIÓN INICIADA • CARGANDO ENTORNO HUD...</p>
        </div>
    `;
    document.body.appendChild(overlay);

    setTimeout(() => {
        overlay.classList.add('fade-out');
        // Elimina el overlay del DOM al terminar la transición para que nunca se quede pegado.
        setTimeout(() => {
            if (overlay.parentNode) overlay.parentNode.removeChild(overlay);
        }, 550);
    }, 1500);
}

function renderDashboard(content, data) {
    const perfil = data.perfil || null;
    const esDocente = perfil?.rol === 'PROFESOR';
    const materias = Array.isArray(data.materias) ? data.materias : [];
    const publicaciones = Array.isArray(data.publicaciones) ? data.publicaciones : [];
    const accesos = renderAccessCards(data);

    content.innerHTML = `
        <div class="inicio-container">
            ${perfil ? (esDocente ? renderDocenteBanner(perfil) : renderBanner(perfil)) : ''}
            ${data.carreraFiltrada && !esDocente ? `<p class="filter-note">Mostrando solo contenido de tu carrera: <strong>${escapeHtml(data.carreraFiltrada)}</strong></p>` : ''}
            <div class="main-layout">
                <section class="feed-column" aria-label="Actividad reciente">
                    ${!esDocente && materias.length ? renderQuickQuestion(materias) : ''}
                    ${publicaciones.length ? publicaciones.map(renderPost).join('') : renderEmptyFeed()}
                </section>
                ${accesos ? `<aside class="sidebar-section" aria-label="Accesos directos">${accesos}</aside>` : ''}
            </div>
        </div>
    `;

    document.getElementById('quick-question-form')?.addEventListener('submit', async event => {
        event.preventDefault();
        const question = document.getElementById('quick-question')?.value.trim() || '';
        const materia = document.getElementById('quick-question-materia')?.value;
        const idMateria = materia ? Number(materia) : null;
        const submit = event.currentTarget.querySelector('button[type="submit"]');

        if (!question) return;

        submit.disabled = true;
        submit.textContent = 'Publicando...';
        try {
            await API.request('/foro', {
                method: 'POST',
                body: JSON.stringify({
                    titulo: question,
                    contenido: question,
                    idMateria
                })
            });
            Utils.toast('Duda publicada correctamente.', 'success');
            window.location.href = 'comunidad.html#foro-dudas';
        } catch (error) {
            if (error.status === 409) {
                Utils.toast('Ya existe una duda similar. Revísala en la comunidad.', 'info');
                window.location.href = 'comunidad.html#foro-dudas';
                return;
            }
            Utils.toast(error.message || 'No se pudo publicar la duda.', 'error');
            submit.disabled = false;
            submit.textContent = 'Publicar duda';
        }
    });

    content.addEventListener('click', event => {
        const followBtn = event.target.closest('.follow-btn');
        if (!followBtn) return;
        event.preventDefault();
        const id = followBtn.getAttribute('data-id');
        if (!id) return;
        Utils.alternarSeguir(followBtn, id);
    });
}

function renderDocenteBanner(perfil) {
    return `
        <section class="card maps-banner prof-card" aria-labelledby="prof-title">
            <div class="banner-content">
                <div>
                    <h2 id="prof-title">Bienvenido, Docente</h2>
                    <p>Panel de docencia MAPS. Aquí aparecerán los avisos y materiales que compartes con tu comunidad.</p>
                </div>
                <span class="badge badge-verified">Perfil Docente</span>
            </div>
        </section>
    `;
}

function renderBanner(perfil) {
    const semestre = Number(perfil.semestre) || 0;
    const totalSemestres = Number(perfil.totalSemestres) || 0;
    const avance = totalSemestres ? Math.min(100, Math.round((semestre / totalSemestres) * 100)) : 0;

    return `
        <section class="card maps-banner" aria-labelledby="maps-title">
            <div class="banner-content">
                <div>
                    <h2 id="maps-title">${escapeHtml(perfil.carrera || 'Mi trayectoria MAPS')}</h2>
                    <p>${perfil.certificado ? `Certificado Activo: <strong>${escapeHtml(perfil.certificado)}</strong>` : 'Sin certificado activo'}${semestre ? ` • Semestre Vigente: <strong>${semestre}°</strong>` : ''}</p>
                </div>
                <span class="badge badge-materia">Plan MAPS Oficial</span>
            </div>
            ${totalSemestres ? `<div class="progress-container"><div class="progress-labels"><span>Avance de Carrera</span><span>Semestre ${semestre} de ${totalSemestres} (${avance}%)</span></div><div class="progress-bar" role="progressbar" aria-label="Avance de carrera" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${avance}"><div class="progress-fill" style="width: ${avance}%"></div></div></div>` : ''}
        </section>
    `;
}

function renderQuickQuestion(materias) {
    const options = materias.map(materia => `<option value="${escapeHtml(materia.id ?? '')}">${escapeHtml(materia.nombre || materia)}</option>`).join('');
    return `
        <form class="card create-post" id="quick-question-form">
            <label class="visually-hidden" for="quick-question">Nueva duda académica</label>
            <input id="quick-question" name="question" type="text" maxlength="180" placeholder="¿Tienes una duda académica? Pregunta a tu comunidad..." required>
            <div class="create-post-actions"><select id="quick-question-materia" aria-label="Materia de la duda"><option value="">Seleccionar materia...</option>${options}</select><button class="btn-solid" type="submit">Publicar duda</button></div>
        </form>
    `;
}

function renderPost(post) {
    const author = (post.autor && typeof post.autor === 'object') ? post.autor : {};
    const esDocente = author.tipo === 'profesor' || author.tipo === 'docente' || Boolean(post.verificadoDocente);
    const esMaterial = post.tipoContenido === 'material';
    const nombre = author.nombre || 'Usuario';
    const meta = esMaterial
        ? ['Docente', post.materia ? `de ${post.materia}` : ''].filter(Boolean).join(' ')
        : (author.semestre ? `${author.semestre}° Semestre` : 'Estudiante');
    const tiempo = post.fechaRelativa ? ` • ${post.fechaRelativa}` : '';
    const meId = Auth.getUser() && Auth.getUser().id;
    const clickeable = author.id && author.id !== meId;
    const hrefAutor = `usuario.html?id=${encodeURIComponent(author.id)}`;
    const avatarWrap = clickeable
        ? `<a class="user-link" href="${hrefAutor}" aria-label="Ver perfil de ${escapeHtml(nombre)}">${Utils.avatarHtml(nombre, author.foto, `avatar ${esDocente ? 'prof' : ''}`, nombre)}</a>`
        : Utils.avatarHtml(nombre, author.foto, `avatar ${esDocente ? 'prof' : ''}`, nombre);
    const nombreWrap = clickeable
        ? `<a class="user-link" href="${hrefAutor}"><strong>${escapeHtml(nombre)}</strong></a>`
        : `<strong>${escapeHtml(nombre)}</strong>`;
    const seguirBoton = (author.id && author.id !== meId)
        ? `<button class="follow-btn${author.seguido ? ' siguiendo' : ''}" type="button" data-id="${author.id}" data-siguiendo="${author.seguido ? 'true' : 'false'}">${author.seguido ? 'Siguiendo' : 'Seguir'}</button>`
        : '';

    const tags = [
        post.materia && `<span class="badge badge-materia">${escapeHtml(post.materia)}</span>`,
        post.participado && '<span class="badge badge-interacted">Has participado</span>',
        post.solucionAceptada && '<span class="badge badge-solved">Solución Aceptada</span>',
        esDocente && '<span class="badge badge-verified">Aviso Docente</span>',
    ].filter(Boolean).join('');

    const embed = esMaterial && post.archivo
        ? `<div class="embed-box prof-box"><strong style="color:#0284c7;">Archivo disponible:</strong><p>${escapeHtml(post.archivo.nombre)} (${escapeHtml(post.archivo.peso)}) • ${escapeHtml(post.archivo.descargas)} descargas de tu carrera</p></div>`
        : (post.respuestaUsuario ? `<div class="embed-box"><strong>Tu respuesta:</strong><p>${escapeHtml(post.respuestaUsuario)}</p></div>` : '');

    const votos = Number(post.votos);
    const metricaVotos = Number.isFinite(votos)
        ? `<span class="btn-action" aria-label="${votos} votos">${votos} Votos</span>`
        : '';
    const enlaceForo = 'comunidad.html#foro-dudas';
    const botonRespuestas = `<a class="btn-action" href="${enlaceForo}">${Number(post.respuestas) || 0} ${esMaterial ? 'Comentarios' : 'Respuestas'}</a>`;
    const tercerBoton = esMaterial
        ? '<a class="btn-action" href="comunidad.html#apuntes">Ver recursos</a>'
        : `<a class="btn-action" href="${enlaceForo}">Ver en el foro</a>`;
    const status = esMaterial ? 'Material Oficial' : (post.solucionAceptada ? 'Resuelto' : '');

    return `
        <article class="card post-card">
            <div class="post-header"><div class="user-block">${avatarWrap}<div class="user-data"><div class="user-data-line">${nombreWrap}${seguirBoton}</div><span>${escapeHtml(meta + tiempo)}</span></div></div><div class="badges-wrap">${tags}</div></div>
            <h3 class="post-title">${escapeHtml(post.titulo || '')}</h3>
            ${post.contenido ? `<p class="post-body">${escapeHtml(post.contenido)}</p>` : ''}
            ${embed}
            <div class="post-footer"><div class="action-links">${metricaVotos}${botonRespuestas}${tercerBoton}</div>${status ? `<span class="post-status">${escapeHtml(status)}</span>` : ''}</div>
        </article>
    `;
}

function renderAccessCards(data) {
    const cards = [];
    const circulo = data.circuloActual;
    const asesoria = data.asesoria;
    const empresa = data.empresaDestacada;

    if (circulo) cards.push(`<section class="card side-card"><span class="badge badge-materia">${escapeHtml(circulo.fecha || 'Próximamente')}</span><h4>${escapeHtml(circulo.nombre || 'Círculo de estudio')}</h4>${circulo.descripcion ? `<p>${escapeHtml(circulo.descripcion)}</p>` : ''}<a class="btn-solid" style="width:100%;" href="circulos.html">${circulo.plataforma ? `Entrar a Sala (${escapeHtml(circulo.plataforma)})` : 'Ver círculo'}</a></section>`);
    if (asesoria) cards.push(`<section class="card side-card prof-card"><span class="badge badge-verified">Asesoría Disponible</span><h4>${escapeHtml(asesoria.docente || 'Docente')}</h4>${asesoria.horario ? `<p>Horario de atención para dudas del certificado: <strong>${escapeHtml(asesoria.horario)}</strong></p>` : ''}<a class="btn-clean" href="mensajes.html">Enviar Mensaje Privado</a></section>`);
    if (empresa) cards.push(`<section class="card side-card"><h4>Radar Semestre Empresarial</h4><p><strong>${escapeHtml(empresa.nombre || '')}${empresa.calificacion ? ` • ${escapeHtml(empresa.calificacion)}` : ''}</strong>${empresa.resena ? `<br>${escapeHtml(empresa.resena)}` : ''}</p><a class="side-link" href="empresarial.html">Ver directorio de empresas →</a></section>`);

    // El inicio debe conservar los accesos principales aunque todavía no haya
    // una sesión o asesoría asignada por la API.
    if (!circulo) {
        cards.unshift(
            '<section class="card side-card obs-access-card">' +
            '<span class="badge badge-materia">Círculos de estudio</span>' +
            '<h4>Encuentra tu próxima sesión</h4>' +
            '<p>Explora los repasos abiertos y únete al círculo de la materia que necesitas.</p>' +
            '<a class="btn-solid" style="width:100%;" href="circulos.html">Explorar círculos</a>' +
            '</section>'
        );
    }

    if (!asesoria) {
        const mentoriaCard =
            '<section class="card side-card prof-card obs-access-card">' +
            '<span class="badge badge-verified">Mentoría disponible</span>' +
            '<h4>Acompañamiento docente</h4>' +
            '<p>Consulta tus opciones de asesoría y envía una duda a tu docente.</p>' +
            '<a class="btn-clean" href="mensajes.html">Enviar mensaje</a>' +
            '</section>';

        // La tarjeta empresarial, cuando existe, siempre queda al final.
        cards.splice(empresa ? cards.length - 1 : cards.length, 0, mentoriaCard);
    }

    return cards.join('');
}

function renderEmptyFeed() {
    return '<section class="card empty-state"><h2>Aún no hay actividad</h2><p>Las dudas, avisos y recursos de tu comunidad aparecerán aquí cuando estén disponibles.</p><a class="side-link" href="comunidad.html#foro-dudas">Ir al foro →</a></section>';
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}
