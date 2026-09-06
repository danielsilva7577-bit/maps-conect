document.addEventListener('DOMContentLoaded', async () => {
    await Auth.redirectIfAuthenticated();

    document.getElementById('link-aviso-login')?.addEventListener('click', () => AvisoPrivacidad.mostrar('Aviso de Privacidad'));

    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        Utils.clearAlert('alert-container');

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        if (!email || !password) {
            Utils.showAlert('alert-container', 'Ingresa tu correo y contraseña.');
            return;
        }

        const submitBtn = e.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Iniciando sesión...';

        try {
            const response = await API.request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, contrasena: password })
            });

            Auth.saveSession(response.token, response.usuario);
            try {
                localStorage.removeItem('bienvenida-pendiente');
                sessionStorage.setItem('bienvenida-pendiente', '1');
            } catch (e) {}
            window.location.href = Auth.resolvePath('pages/inicio.html');
        } catch (error) {
            Utils.showAlert('alert-container', error.message || 'No se pudo iniciar sesión.');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Entrar';
        }
    });
});