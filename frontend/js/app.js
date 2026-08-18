// MAPS Connect API Configuration

const API_BASE_URL = 'http://localhost:8080/api';

const API = {
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const token = localStorage.getItem('token');
        
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...token && { 'Authorization': `Bearer ${token}` }
            },
            ...options
        };

        const response = await fetch(url, config);
        if (!response.ok) throw new Error(`API Error: ${response.status}`);
        return response.json();
    },

    // Auth endpoints
    login: (email, password) => API.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    }),

    register: (nombre, email, password, rol) => API.request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ nombre, email, password, rol })
    })
};
