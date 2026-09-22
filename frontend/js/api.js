const API_BASE_URL = (() => {
    const origin = window.location.origin;
    if (window.location.port === '5500' || window.location.port === '5501' || window.location.port === '3000') {
        return `${window.location.protocol}//${window.location.hostname}:8080/api`;
    }
    if (origin && origin !== 'null' && !window.location.protocol.startsWith('file')) {
        return `${origin}/api`;
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
            if (response.status === 401) {
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
