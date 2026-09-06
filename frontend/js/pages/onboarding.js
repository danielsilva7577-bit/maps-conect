(function () {
    let rolActual = null;

    document.addEventListener('DOMContentLoaded', () => {
        if (!Auth.requireAuth()) return;

        const user = Auth.getUser();
        rolActual = user?.rol || '';
        document.getElementById('ono-titulo').textContent =
            rolActual === 'PROFESOR' ? 'Perfil del Maestro' : 'Bienvenido, estudiante';

        if (rolActual === 'ESTUDIANTE') {
            document.getElementById('ono-subtitulo').textContent =
                'Cuéntanos tu carrera y semestre para mostrar tus materias y empezar a navegar.';
            renderFormularioEstudiante();
        } else if (rolActual === 'PROFESOR') {
            document.getElementById('ono-subtitulo').textContent =
                'Define tu asignación académica y tus datos de asesoría para aparecer en el directorio.';
            renderFormularioProfesor();
        } else {
            window.location.href = Auth.resolvePath('pages/inicio.html');
        }
    });

    function cargarOpciones(url, textoVacio) {
        return API.request(url).then(res => res || []);
    }

    /* ------------------------- ESTUDIANTE ------------------------- */

    async function renderFormularioEstudiante() {
        const cont = document.getElementById('ono-contenido');
        const matricula = sessionStorage.getItem('pending-matricula') || '';
        cont.innerHTML = `
            <form id="ono-form-estudiante">
                <div class="form-group">
                    <label for="ono-matricula">Matrícula</label>
                    <input type="text" id="ono-matricula" class="form-control" required
                           value="${escapeHtml(matricula)}" placeholder="A12345678">
                </div>
                <div class="form-group">
                    <label for="ono-carrera">Carrera</label>
                    <select id="ono-carrera" class="form-control" required>
                        <option value="">Selecciona tu carrera...</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Semestre</label>
                    <div class="ono-grid" id="ono-semestres"></div>
                </div>
                <div class="ono-section">
                    <h3>Materias de tu semestre</h3>
                    <div id="ono-materias">${spinnerHtml()}</div>
                </div>
                <div class="form-group" style="margin-top:1.25rem">
                    <button type="submit" class="btn btn-primary" style="width:100%">Confirmar y empezar a navegar</button>
                </div>
            </form>`;

        const selCarrera = document.getElementById('ono-carrera');
        const selSemestre = document.getElementById('ono-semestres');

        try {
            const carreras = await cargarOpciones('/carreras', 'Sin carreras');
            selCarrera.innerHTML =
                '<option value="">Selecciona tu carrera...</option>' +
                carreras.map(c => `<option value="${c.id}">${escapeHtml(c.nombre)}</option>`).join('');

            selSemestre.innerHTML = Array.from({ length: 12 }, (_, i) => i + 1).map(s =>
                `<label class="${s === 1 ? 'sem-activo' : ''}">
                    <input type="radio" name="ono-semestre" value="${s}" ${s === 1 ? 'checked' : ''}>
                    ${s}°
                </label>`).join('');

            selSemestre.addEventListener('change', (e) => {
                if (e.target.name === 'ono-semestre') {
                    selSemestre.querySelectorAll('label').forEach(l => l.classList.remove('sem-activo'));
                    e.target.closest('label').classList.add('sem-activo');
                    cargarMateriasEstudiante();
                }
            });
            selCarrera.addEventListener('change', cargarMateriasEstudiante);

            if (carreras.length) cargarMateriasEstudiante();

            document.getElementById('ono-form-estudiante').addEventListener('submit', async (ev) => {
                ev.preventDefault();
                Utils.clearAlert('alert-container');
                const btn = ev.target.querySelector('button[type="submit"]');

                if (!selCarrera.value) {
                    Utils.showAlert('alert-container', 'Selecciona tu carrera.');
                    return;
                }
                const semestre = Number(document.querySelector('input[name="ono-semestre"]:checked')?.value || 1);

                try {
                    btn.disabled = true;
                    btn.textContent = 'Guardando...';
                    const materias = [...document.querySelectorAll('#ono-materias input[type="checkbox"]:checked')]
                        .map(cb => Number(cb.value));
                    await API.request('/auth/onboarding/estudiante', {
                        method: 'POST',
                        body: JSON.stringify({
                            matricula: document.getElementById('ono-matricula').value.trim(),
                            idCarrera: Number(selCarrera.value),
                            semestre,
                            materias
                        })
                    });
                    sessionStorage.removeItem('pending-matricula');
                    window.location.href = Auth.resolvePath('pages/inicio.html');
                } catch (error) {
                    Utils.showAlert('alert-container', error.message || 'No se pudo completar el perfil.');
                    btn.disabled = false;
                    btn.textContent = 'Confirmar y empezar a navegar';
                }
            });
        } catch (error) {
            cont.innerHTML = `<div class="alert alert-error">Error al cargar el catálogo: ${escapeHtml(error.message)}</div>`;
        }
    }

    async function cargarMateriasEstudiante() {
        const idCarrera = document.getElementById('ono-carrera')?.value;
        const semestre = Number(document.querySelector('input[name="ono-semestre"]:checked')?.value || 1);
        const contMaterias = document.getElementById('ono-materias');
        if (!idCarrera) {
            contMaterias.innerHTML = '<div class="ono-espera">Selecciona tu carrera para ver las materias.</div>';
            return;
        }
        contMaterias.innerHTML = spinnerHtml();
        try {
            const materias = await cargarOpciones(`/materias/plan/${idCarrera}/${semestre}`, 'Sin materias');
            contMaterias.innerHTML = materias.length
                ? `<div class="ono-grid">${materias.map(m => `
                    <label>
                        <input type="checkbox" name="ono-materia" value="${m.id}"> ${escapeHtml(m.nombre)}
                    </label>`).join('')}
                </div>
                <div class="form-hint">Marca las materias que cursas este semestre.</div>`
                : '<div class="ono-espera">No hay materias registradas para ese semestre.</div>';
        } catch (error) {
            contMaterias.innerHTML = `<div class="alert alert-error">${escapeHtml(error.message)}</div>`;
        }
    }

    /* ------------------------- PROFESOR ------------------------- */

    async function renderFormularioProfesor() {
        const cont = document.getElementById('ono-contenido');
        const nomina = sessionStorage.getItem('pending-numeroNomina') || '';
        cont.innerHTML = `
            <form id="ono-form-profesor">
                <div class="form-group">
                    <label for="ono-nomina">Nómina / Matrícula Docente</label>
                    <input type="text" id="ono-nomina" class="form-control" required
                           value="${escapeHtml(nomina)}" placeholder="L01234567">
                </div>
                <div class="ono-section">
                    <h3>Asignación Académica y Especialidad (Ruta MAPS)</h3>
                    <div class="form-group">
                        <label for="ono-area">Área / Carrera principal</label>
                        <select id="ono-area" class="form-control">
                            <option value="">Selecciona el departamento...</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Certificados a cargo</label>
                        <div class="ono-grid" id="ono-certificados"><div class="ono-espera">${spinnerHtml()}</div></div>
                    </div>
                    <div class="form-group">
                        <label>Materias que imparte</label>
                        <div class="ono-grid" id="ono-materias-prof"><div class="ono-espera">${spinnerHtml()}</div></div>
                    </div>
                    <div class="form-group">
                        <label for="ono-semestres">Semestres asignados</label>
                        <input type="text" id="ono-semestres" class="form-control" placeholder="ej. 4°, 5°, 6°">
                        <div class="form-hint">Ciclos académicos donde tiene grupos activos.</div>
                    </div>
                </div>
                <div class="ono-section">
                    <h3>Datos de Asesoría y Contacto</h3>
                    <div class="form-group">
                        <label for="ono-horario">Horario de asesoría semanal</label>
                        <input type="text" id="ono-horario" class="form-control" placeholder="ej. Lunes y Miércoles 4:00 PM - 6:00 PM">
                    </div>
                    <div class="form-group">
                        <label for="ono-enlace">Enlace de sala virtual</label>
                        <input type="url" id="ono-enlace" class="form-control" placeholder="https://teams.microsoft.com/...">
                    </div>
                    <div class="form-group">
                        <label for="ono-semblanza">Semblanza / Especialidad técnica</label>
                        <textarea id="ono-semblanza" class="form-control" rows="3"
                                  placeholder="ej. Especialista en arquitectura backend Java y modelado relacional en MySQL."></textarea>
                    </div>
                </div>
                <div class="form-group" style="margin-top:1.25rem">
                    <button type="submit" class="btn btn-primary" style="width:100%">Guardar y empezar a navegar</button>
                </div>
            </form>`;

        const selArea = document.getElementById('ono-area');
        const contCert = document.getElementById('ono-certificados');
        const contMat = document.getElementById('ono-materias-prof');

        const [carreras, certificados, materias] = await Promise.all([
            cargarOpciones('/carreras', 'Sin carreras'),
            cargarOpciones('/certificados', 'Sin certificados'),
            cargarOpciones('/materias', 'Sin materias')
        ]);

        selArea.innerHTML =
            '<option value="">Selecciona el departamento...</option>' +
            carreras.map(c => `<option value="${escapeHtml(c.nombre)}">${escapeHtml(c.nombre)}</option>`).join('');

        contCert.innerHTML = certificados.length
            ? certificados.map(c => `<label>
                    <input type="checkbox" name="ono-certificado" value="${c.id}"> ${escapeHtml(c.nombre)}
                </label>`).join('')
            : `<div class="ono-espera" style="grid-column:1/-1">Aún no hay certificados registrados en el catálogo.</div>`;

        contMat.innerHTML = materias.length
            ? materias.map(m => `<label>
                    <input type="checkbox" name="ono-materia-prof" value="${m.id}"> ${escapeHtml(m.nombre)}
                </label>`).join('')
            : `<div class="ono-espera" style="grid-column:1/-1">Aún no hay materias en el catálogo.</div>`;

        document.getElementById('ono-form-profesor').addEventListener('submit', async (ev) => {
            ev.preventDefault();
            Utils.clearAlert('alert-container');
            const btn = ev.target.querySelector('button[type="submit"]');
            const certIds = [...document.querySelectorAll('input[name="ono-certificado"]:checked')].map(i => Number(i.value));
            const matIds = [...document.querySelectorAll('input[name="ono-materia-prof"]:checked')].map(i => Number(i.value));

            try {
                btn.disabled = true;
                btn.textContent = 'Guardando...';
                await API.request('/auth/onboarding/profesor', {
                    method: 'POST',
                    body: JSON.stringify({
                        numeroNomina: document.getElementById('ono-nomina').value.trim(),
                        areaEspecialidad: selArea.value,
                        semestresAsignados: document.getElementById('ono-semestres').value.trim(),
                        horarioAsesorias: document.getElementById('ono-horario').value.trim(),
                        enlaceSalaVirtual: document.getElementById('ono-enlace').value.trim(),
                        semblanza: document.getElementById('ono-semblanza').value.trim(),
                        certificados: certIds,
                        materias: matIds
                    })
                });
                sessionStorage.removeItem('pending-numeroNomina');
                window.location.href = Auth.resolvePath('pages/inicio.html');
            } catch (error) {
                Utils.showAlert('alert-container', error.message || 'No se pudo completar el perfil.');
                btn.disabled = false;
                btn.textContent = 'Guardar y empezar a navegar';
            }
        });
    }

    /* ------------------------- UTILIDADES ------------------------- */

    function escapeHtml(texto) {
        return String(texto || '').replace(/[&<>"']/g, c => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[c]));
    }

    function spinnerHtml() {
        return '<div class="ono-espera">Cargando...</div>';
    }
})();