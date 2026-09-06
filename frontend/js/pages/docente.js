/**
 * Panel Docente - Gestion y Acompanamiento Academico.
 * Contrato de GET /docente/stats:
 * { perfil: { nombre, nomina, area, horario, enlace, rol },
 *   kpis: { dudasPorValidar, circulosACargo, alumnosAsesorados, descargasApuntes },
 *   dudas: [{ id, titulo, contenido, materia, autor }],
 *   asesorias: [{ id, titulo, materia, fecha, hora, inscritos, cupoMax, enlace }] }
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('docente.html');
    Layout.setPageTitle('Gestion Docente');

    const content = document.getElementById('main-content');
    if (!content) return;

    content.innerHTML = '<div class="docente-container"><p class="loading-message">Cargando panel docente...</p></div>';

    let data = {};
    try {
        data = await API.request('/docente/stats');
    } catch (e) {
        content.innerHTML = `<div class="docente-container"><div class="card docente-empty">No se pudo cargar el panel: ${Utils.esc(e.message || 'sin permisos de profesor')}</div></div>`;
        return;
    }

    renderDocente(content, data || {});
});

function renderDocente(content, data) {
    const perfil = data.perfil || {};
    const kpis = data.kpis || {};
    const dudas = Array.isArray(data.dudas) ? data.dudas : [];
    const asesorias = Array.isArray(data.asesorias) ? data.asesorias : [];

    content.innerHTML = `
        <div class="docente-container">
            <header class="card docente-header">
                <div class="header-info">
                    <h2>Espacio de Acompanamiento Academico</h2>
                    <p>
                        Profesor: <strong>${Utils.esc(perfil.nombre || '—')}</strong>
                        &bull; Nomina: <strong>${Utils.esc(perfil.nomina || '—')}</strong>
                        &bull; ${Utils.esc(perfil.area || '—')}
                    </p>
                </div>
                <div>
                    <span class="docente-badge">Rol: ${Utils.esc(perfil.rol || 'Asesor Docente')}</span>
                </div>
            </header>

            <div class="docente-kpi-grid">
                <div class="docente-kpi">
                    <div class="val">${Number(kpis.dudasPorValidar) || 0}</div>
                    <div class="lbl">Dudas por Validar</div>
                </div>
                <div class="docente-kpi">
                    <div class="val">${Number(kpis.circulosACargo) || 0}</div>
                    <div class="lbl">Circulos a mi Cargo</div>
                </div>
                <div class="docente-kpi">
                    <div class="val">${Number(kpis.alumnosAsesorados) || 0}</div>
                    <div class="lbl">Alumnos Asesorados</div>
                </div>
                <div class="docente-kpi">
                    <div class="val">${Number(kpis.descargasApuntes) || 0}</div>
                    <div class="lbl">Descargas de mis Apuntes</div>
                </div>
            </div>

            <section class="card docente-section">
                <div class="docente-section-header">
                    <h3>Dudas de tus Grupos que Esperan Visto Bueno</h3>
                </div>
                ${dudas.length ? `
                <table class="docente-table">
                    <thead>
                        <tr>
                            <th>Materia</th>
                            <th>Pregunta del Alumno</th>
                            <th>Autor</th>
                            <th>Accion Docente</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${dudas.map(d => `
                        <tr>
                            <td><strong>${Utils.esc(d.materia || 'General')}</strong></td>
                            <td>${Utils.esc(d.titulo || d.contenido || '')}</td>
                            <td>${Utils.esc(d.autor || '—')}</td>
                            <td><button class="btn-solid docente-btn-validar" data-id="${d.id}" style="padding: 5px 10px; font-size: 0.8rem;">Validar y Cerrar</button></td>
                        </tr>`).join('')}
                    </tbody>
                </table>` : '<p class="docente-empty">No hay dudas pendientes por validar.</p>'}
            </section>

            <section class="card docente-section">
                <div class="docente-section-header">
                    <h3>Mis Salases de Asesoria Programadas</h3>
                    <button class="btn-outline" id="btn-agendar">+ Agendar Nueva Asesoria</button>
                </div>
                <p class="docente-meta">
                    Horario configurado: <strong>${Utils.esc(perfil.horario || 'Sin horario configurado')}</strong>
                    ${perfil.enlace ? ` &bull; Sala virtual: <a class="docente-link" href="${Utils.esc(perfil.enlace)}" target="_blank">${Utils.esc(perfil.enlace)}</a>` : ''}
                </p>
                ${asesorias.length ? `
                <table class="docente-table">
                    <thead>
                        <tr>
                            <th>Fecha y Hora</th>
                            <th>Materia / Tema</th>
                            <th>Inscritos</th>
                            <th>Enlace</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${asesorias.map(a => `
                        <tr>
                            <td>${Utils.esc(a.fecha || '')} ${Utils.esc(a.hora || '')}</td>
                            <td>${Utils.esc(a.materia || '')} &mdash; ${Utils.esc(a.titulo || '')}</td>
                            <td>${a.inscritos} / ${a.cupoMax}</td>
                            <td>${a.enlace ? `<a class="docente-link" href="${Utils.esc(a.enlace)}" target="_blank">Abrir sala</a>` : '<span style="color:#999">—</span>'}</td>
                        </tr>`).join('')}
                    </tbody>
                </table>` : '<p class="docente-empty">No hay sesiones de asesoria programadas.</p>'}
            </section>
        </div>
    `;

    document.querySelectorAll('.docente-btn-validar').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-id');
            if (!id) return;
            btn.disabled = true;
            btn.textContent = 'Cerrando...';
            try {
                await API.request(`/docente/dudas/${id}/validar`, { method: 'POST' });
                Utils.toast('Duda validada y cerrada.', 'success');
                refresh();
            } catch (e) {
                Utils.toast(e.message || 'No se pudo validar la duda.', 'error');
                btn.disabled = false;
                btn.textContent = 'Validar y Cerrar';
            }
        });
    });

    document.getElementById('btn-agendar')?.addEventListener('click', () => {
        showAgendarForm(content);
    });
}

function refresh() {
    const content = document.getElementById('main-content');
    if (!content) return;
    content.innerHTML = '<div class="docente-container"><p class="loading-message">Cargando panel docente...</p></div>';
    API.request('/docente/stats')
        .then(data => renderDocente(content, data || {}))
        .catch(e => {
            content.innerHTML = `<div class="docente-container"><div class="card docente-empty">No se pudo cargar el panel: ${Utils.esc(e.message || 'error')}</div></div>`;
        });
}

function showAgendarForm(content) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
        <div class="modal docente-asistencia-modal">
            <h3>Agendar Nueva Asesoria</h3>
            <form id="agendar-form">
                <label>Titulo / Tema</label>
                <input type="text" id="ag-titulo" required placeholder="Ej. Repaso de Patrones de Diseño">

                <label>Materia</label>
                <input type="text" id="ag-materia" required placeholder="Ej. Arquitectura de Software">

                <label>Descripcion</label>
                <textarea id="ag-descripcion" rows="3" placeholder="Temas que se cubriran..."></textarea>

                <div class="ag-row">
                    <div>
                        <label>Fecha</label>
                        <input type="date" id="ag-fecha" required>
                    </div>
                    <div>
                        <label>Hora</label>
                        <input type="time" id="ag-hora" required>
                    </div>
                </div>

                <div class="ag-row">
                    <div>
                        <label>Modalidad</label>
                        <select id="ag-modalidad">
                            <option value="Grupal">Grupal</option>
                            <option value="Individual">Individual</option>
                            <option value="Virtual">Virtual</option>
                        </select>
                    </div>
                    <div>
                        <label>Ubicacion</label>
                        <input type="text" id="ag-ubicacion" placeholder="Salon o enlace de sala">
                    </div>
                </div>

                <div class="ag-row">
                    <div>
                        <label>Duracion (min)</label>
                        <input type="number" id="ag-duracion" value="90" min="15">
                    </div>
                    <div>
                        <label>Cupo maximo</label>
                        <input type="number" id="ag-cupo" value="30" min="1">
                    </div>
                </div>

                <div class="modal-actions">
                    <button type="button" class="btn-outline" id="ag-cancelar">Cancelar</button>
                    <button type="submit" class="btn-solid">Programar Asesoria</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(overlay);

    overlay.querySelector('#ag-cancelar').addEventListener('click', () => overlay.remove());
    overlay.querySelector('#agendar-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            titulo: overlay.querySelector('#ag-titulo').value.trim(),
            materia: overlay.querySelector('#ag-materia').value.trim(),
            descripcion: overlay.querySelector('#ag-descripcion').value.trim(),
            fecha: overlay.querySelector('#ag-fecha').value,
            hora: overlay.querySelector('#ag-hora').value,
            modalidad: overlay.querySelector('#ag-modalidad').value,
            ubicacion: overlay.querySelector('#ag-ubicacion').value.trim() || null,
            duracionMin: parseInt(overlay.querySelector('#ag-duracion').value, 10) || 90,
            cupoMax: parseInt(overlay.querySelector('#ag-cupo').value, 10) || 30
        };
        const submitBtn = overlay.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Programando...';
        try {
            await API.request('/docente/asesorias', { method: 'POST', body: JSON.stringify(payload) });
            Utils.toast('Asesoria programada.', 'success');
            overlay.remove();
            refresh();
        } catch (err) {
            Utils.toast(err.message || 'No se pudo programar la asesoria.', 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Programar Asesoria';
        }
    });
}
