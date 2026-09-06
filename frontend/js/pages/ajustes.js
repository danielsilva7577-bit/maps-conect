/**
 * MAPS Connect - Ajustes
 * Permite: personalizar el color de la página, elegir un estilo visual,
 * activar/desactivar el modo oscuro, administrar la cuenta (nombre, contraseña)
 * y consultar el aviso de privacidad.
 */
const Ajustes = {
    init() {
        Layout.init('ajustes.html');
        Layout.setPageTitle('Ajustes');

        this.initColor();
        this.initEstilos();
        this.initTema();
        this.initCuenta();
        document.getElementById('btn-ver-aviso')?.addEventListener('click', () => AvisoPrivacidad.mostrar());
    },

    /** Personalización del color de la página (predefinidos + color a medida). */
    initColor() {
        const swatches = document.getElementById('swatches-colores');
        const customInput = document.getElementById('color-custom');
        const customTexto = document.getElementById('color-custom-texto');
        const btnRestaurar = document.getElementById('btn-restaurar-color');

        // --- Swatches predefinidos ---
        const current = Colores.guardado();
        const activarSwatch = (hex, boton) => {
            if (!swatches) return;
            swatches.querySelectorAll('.color-swatch').forEach((el) => {
                el.classList.toggle('activo', el === boton);
                el.setAttribute('aria-pressed', el === boton ? 'true' : 'false');
            });
            if (customInput) customInput.value = hex;
            if (customTexto) customTexto.textContent = hex;
        };

        if (swatches) {
            Colores.PRESETS.forEach(({ id, hex, nombre }) => {
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'color-swatch';
                btn.dataset.id = id;
                btn.dataset.hex = hex;
                btn.style.setProperty('--swatch', hex);
                btn.title = nombre;
                btn.setAttribute('aria-label', `Color ${nombre}`);
                btn.setAttribute('aria-pressed', 'false');
                if (current === hex) {
                    btn.classList.add('activo');
                    btn.setAttribute('aria-pressed', 'true');
                    if (customInput) customInput.value = hex;
                    if (customTexto) customTexto.textContent = hex;
                }
                btn.addEventListener('click', () => {
                    Colores.set(hex);
                    activarSwatch(hex, btn);
                    Utils.toast(`Color ${nombre} aplicado.`, 'success');
                });
                swatches.appendChild(btn);
            });
        }

        // --- Color a medida ---
        if (customInput) {
            customInput.addEventListener('input', () => {
                const hex = customInput.value.toLowerCase();
                Colores.set(hex);
                if (customTexto) customTexto.textContent = hex;
                swatches?.querySelectorAll('.color-swatch').forEach((el) => el.classList.remove('activo'));
            });
        }

        // --- Restablecer color por defecto ---
        btnRestaurar?.addEventListener('click', () => {
            Colores.limpiar();
            swatches?.querySelectorAll('.color-swatch').forEach((el) => el.classList.remove('activo'));
            // Marca el verde (por defecto) como activo.
            const verde = swatches?.querySelector('.color-swatch[data-id="verde"]');
            if (verde) {
                verde.classList.add('activo');
                if (customInput) customInput.value = verde.dataset.hex;
                if (customTexto) customTexto.textContent = verde.dataset.hex;
            }
            Utils.toast('Color por defecto restablecido.', 'success');
        });
    },

    /** Selector de estilos de la página (Estilos.STYLES). */
    initEstilos() {
        const lista = document.getElementById('estilos-lista');
        if (!lista) return;

        const renderizar = () => {
            const activo = Estilos.activo();
            lista.querySelectorAll('.estilo-opcion').forEach((op) => {
                const esActivo = op.dataset.estilo === activo;
                op.classList.toggle('activo', esActivo);
                op.setAttribute('aria-pressed', esActivo ? 'true' : 'false');
            });
            this._bloquearPersonalizaColor();
        };

        Estilos.STYLES.forEach(({ id, nombre, descripcion, preview }) => {
            const op = document.createElement('button');
            op.type = 'button';
            op.className = 'estilo-opcion';
            op.dataset.estilo = id;
            op.setAttribute('aria-pressed', 'false');
            op.innerHTML = `
                <span class="estilo-preview" style="background: ${preview}" aria-hidden="true"></span>
                <span class="estilo-nombre">${nombre}</span>
                <span class="estilo-desc">${descripcion}</span>
                <span class="estilo-check" aria-hidden="true">✓</span>`;
            op.addEventListener('click', () => {
                const activado = Estilos.set(id);
                renderizar();
                const e = Estilos.STYLES.find((s) => s.id === activado);
                Utils.toast(`Estilo "${e ? e.nombre : activado}" aplicado.`, 'success');
            });
            lista.appendChild(op);
        });

        renderizar();
    },

    /**
     * La personalización de color solo aplica al estilo "Clásico". Con otro
     * estilo activo se deshabilita para evitar mezclar paletas.
     */
    _bloquearPersonalizaColor() {
        const personaliza = document.querySelector('.personaliza-seccion');
        if (!personaliza) return;
        const bloqueado = Estilos.activo() !== 'clasico';
        personaliza.querySelectorAll('button, input').forEach((el) => { el.disabled = bloqueado; });
        personaliza.classList.toggle('personaliza-bloqueado', bloqueado);
    },

    /** Modo oscuro / claro. */
    initTema() {
        const toggle = document.getElementById('tema-toggle');
        if (toggle) {
            toggle.checked = Tema.activo();
            toggle.addEventListener('change', () => {
                const oscuro = Tema.alternar();
                // Después de alternar la clase real, sincronizamos el switch.
                toggle.checked = oscuro;
                Utils.toast(oscuro ? 'Modo oscuro activado.' : 'Modo claro activado.', 'success');
            });
        }
    },

    /** Mi cuenta: cambiar nombre y contraseña. */
    initCuenta() {
        const user = Auth.getUser();
        const cuentaEmail = document.getElementById('cuenta-email');
        if (cuentaEmail) cuentaEmail.textContent = user?.email || '—';

        const formNombre = document.getElementById('form-nombre');
        const inputNombre = document.getElementById('input-nombre');
        if (inputNombre) inputNombre.value = user?.nombre || '';

        formNombre?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const nombre = inputNombre.value.trim();
            if (nombre.length < 2) {
                return Utils.toast('Escribe tu nombre completo (mínimo 2 caracteres).', 'error');
            }
            try {
                const data = await API.request('/usuarios/cuenta/nombre', {
                    method: 'PUT',
                    body: JSON.stringify({ nombre })
                });
                Auth.actualizar({ nombre: data.nombre });
                inputNombre.value = data.nombre;
                Utils.toast('Nombre actualizado.', 'success');
            } catch (err) {
                Utils.toast(err.message || 'No se pudo actualizar el nombre.', 'error');
            }
        });

        const formContrasena = document.getElementById('form-contrasena');
        const actual = document.getElementById('input-contrasena-actual');
        const nueva = document.getElementById('input-contrasena-nueva');
        const confirmar = document.getElementById('input-contrasena-confirmar');

        formContrasena?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const a = actual.value;
            const n = nueva.value;
            const c = confirmar.value;

            if (!a || !n || !c) {
                return Utils.toast('Completa todos los campos.', 'error');
            }
            if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/.test(n)) {
                return Utils.toast('La contraseña nueva debe tener al menos 8 caracteres y combinar mayúscula, minúscula y número.', 'error');
            }
            if (n !== c) {
                return Utils.toast('La confirmación no coincide.', 'error');
            }
            if (n === a) {
                return Utils.toast('La nueva contraseña no puede ser igual a la actual.', 'error');
            }

            try {
                await API.request('/usuarios/cuenta/contrasena', {
                    method: 'PUT',
                    body: JSON.stringify({ contrasenaActual: a, contrasenaNueva: n })
                });
                actual.value = '';
                nueva.value = '';
                confirmar.value = '';
                Utils.toast('Contraseña actualizada.', 'success');
            } catch (err) {
                Utils.toast(err.message || 'No se pudo cambiar la contraseña.', 'error');
            }
        });
    }
};

document.addEventListener('DOMContentLoaded', () => Ajustes.init());
