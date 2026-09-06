/**
 * MAPS Connect - Autenticación y sesión
 */

const Auth = {
    TOKEN_KEY: 'token',
    USER_KEY: 'user',

    saveSession(token, user) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getUser() {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    },

    /** Actualiza la foto del usuario en la sesión local (nuevaFoto null = eliminar). */
    foto(nuevaFoto) {
        const user = this.getUser();
        if (!user) return;
        user.foto = nuevaFoto || null;
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    /** Actualiza campos del usuario en la sesión local (p. ej. nombre). */
    actualizar(patch) {
        const user = this.getUser();
        if (!user) return;
        Object.assign(user, patch);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        window.location.href = Auth.resolvePath('index.html');
    },

    /** Redirige a login si no hay sesión. Usar en páginas protegidas. */
    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = this.resolvePath('login.html');
            return false;
        }
        return true;
    },

    /** Valida la sesión contra el servidor y redirige al dashboard si es válida.
     *  Usar en login/registro: evita el bucle con tokens viejos o expirados. */
    async redirectIfAuthenticated() {
        if (!this.isAuthenticated()) return false;
        try {
            await API.request('/auth/perfil-estado');
            window.location.href = this.resolvePath('pages/inicio.html');
            return true;
        } catch (e) {
            // Token inválido o expirado: se limpia la sesión y se deja ver el login.
            localStorage.removeItem(this.TOKEN_KEY);
            localStorage.removeItem(this.USER_KEY);
            return false;
        }
    },

    /** Si el perfil está incompleto, lleva al usuario a completar onboarding. */
    async redirectIfPerfilIncompleto() {
        if (!this.isAuthenticated()) return false;
        try {
            const res = await API.request('/auth/perfil-estado');
            if (!res?.completado) {
                window.location.href = this.resolvePath('onboarding.html');
                return true;
            }
        } catch (e) {
            // Sin perfil consultable (p. ej. usuario demo) se permite navegar.
        }
        return false;
    },

    resolvePath(relativePath) {
        const inPages = window.location.pathname.includes('/pages/');
        if (inPages && !relativePath.startsWith('../') && !relativePath.startsWith('pages/')) {
            return '../' + relativePath;
        }
        if (!inPages && relativePath.startsWith('pages/')) {
            return relativePath;
        }
        return relativePath;
    }
};
