const EMAIL_INSTITUCIONAL_REGEX = /^[^\s@]+@([a-zA-Z0-9.-]+\.)?tecmilenio\.(mx|edu\.mx)$/i;

document.addEventListener('DOMContentLoaded', async () => {
    await Auth.redirectIfAuthenticated();

    document.getElementById('link-aviso-registro')?.addEventListener('click', () => AvisoPrivacidad.mostrar('Aviso de Privacidad'));
    document.getElementById('link-terminos-registro')?.addEventListener('click', () => AvisoPrivacidad.mostrar('Términos y Condiciones'));

    const inputIdentificador = document.getElementById('matricula');

    document.getElementById('registro-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        Utils.clearAlert('alert-container');

        const nombre = document.getElementById('nombre').value.trim();
        const apellido = document.getElementById('apellido').value.trim();
        const email = document.getElementById('email').value.trim();
        const contrasena = document.getElementById('contrasena').value;
        const confirmarContrasena = document.getElementById('confirmar-contrasena').value;
        const identificador = inputIdentificador.value.trim();

        if (!nombre || !apellido || !email || !contrasena || !confirmarContrasena || !identificador) {
            Utils.showAlert('alert-container', 'Completa todos los campos obligatorios.');
            return;
        }

        const acepTerminos = document.getElementById('acepta-terminos');
        if (!acepTerminos || !acepTerminos.checked) {
            Utils.showAlert('alert-container', 'Debes aceptar el Aviso de Privacidad y los Términos y Condiciones para crear tu cuenta.');
            return;
        }

        if (!EMAIL_INSTITUCIONAL_REGEX.test(email)) {
            Utils.showAlert('alert-container', 'El correo debe terminar en @tecmilenio.mx o subdominio institucional (ej. al01234567@tecmilenio.mx).');
            return;
        }

        if (contrasena.length < 8 || !/[A-Z]/.test(contrasena) || !/[a-z]/.test(contrasena) || !/\d/.test(contrasena)) {
            Utils.showAlert('alert-container', 'La contraseña debe tener al menos 8 caracteres y combinar al menos una mayúscula, una minúscula y un número (ej. Tec2026mi).');
            return;
        }

        if (contrasena !== confirmarContrasena) {
            Utils.showAlert('alert-container', 'Las contraseñas no coinciden.');
            return;
        }

        const submitBtn = e.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Creando cuenta...';

        try {
            const payload = {
                nombre,
                apellido,
                email,
                contrasena,
                rol: 'ESTUDIANTE',
                matricula: identificador
            };

            const response = await API.request('/auth/registrar', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            Auth.saveSession(response.token, response.usuario);
            sessionStorage.setItem('pending-matricula', identificador);
            window.location.href = Auth.resolvePath('onboarding.html');
        } catch (error) {
            let msg = error.message || 'No se pudo completar el registro.';
            // Limpiar formato de mapa de errores de validación {campo=mensaje}
            if (msg.startsWith('{') && msg.endsWith('}')) {
                msg = msg.slice(1, -1).split(', ').map(item => {
                    const [k, v] = item.split('=');
                    return v || k;
                }).join(' • ');
            }
            Utils.showAlert('alert-container', msg);
            submitBtn.disabled = false;
            submitBtn.textContent = 'Crear cuenta';
        }
    });
});