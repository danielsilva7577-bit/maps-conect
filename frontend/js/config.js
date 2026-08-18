/* ========================
   API CONFIGURATION
   ======================== */

const API_CONFIG = {
    BASE_URL: 'http://localhost:8080/api',
    TIMEOUT: 5000,
    ENDPOINTS: {
        AUTH: {
            LOGIN: '/auth/login',
            REGISTER: '/auth/register',
            LOGOUT: '/auth/logout',
            REFRESH: '/auth/refresh'
        },
        HEALTH: '/health',
        USERS: '/users',
        FORUM: '/forum',
        TIPS: '/tips',
        RESOURCES: '/resources',
        CHAT: '/chat',
        COMMUNITIES: '/communities',
        COMPANIES: '/companies'
    }
};

/* ========================
   UTILITY FUNCTIONS
   ======================== */

/**
 * Realiza peticiones HTTP a la API
 * @param {string} endpoint - Endpoint de la API
 * @param {string} method - GET, POST, PUT, DELETE
 * @param {object} data - Datos a enviar (opcional)
 * @returns {Promise}
 */
async function fetchAPI(endpoint, method = 'GET', data = null) {
    const url = `${API_CONFIG.BASE_URL}${endpoint}`;
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    // Añadir token JWT si existe
    const token = localStorage.getItem('authToken');
    if (token) {
        options.headers['Authorization'] = `Bearer ${token}`;
    }

    // Añadir body si es POST, PUT o PATCH
    if (data && ['POST', 'PUT', 'PATCH'].includes(method)) {
        options.body = JSON.stringify(data);
    }

    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT);

        const response = await fetch(url, {
            ...options,
            signal: controller.signal
        });

        clearTimeout(timeoutId);

        // Parsear respuesta
        const contentType = response.headers.get('content-type');
        let result;
        
        if (contentType && contentType.includes('application/json')) {
            result = await response.json();
        } else {
            result = await response.text();
        }

        // Validar status code
        if (!response.ok) {
            throw {
                status: response.status,
                data: result,
                message: result?.message || response.statusText
            };
        }

        return result;
    } catch (error) {
        if (error instanceof TypeError) {
            throw new Error('Error de conexión. Verifica que el backend esté ejecutándose.');
        }
        throw error;
    }
}

/**
 * Almacena el token JWT
 * @param {string} token - JWT token
 * @param {number} expiresIn - Tiempo de expiración en milisegundos
 */
function setAuthToken(token, expiresIn) {
    localStorage.setItem('authToken', token);
    if (expiresIn) {
        const expiryTime = Date.now() + expiresIn;
        localStorage.setItem('tokenExpiry', expiryTime);
    }
}

/**
 * Obtiene el token JWT almacenado
 * @returns {string|null}
 */
function getAuthToken() {
    return localStorage.getItem('authToken');
}

/**
 * Elimina el token JWT
 */
function clearAuthToken() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('tokenExpiry');
}

/**
 * Verifica si el token es válido
 * @returns {boolean}
 */
function isTokenValid() {
    const token = getAuthToken();
    const expiry = localStorage.getItem('tokenExpiry');
    
    if (!token || !expiry) return false;
    return Date.now() < parseInt(expiry);
}

/**
 * Obtiene datos del usuario almacenados
 * @returns {object|null}
 */
function getStoredUser() {
    const user = localStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
}

/**
 * Almacena datos del usuario
 * @param {object} user - Objeto usuario
 */
function setStoredUser(user) {
    localStorage.setItem('currentUser', JSON.stringify(user));
}

/**
 * Elimina datos del usuario
 */
function clearStoredUser() {
    localStorage.removeItem('currentUser');
}

/**
 * Muestra un mensaje de error global
 * @param {string} message - Mensaje de error
 */
function showError(message) {
    const errorElement = document.getElementById('globalError');
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
        setTimeout(() => {
            errorElement.style.display = 'none';
        }, 5000);
    }
}

/**
 * Valida formato de email institucional
 * @param {string} email - Email a validar
 * @returns {boolean}
 */
function isValidInstitutionalEmail(email) {
    const institutionalDomains = ['@tecmilenio.mx', '@servicios.tecmilenio.mx'];
    return institutionalDomains.some(domain => email.endsWith(domain));
}

/**
 * Valida contraseña (mínimo 8 caracteres, mayúscula, número)
 * @param {string} password - Contraseña a validar
 * @returns {boolean}
 */
function isValidPassword(password) {
    const minLength = 8;
    const hasUpperCase = /[A-Z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    
    return password.length >= minLength && hasUpperCase && hasNumber;
}

/**
 * Valida nombre completo
 * @param {string} name - Nombre a validar
 * @returns {boolean}
 */
function isValidName(name) {
    return name.trim().length >= 3 && name.trim().split(' ').length >= 2;
}

/**
 * Formatea fecha para mostrar
 * @param {string|Date} date - Fecha
 * @returns {string}
 */
function formatDate(date) {
    const d = new Date(date);
    return d.toLocaleDateString('es-MX', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Trunca texto a N caracteres
 * @param {string} text - Texto
 * @param {number} length - Longitud máxima
 * @returns {string}
 */
function truncateText(text, length = 100) {
    return text.length > length ? text.substring(0, length) + '...' : text;
}

/**
 * Limpia formulario
 * @param {HTMLFormElement} form - Formulario
 */
function clearForm(form) {
    form.reset();
    form.querySelectorAll('.error-message').forEach(el => {
        el.classList.remove('show');
    });
}

/**
 * Muestra elemento
 * @param {HTMLElement} element - Elemento
 */
function show(element) {
    if (element) element.style.display = '';
}

/**
 * Oculta elemento
 * @param {HTMLElement} element - Elemento
 */
function hide(element) {
    if (element) element.style.display = 'none';
}
