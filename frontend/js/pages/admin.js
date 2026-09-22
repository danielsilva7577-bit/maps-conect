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
    const ciclosReporte = opcionesCicloReportes();

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

            <section class="card report-dashboard-card" id="seccion-reportes">
                <div class="section-header report-section-head">
                    <div>
                        <strong class="report-header-title">Dashboard y Reportería Institucional</strong>
                        <div class="report-header-subtitle">Métricas ejecutivas en tiempo real y exportación de documentos oficiales</div>
                    </div>
                    <div class="report-controls-inline">
                        <select class="form-control" id="report-ciclo" style="width:auto; min-width:210px; font-weight:600;">
                            ${ciclosReporte}
                        </select>
                        <div class="report-export-buttons">
                            <button class="btn-solid" id="btn-export-pdf" type="button" title="Descargar documento PDF ejecutivo">Exportar PDF</button>
                            <button class="btn-solid" id="btn-export-excel" type="button" title="Descargar libro de cálculo estructurado en Excel (.xls)">Exportar Excel</button>
                            <button class="btn-outline" id="btn-export-csv" type="button" title="Descargar datos en CSV (.csv)">Exportar CSV</button>
                            <button class="btn-outline" id="btn-print-report" type="button" title="Imprimir reporte" style="padding:8px 12px;">Imprimir</button>
                        </div>
                    </div>
                </div>

                <!-- Pestañas de módulos -->
                <div class="report-module-tabs" id="report-tabs">
                    <button class="report-tab-btn active" data-tipo="circulos" type="button">Círculos de Estudio</button>
                    <button class="report-tab-btn" data-tipo="dudas" type="button">Resolución de Dudas</button>
                    <button class="report-tab-btn" data-tipo="recursos" type="button">Repositorio de Apuntes</button>
                    <button class="report-tab-btn" data-tipo="empresas" type="button">Empresas Vinculadas</button>
                </div>

                <!-- Contenedor del Dashboard Dinámico -->
                <div id="report-dashboard-container">
                    <div class="admin-empty">Cargando métricas del dashboard…</div>
                </div>
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
                    <button class="btn-solid" type="submit">Crear Docente</button>
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

    // Gestión del Dashboard de Reportería
    let tipoReporteActual = 'circulos';
    const cicloReporteSelect = document.getElementById('report-ciclo');

    document.querySelectorAll('#report-tabs .report-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#report-tabs .report-tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            tipoReporteActual = btn.dataset.tipo || 'circulos';
            cargarDashboardReporte(tipoReporteActual, cicloReporteSelect?.value || 'current');
        });
    });

    cicloReporteSelect?.addEventListener('change', () => {
        cargarDashboardReporte(tipoReporteActual, cicloReporteSelect.value);
    });

    document.getElementById('btn-export-pdf')?.addEventListener('click', async (e) => {
        const ciclo = cicloReporteSelect?.value || 'current';
        await descargarReporte(tipoReporteActual, ciclo, 'pdf', e.currentTarget);
    });

    document.getElementById('btn-export-excel')?.addEventListener('click', async (e) => {
        const ciclo = cicloReporteSelect?.value || 'current';
        await descargarReporte(tipoReporteActual, ciclo, 'excel', e.currentTarget);
    });

    document.getElementById('btn-export-csv')?.addEventListener('click', async (e) => {
        const ciclo = cicloReporteSelect?.value || 'current';
        await descargarReporte(tipoReporteActual, ciclo, 'csv', e.currentTarget);
    });

    document.getElementById('btn-print-report')?.addEventListener('click', () => {
        window.print();
    });

    // Carga inicial del dashboard de reportes
    cargarDashboardReporte(tipoReporteActual, cicloReporteSelect?.value || 'current');

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
            resultado.innerHTML = '<div class="admin-empty" style="color:#087527;">Docente creado correctamente.</div>';
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

async function descargarReporte(tipo, ciclo, formato, boton) {
    const textoOriginal = boton?.textContent || 'Descargar Reporte';
    if (boton) {
        boton.disabled = true;
        boton.textContent = 'Generando reporte…';
    }

    try {
        const parametros = new URLSearchParams({ tipo, ciclo, formato });
        const token = localStorage.getItem('token');
        const respuesta = await fetch(`${API_BASE_URL}/admin/reportes?${parametros.toString()}`, {
            headers: token ? { Authorization: `Bearer ${token}` } : {}
        });

        if (!respuesta.ok) {
            const error = await respuesta.json().catch(() => ({}));
            throw new Error(error?.message || `No se pudo generar el reporte (${respuesta.status}).`);
        }

        const blob = await respuesta.blob();
        const extension = formato === 'pdf' ? 'pdf' : formato === 'excel' ? 'xls' : 'csv';
        const cicloArchivo = ciclo === 'current' ? 'vigente' : ciclo === 'all' ? 'historico' : ciclo;
        descargar(blob, `reporte-${tipo}-${cicloArchivo}.${extension}`);
        const formatoNombre = formato === 'pdf' ? 'PDF' : formato === 'excel' ? 'Excel' : 'CSV';
        Utils.toast(`Reporte ${formatoNombre} generado correctamente.`, 'success');
    } catch (e) {
        Utils.toast(e.message || 'No se pudo generar el reporte.', 'error');
    } finally {
        if (boton) {
            boton.disabled = false;
            boton.textContent = textoOriginal;
        }
    }
}

function descargar(blob, nombre) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombre;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
}

function opcionesCicloReportes() {
    const hoy = new Date();
    const anioActual = hoy.getFullYear();
    const semestreActual = hoy.getMonth() < 6 ? 1 : 2;
    const cicloVigente = `${anioActual}-${semestreActual}`;
    const opciones = [`<option value="current">Ciclo vigente (${cicloVigente})</option>`];

    for (let anio = anioActual; anio >= anioActual - 2; anio--) {
        for (const semestre of [2, 1]) {
            const ciclo = `${anio}-${semestre}`;
            if (ciclo === cicloVigente) continue;
            const meses = semestre === 1 ? 'Enero - Mayo' : 'Agosto - Diciembre';
            opciones.push(`<option value="${ciclo}">${ciclo} (${meses})</option>`);
        }
    }
    opciones.push('<option value="all">Histórico completo</option>');
    return opciones.join('');
}

function formatFecha(value) {
    if (!value) return '';
    const s = String(value).replace('T', ' ');
    const m = s.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (!m) return '';
    return `${m[3]}/${m[2]}/${m[1]}`;
}

function formatFechaHora(value) {
    if (!value) return '';
    const s = String(value).replace('T', ' ');
    const m = s.match(/^(\d{4})-(\d{2})-(\d{2})\s*(\d{2}):(\d{2})/);
    if (!m) return formatFecha(value);
    return `${m[3]}/${m[2]}/${m[1]} ${m[4]}:${m[5]}`;
}

async function cargarDashboardReporte(tipo, ciclo) {
    const container = document.getElementById('report-dashboard-container');
    if (!container) return;

    container.innerHTML = '<div class="admin-empty" style="padding:40px 20px;">Cargando métricas y datos del dashboard…</div>';

    try {
        const datos = await API.request(`/admin/reportes/datos?tipo=${encodeURIComponent(tipo)}&ciclo=${encodeURIComponent(ciclo)}`);
        if (!datos) {
            container.innerHTML = '<div class="admin-empty">No se obtuvieron datos para el reporte solicitado.</div>';
            return;
        }

        const indicadoresHtml = (datos.indicadores || []).map((ind, i) => {
            const variante = i % 3 === 1 ? 'accent-alt' : i % 3 === 2 ? 'accent-gold' : '';
            return `
                <div class="report-kpi-card ${variante}">
                    <div class="report-kpi-val">${Utils.esc(ind.valor || '0')}</div>
                    <div class="report-kpi-label">${Utils.esc(ind.nombre || '—')}</div>
                </div>
            `;
        }).join('');

        const notasHtml = (datos.notas || []).map(nota => `
            <div class="report-nota-box">
                <strong>Nota metodológica:</strong> ${Utils.esc(nota)}
            </div>
        `).join('');

        const seccionesHtml = (datos.secciones || []).map((sec, sIdx) => {
            const columnas = sec.columnas || [];
            const filas = sec.filas || [];
            const tableId = `reporte-tabla-${sIdx}`;
            const countId = `count-${tableId}`;

            const thHtml = columnas.map(col => `<th>${Utils.esc(col)}</th>`).join('');
            const trHtml = filas.length
                ? filas.map(fila => `
                    <tr>
                        ${fila.map(celda => `<td>${Utils.esc(celda)}</td>`).join('')}
                    </tr>
                `).join('')
                : `<tr><td colspan="${Math.max(columnas.length, 1)}"><div class="admin-empty">Sin registros para el periodo seleccionado.</div></td></tr>`;

            return `
                <div class="report-section-container" style="margin-top:24px;">
                    <div class="report-section-title">${Utils.esc(sec.titulo || 'Detalle')}</div>
                    <div class="report-table-controls">
                        <input type="text" class="form-control report-table-search" 
                               placeholder="Filtrar en esta tabla…" 
                               data-tabla-id="${tableId}"
                               data-count-id="${countId}">
                        <span class="report-table-count" id="${countId}">${filas.length} registros</span>
                    </div>
                    <div class="report-table-wrapper">
                        <table class="report-dashboard-table" id="${tableId}">
                            <thead><tr>${thHtml}</tr></thead>
                            <tbody>${trHtml}</tbody>
                        </table>
                    </div>
                </div>
            `;
        }).join('');

        const fechaGenerado = datos.generadoEn ? formatFechaHora(datos.generadoEn) : 'En tiempo real';

        container.innerHTML = `
            <div class="report-meta-header">
                <div>
                    <h3>${Utils.esc(datos.titulo || 'Reporte Institucional')}</h3>
                    <div class="report-meta-info" style="margin-top:4px;">
                        <strong>Ciclo:</strong> ${Utils.esc(datos.periodo || ciclo)} &nbsp;·&nbsp; 
                        <strong>Generado:</strong> ${Utils.esc(fechaGenerado)} &nbsp;·&nbsp;
                        <strong>Sistema:</strong> MAPS Connect (Tecmilenio)
                    </div>
                </div>
            </div>

            ${indicadoresHtml ? `<div class="report-kpis-grid">${indicadoresHtml}</div>` : ''}
            ${notasHtml}
            ${seccionesHtml}
        `;

        // Filtrado dinámico por tabla
        container.querySelectorAll('.report-table-search').forEach(input => {
            input.addEventListener('input', () => {
                const tableId = input.dataset.tablaId;
                const countId = input.dataset.countId;
                const query = input.value.toLowerCase().trim();
                const table = document.getElementById(tableId);
                const countElem = document.getElementById(countId);
                if (!table) return;

                const rows = table.querySelectorAll('tbody tr');
                let visibles = 0;
                let total = 0;
                rows.forEach(row => {
                    if (row.querySelector('.admin-empty')) return;
                    total++;
                    const text = row.textContent.toLowerCase();
                    const match = !query || text.includes(query);
                    row.style.display = match ? '' : 'none';
                    if (match) visibles++;
                });

                if (countElem) {
                    countElem.textContent = query ? `${visibles} de ${total} registros` : `${total} registros`;
                }
            });
        });

    } catch (e) {
        container.innerHTML = `<div class="admin-empty" style="color:#b91c1c;">Error al cargar datos del reporte: ${Utils.esc(e.message || 'Error de conexión')}</div>`;
    }
}
