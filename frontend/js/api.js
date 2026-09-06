const API_BASE_URL = (() => {
    const path = window.location.pathname;
    const sameOrigin = path === '/' || path === '/index.html' || path.startsWith('/api');
    if (sameOrigin) {
        return `${window.location.origin}/api`;
    }
    return 'http://localhost:8080/api';
})();

const API = {
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const token = localStorage.getItem('token');

        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...(token && { Authorization: `Bearer ${token}` })
            },
            ...options
        };

const response = await fetch(url, config);
        const body = await response.json().catch(() => null);

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                const path = window.location.pathname;
                if (!path.endsWith('login.html') && !path.endsWith('index.html') && !path.endsWith('registro.html')) {
                    const prefix = path.includes('/pages/') ? '../' : '';
                    window.location.href = `${prefix}login.html`;
                }
                throw new Error(body?.message || 'Sesión expirada o sin autorización. Vuelve a iniciar sesión.');
            }
            const error = new Error(body?.message || `Error ${response.status}`);
            error.status = response.status;
            error.data = body && body.data !== undefined ? body.data : body;
            throw error;
        }

        return body && body.data !== undefined ? body.data : body;
    }
};
