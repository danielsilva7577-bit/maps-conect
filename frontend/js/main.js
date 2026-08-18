/* ========================
   MAIN APPLICATION LOGIC
   ======================== */

const authSection = document.getElementById('authSection');
const dashboardSection = document.getElementById('dashboardSection');
const userNameElement = document.getElementById('userName');
const logoutBtn = document.getElementById('logoutBtn');
const sidebar = document.getElementById('sidebar');
const toggleBtn = document.getElementById('toggleBtn');

/**
 * Muestra la sección de dashboard
 * @param {object} user - Datos del usuario
 */
function showDashboard(user) {
    hide(authSection);
    show(dashboardSection);
    
    if (user && userNameElement) {
        const firstName = user.nombre ? user.nombre.split(' ')[0] : 'Usuario';
        userNameElement.textContent = firstName;
    }
    
    loadActivity();
}

/**
 * Muestra la sección de autenticación
 */
function showAuth() {
    show(authSection);
    hide(dashboardSection);
    clearStoredUser();
    clearAuthToken();
}

/**
 * Carga actividad reciente
 */
async function loadActivity() {
    try {
        const activityContainer = document.getElementById('activityContainer');
        
        // Simulación: En producción, esto vendría del backend
        const activities = [
            {
                title: 'Respondiste una duda en Matemáticas',
                time: 'hace 2 horas'
            },
            {
                title: 'Te uniste al círculo de Programación',
                time: 'hace 1 día'
            },
            {
                title: 'Compartiste un tip académico',
                time: 'hace 2 días'
            }
        ];
        
        activityContainer.innerHTML = activities.map(activity => `
            <div class="activity-item">
                <p>${activity.title}</p>
                <small class="text-muted">${activity.time}</small>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error cargando actividad:', error);
    }
}

/**
 * Maneja logout
 */
async function handleLogout() {
    try {
        // Opcional: notificar al backend
        // await fetchAPI(API_CONFIG.ENDPOINTS.AUTH.LOGOUT, 'POST');
    } catch (error) {
        console.error('Error en logout:', error);
    } finally {
        showAuth();
    }
}

/**
 * Verifica si el usuario está autenticado y carga la UI apropiada
 */
function initializeApp() {
    if (isTokenValid() && getStoredUser()) {
        showDashboard(getStoredUser());
    } else {
        clearAuthToken();
        clearStoredUser();
        showAuth();
    }
}

/**
 * Toggle sidebar en móvil
 */
if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
        sidebar.classList.toggle('collapsed');
    });
}

/**
 * Logout button
 */
if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
        if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
            handleLogout();
        }
    });
}

/**
 * Actualizar enlace activo en navegación
 */
function updateActiveNavLink() {
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPage || (currentPage === '' && href === 'index.html')) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

/**
 * Inicializa la aplicación cuando el DOM esté listo
 */
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
    updateActiveNavLink();
});

/**
 * Escucha cambios en el almacenamiento local (para sincronizar entre pestañas)
 */
window.addEventListener('storage', (e) => {
    if (e.key === 'authToken' && !e.newValue) {
        showAuth();
    }
});

/**
 * Manejo global de errores
 */
window.addEventListener('error', (event) => {
    console.error('Error global:', event.error);
    showError('Ocurrió un error inesperado. Por favor, recarga la página.');
});

/**
 * Manejo de rechazos de promesas no capturadas
 */
window.addEventListener('unhandledrejection', (event) => {
    console.error('Promesa rechazada:', event.reason);
    showError('Error de conexión. Verifica tu conexión a internet.');
});
