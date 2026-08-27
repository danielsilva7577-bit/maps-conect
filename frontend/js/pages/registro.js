const EMAIL_INSTITUCIONAL_REGEX = /^[^\s@]+@(tecmilenio\.mx|servicios\.tecmilenio\.mx)$/i;

document.addEventListener('DOMContentLoaded', () => {
    Auth.redirectIfAuthenticated();

    document.getElementById('registro-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        Utils.clearAlert('alert-container');

        const nombre = document.getElementById('nombre').value.trim();
        const apellido = document.getElementById('apellido').value.trim();
        const email = document.getElementById('email').value.trim();
        const contrasena = document.getElementById('contrasena').value;
        const confirmarContrasena = document.getElementById('confirmar-contrasena').value;

        if (!nombre || !apellido || !email || !contrasena || !confirmarContrasena) {
            Utils.showAlert('alert-container', 'Completa todos los campos.');
            return;
        }

        if (!EMAIL_INSTITUCIONAL_REGEX.test(email)) {
            Utils.showAlert('alert-container', 'Usa tu correo institucional (@tecmilenio.mx o @servicios.tecmilenio.mx).');
            return;
        }

        if (contrasena.length < 6) {
            Utils.showAlert('alert-container', 'La contraseña debe tener al menos 6 caracteres.');
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
            const response = await API.request('/auth/registrar', {
                method: 'POST',
                body: JSON.stringify({ nombre, apellido, email, contrasena })
            });

            Auth.saveSession(response.data.token, response.data.usuario);
            window.location.href = Auth.resolvePath('pages/inicio.html');
        } catch (error) {
            Utils.showAlert('alert-container', error.message || 'No se pudo completar el registro.');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Crear cuenta';
        }
    });
});
