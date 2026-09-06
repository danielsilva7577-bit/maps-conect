/**
 * Panel Administrativo y Reportería.
 * Contrato esperado de GET /admin/stats:
 * { perfil, kpis: { totalEstudiantes, totalProfesores, totalPublicaciones,
 *   sesionesAbiertas, totalEmpresas, totalResenas, totalRecursos },
 *   actividad: [{ modulo, titulo, autor, tipo, estado, fecha }] }
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('admin.html');
    Layout.setPageTitle('Panel Administrativo');

    const content = document.getElementById('main-content');
    if (!content) return;

    content.innerHTML = '<div class="admin-container"><p class="loading-message">Cargando panel administrativo…</p></div>';

    let data = {};
    try {
        data = await API.request('/admin/stats');
    } catch (e) {
        content.innerHTML = `<div class="admin-container"><div class="card admin-empty">No se pudo cargar el panel: ${Utils.esc(e.message || 'sin permisos de administrador')}</div></div>`;
        return;
    }

    renderAdmin(content, data || {});
});

function renderAdmin(content, data) {
    const perfil = data.perfil || {};
    const kpis = data.kpis || {};
    const actividad = Array.isArray(data.actividad) ? data.actividad : [];

    content.innerHTML = `
        <div class="admin-container">
            <header class="card admin-header">
                <div class="admin-title">
                    <h2>Panel de Coordinación y Reportería Académica</h2>
                    <p>Profesor / Asesor Docente: <strong>${Utils.esc(perfil.nombre || 'Administrador')}</strong> • Campus Guadalajara</p>
                </div>
                <div>
                    <span class="badge badge-info">Rol: Administrador Docente</span>
                </div>
            </header>

            <section class="kpi-grid">
                <div class="kpi-card">
                    <div class="kpi-value">${Number(kpis.totalEstudiantes) || 0}</div>
                    <div class="kpi-label">Estudiantes</div>
                    <div class="kpi-sub">Certificados activos en la plataforma</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-value">${Number(kpis.totalProfesores) || 0}</div>
                    <div class="kpi-label">Docentes</div>
                    <div class="kpi-sub">Personal académico registrado</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-value">${Number(kpis.totalPublicaciones) || 0}</div>
                    <div class="kpi-label">Dudas / Publicaciones</div>
                    <div class="kpi-sub">Actividad del foro académico</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-value">${Number(kpis.sesionesAbiertas) || 0}</div>
                    <div class="kpi-label">Círculos de Estudio</div>
                    <div class="kpi-sub">Sesiones activas programadas</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-value">${Number(kpis.totalEmpresas) || 0}</div>
                    <div class="kpi-label">Empresas Vinculadas</div>
                    <div class="kpi-sub">Directorio del Semestre Empresarial</div>
                </div>
                <div class="kpi-card alert">
                    <div class="kpi-value">${Number(kpis.totalResenas) || 0}</div>
                    <div class="kpi-label">Reseñas Empresariales</div>
                    <div class="kpi-sub">Evaluaciones del ciclo vigente</div>
                </div>
            </section>

            <section class="card">
                <div class="section-header">
                    <strong>Extracción de Reportes Institucionales</strong>
                    <span style="font-size: 0.8rem; color: #666;">Exportación directa para comités y acreditaciones</span>
                </div>
                <form class="report-form" id="report-form">
                    <div class="form-group">
                        <label for="report-tipo">Tipo de Reporte</label>
                        <select class="form-control" id="report-tipo">
                            <option value="círculos">Participación en Círculos de Estudio y Asistencia</option>
                            <option value="dudas">Métricas de Resolución de Dudas por Materia</option>
                            <option value="recursos">Descargas y Calidad de Repositorio de Apuntes</option>
                            <option value="empresas">Evaluación y Desempeño de Empresas Vinculadas</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="report-ciclo">Semestre / Ciclo</label>
                        <select class="form-control" id="report-ciclo">
                            <option value="current">Ciclo Vigente (Semestre Actual)</option>
                            <option value="2026-1">2026-1 (Enero - Mayo)</option>
                            <option value="all">Histórico Anual</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="report-formato">Formato de Salida</label>
                        <select class="form-control" id="report-formato">
                            <option value="csv">Archivo CSV / Excel (.csv)</option>
                            <option value="pdf">Documento Ejecutivo (.pdf)</option>
                        </select>
                    </div>
                    <button class="btn-solid" type="submit">⬇ Descargar Reporte</button>
                </form>
            </section>

            <section class="card">
                <div class="section-header">
                    <strong>Gestión de Docentes</strong>
                    <span style="font-size: 0.8rem; color: #666;">Alta de cuentas de asesores docentes</span>
                </div>
                <form class="report-form" id="docente-form">
                    <div class="form-group">
                        <label for="doc-nombre">Nombre(s) del docente</label>
                        <input type="text" class="form-control" id="doc-nombre" required>
                    </div>
                    <div class="form-group">
                        <label for="doc-apellido">Apellido(s)</label>
                        <input type="text" class="form-control" id="doc-apellido" required>
                    </div>
                    <div class="form-group">
                        <label for="doc-email">Correo institucional</label>
                        <input type="email" class="form-control" id="doc-email" required placeholder="docente@tecmilenio.mx">
                    </div>
                    <div class="form-group">
                        <label for="doc-nomina">Número de nómina</label>
                        <input type="text" class="form-control" id="doc-nomina" required placeholder="L01234567">
                    </div>
                    <div class="form-group">
                        <label for="doc-contrasena">Contraseña inicial</label>
                        <input type="text" class="form-control" id="doc-contrasena" required placeholder="Temporal123">
                    </div>
                    <button class="btn-solid" type="submit">➕ Crear Docente</button>
                </form>
                <div id="docente-resultado" style="margin-top:10px;"></div>
            </section>

            <section class="card">
                <div class="section-header">
                    <strong>Gestión de Cuentas (Alumnos y Docentes)</strong>
                    <span style="font-size: 0.8rem; color: #666;">Dar de baja al graduarse, darse de baja o al causar baja</span>
                </div>
                <div id="usuarios-contenido"></div>
            </section>

            <section class="card">
                <div class="section-header">
                    <strong>Supervisión de Actividad y Validación Docente</strong>
                </div>
                <div class="table-responsive">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Módulo</th>
                                <th>Elemento / Título</th>
                                <th>Autor / Alumno</th>
                                <th>Estado</th>
                            </tr>
                        </thead>
                        <tbody>${actividad.length ? actividad.map(renderActividad).join('') : renderFilaVacia()}</tbody>
                    </table>
                </div>
            </section>
        </div>
    `;

    document.getElementById('report-form')?.addEventListener('submit', event => {
        event.preventDefault();
        const tipo = document.getElementById('report-tipo').value;
        const ciclo = document.getElementById('report-ciclo').value;
        const formato = document.getElementById('report-formato').value;
        exportarReporte(tipo, ciclo, formato, data);
    });

    document.getElementById('docente-form')?.addEventListener('submit', async event => {
        event.preventDefault();
        const resultado = document.getElementById('docente-resultado');
        const nombre = document.getElementById('doc-nombre').value.trim();
        const apellido = document.getElementById('doc-apellido').value.trim();
        const email = document.getElementById('doc-email').value.trim();
        const numeroNomina = document.getElementById('doc-nomina').value.trim();
        const contrasena = document.getElementById('doc-contrasena').value;

        if (!/^[^\s@]+@(tecmilenio\.mx|servicios\.tecmilenio\.mx)$/i.test(email)) {
            resultado.innerHTML = '<div class="admin-empty" style="color:#b91c1c;">Usa un correo institucional @tecmilenio.mx.</div>';
            return;
        }
        if (contrasena.length < 8 || !/[A-Z]/.test(contrasena) || !/[a-z]/.test(contrasena) || !/\d/.test(contrasena)) {
            resultado.innerHTML = '<div class="admin-empty" style="color:#b91c1c;">La contraseña requiere 8+ caracteres con mayúscula, minúscula y número.</div>';
            return;
        }

        resultado.innerHTML = '<div class="admin-empty">Creando docente…</div>';
        try {
            await API.request('/admin/crear-docente', {
                method: 'POST',
                body: JSON.stringify({ nombre, apellido, email, numeroNomina, contrasena })
            });
            resultado.innerHTML = '<div class="admin-empty" style="color:#087527;">✅ Docente creado correctamente.</div>';
            event.target.reset();
            cargarUsuarios();
        } catch (e) {
            resultado.innerHTML = `<div class="admin-empty" style="color:#b91c1c;">${Utils.esc(e.message || 'No se pudo crear el docente.')}</div>`;
        }
    });

    cargarUsuarios();
}

async function cargarUsuarios() {
    const contenedor = document.getElementById('usuarios-contenido');
    if (!contenedor) return;

    contenedor.innerHTML = '<p class="admin-empty">Cargando cuentas…</p>';
    let usuarios = [];
    try {
        usuarios = await API.request('/admin/usuarios');
    } catch (e) {
        contenedor.innerHTML = `<p class="admin-empty" style="color:#b91c1c;">No se pudo cargar la lista: ${Utils.esc(e.message || 'sin permisos')}</p>`;
        return;
    }

    if (!Array.isArray(usuarios) || !usuarios.length) {
        contenedor.innerHTML = '<p class="admin-empty">Sin cuentas registradas.</p>';
        return;
    }

    contenedor.innerHTML = `
        <div class="usuarios-buscar">
            <input type="text" id="usuarios-buscar-input" class="form-control"
                   placeholder="Buscar por nombre o matrícula / nómina…" autocomplete="off">
        </div>
        <div class="table-responsive">
            <table class="admin-table" id="usuarios-tabla">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Correo</th>
                        <th>Matrícula / Nómina</th>
                        <th>Tipo</th>
                        <th>Estado</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody id="usuarios-tbody"></tbody>
            </table>
        </div>`;

    const input = document.getElementById('usuarios-buscar-input');
    const tbody = document.getElementById('usuarios-tbody');
    let filtro = '';

    const render = () => {
        const q = filtro.trim().toLowerCase();
        const filtrados = q
            ? usuarios.filter(u => {
                const mat = (u.matricula || u.nomina || '').toLowerCase();
                return (u.nombre || '').toLowerCase().includes(q)
                    || (u.email || '').toLowerCase().includes(q)
                    || mat.includes(q);
              })
            : usuarios;

        if (!filtrados.length) {
            tbody.innerHTML = '<tr><td colspan="6"><div class="admin-empty">Sin resultados para la búsqueda.</div></td></tr>';
            return;
        }

        tbody.innerHTML = filtrados.map(u => {
            const activo = u.activo === true;
            const rolLbl = u.rol === 'PROFESOR' ? 'Docente' : 'Estudiante';
            const credencial = u.rol === 'PROFESOR' ? (u.nomina || '—') : (u.matricula || '—');
            const badge = activo
                ? '<span class="badge badge-info">Activo</span>'
                : '<span class="badge badge-danger">Inhabilitado</span>';
            const accion = activo
                ? `<button class="btn-solid btn-baja" data-id="${u.id}" style="padding:5px 10px;font-size:0.8rem;">Dar de baja</button>`
                : `<button class="btn-outline btn-reactivar" data-id="${u.id}" style="padding:5px 10px;font-size:0.8rem;">Reactivar</button>`;
            return `
                <tr>
                    <td><strong>${Utils.esc(u.nombre || '—')}</strong></td>
                    <td>${Utils.esc(u.email || '—')}</td>
                    <td>${Utils.esc(credencial)}</td>
                    <td><span class="badge badge-info">${rolLbl}</span></td>
                    <td>${badge}</td>
                    <td>${accion}</td>
                </tr>`;
        }).join('');
    };

    input.addEventListener('input', () => {
        filtro = input.value;
        render();
    });

    render();

    document.querySelectorAll('#usuarios-tabla .btn-baja').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.dataset.id;
            if (!confirm('¿Inhabilitar esta cuenta? El usuario ya no podrá iniciar sesión.')) return;
            try {
                await API.request(`/admin/usuarios/${id}/inhabilitar`, { method: 'POST' });
                Utils.toast('Cuenta inhabilitada.', 'success');
                cargarUsuarios();
            } catch (e) {
                Utils.toast(e.message || 'No se pudo inhabilitar.', 'error');
            }
        });
    });

    document.querySelectorAll('#usuarios-tabla .btn-reactivar').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.dataset.id;
            try {
                await API.request(`/admin/usuarios/${id}/reactivar`, { method: 'POST' });
                Utils.toast('Cuenta reactivada.', 'success');
                cargarUsuarios();
            } catch (e) {
                Utils.toast(e.message || 'No se pudo reactivar.', 'error');
            }
        });
    });
}

function renderActividad(item) {
    const fecha = item.fecha ? formatFecha(item.fecha) : '';
    const esPublicacion = item.tipo === 'publicacion';
    const badge = esPublicacion
        ? '<span class="badge badge-warning">Publicación de foro</span>'
        : '<span class="badge badge-info">Actividad</span>';
    const modulo = item.modulo || (esPublicacion ? 'Foro Dudas' : 'General');
    return `
        <tr>
            <td><strong>${Utils.esc(modulo)}</strong></td>
            <td>${Utils.esc(item.titulo || 'Sin título')}</td>
            <td>${Utils.esc(item.autor || '—')}</td>
            <td>${badge}${fecha ? `<span style="color:#888; font-size:0.75rem;"> · ${Utils.esc(fecha)}</span>` : ''}</td>
        </tr>
    `;
}

function renderFilaVacia() {
    return '<tr><td colspan="4"><div class="admin-empty">Sin actividad reciente para supervisar.</div></td></tr>';
}

function exportarReporte(tipo, ciclo, formato, data) {
    const etiquetas = {
        'círculos': 'Participación en Círculos de Estudio y Asistencia',
        'dudas': 'Métricas de Resolución de Dudas por Materia',
        'recursos': 'Descargas y Calidad de Repositorio de Apuntes',
        'empresas': 'Evaluación y Desempeño de Empresas Vinculadas'
    };
    const kpis = (data && data.kpis) || {};
    const actividad = (data && Array.isArray(data.actividad)) ? data.actividad : [];

    const filas = [];
    filas.push(['MAPS CONNECT — REPORTE INSTITUCIONAL']);
    filas.push(['Tipo de reporte', etiquetas[tipo] || tipo]);
    filas.push(['Semestre / Ciclo', ciclo]);
    filas.push(['Generado', new Date().toLocaleString()]);
    filas.push([]);
    filas.push(['INDICADOR', 'VALOR']);
    filas.push(['Estudiantes activos', Number(kpis.totalEstudiantes) || 0]);
    filas.push(['Docentes registrados', Number(kpis.totalProfesores) || 0]);
    filas.push(['Dudas / Publicaciones', Number(kpis.totalPublicaciones) || 0]);
    filas.push(['Círculos de Estudio activos', Number(kpis.sesionesAbiertas) || 0]);
    filas.push(['Empresas vinculadas', Number(kpis.totalEmpresas) || 0]);
    filas.push(['Reseñas empresariales', Number(kpis.totalResenas) || 0]);
    filas.push(['Recursos académicos', Number(kpis.totalRecursos) || 0]);
    filas.push([]);
    filas.push(['ACTIVIDAD RECIENTE — Módulo', 'Elemento / Título', 'Autor', 'Estado', 'Fecha']);
    if (actividad.length) {
        actividad.forEach(a => {
            filas.push([
                a.modulo || 'General',
                a.titulo || 'Sin título',
                a.autor || '—',
                a.tipo === 'publicacion' ? 'Foro' : 'Actividad',
                formatFecha(a.fecha)
            ]);
        });
    } else {
        filas.push(['Sin actividad reciente para este período.']);
    }

    const csv = filas.map(fila => fila.map(campo => {
        const s = String(campo == null ? '' : campo);
        return /[",\n\r]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
    }).join(',')).join('\r\n');

    const nombre = `reporte_${tipo}_${ciclo}`;
    if (formato === 'pdf') {
        const docu = filas.map(fila => fila.join('\t')).join('\r\n');
        const blob = new Blob([docu], { type: 'text/plain;charset=utf-8' });
        descargar(blob, `${nombre}.txt`);
        Utils.toast('Documento ejecutivo generado.', 'success');
        return;
    }
    const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' });
    descargar(blob, `${nombre}.csv`);
    Utils.toast('Reporte CSV generado correctamente.', 'success');
}

function descargar(blob, nombre) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombre;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function formatFecha(value) {
    if (!value) return '';
    const s = String(value).replace('T', ' ');
    const m = s.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (!m) return '';
    return `${m[3]}/${m[2]}/${m[1]}`;
}
