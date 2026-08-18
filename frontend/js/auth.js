/* ========================
   AUTHENTICATION LOGIC
   ======================== */

const authForm = document.getElementById('authForm');
const authTitle = document.getElementById('authTitle');
const authSubmitBtn = document.getElementById('authSubmitBtn');
const toggleAuthBtn = document.getElementById('toggleAuthBtn');
const toggleText = document.getElementById('toggleText');
const formError = document.getElementById('formError');
const loadingSpinner = document.getElementById('loadingSpinner');

// Form fields elements
const loginFields = document.getElementById('loginFields');
const registerFields = document.getElementById('registerFields');
const loginEmail = document.getElementById('loginEmail');
const loginPassword = document.getElementById('loginPassword');
const registerName = document.getElementById('registerName');
const registerEmail = document.getElementById('registerEmail');
const registerPassword = document.getElementById('registerPassword');
const registerRole = document.getElementById('registerRole');

let isLoginMode = true;

/**
 * Alterna entre modo login y registro
 */
function toggleAuthMode() {
    isLoginMode = !isLoginMode;
    
    if (isLoginMode) {
        authTitle.textContent = 'Iniciar Sesión';
        authSubmitBtn.textContent = 'Iniciar Sesión';
        toggleText.innerHTML = '¿No tienes cuenta? <button type="button" id="toggleAuthBtn" class="link-btn">Regístrate</button>';
        show(loginFields);
        hide(registerFields);
    } else {
        authTitle.textContent = 'Crear Cuenta';
        authSubmitBtn.textContent = 'Registrarse';
        toggleText.innerHTML = '¿Ya tienes cuenta? <button type="button" id="toggleAuthBtn" class="link-btn">Inicia Sesión</button>';
        hide(loginFields);
        show(registerFields);
    }
    
    clearForm(authForm);
    formError.style.display = 'none';
    
    // Re-attach event listener al nuevo botón
    document.getElementById('toggleAuthBtn').addEventListener('click', (e) => {
        e.preventDefault();
        toggleAuthMode();
    });
}

/**
 * Valida formulario de login
 * @returns {boolean}
 */
function validateLoginForm() {
    let isValid = true;
    const emailError = document.getElementById('loginEmailError');
    const passwordError = document.getElementById('loginPasswordError');
    
    // Validar email
    if (!loginEmail.value.trim()) {
        emailError.textContent = 'El email es requerido';
        emailError.classList.add('show');
        isValid = false;
    } else if (!isValidInstitutionalEmail(loginEmail.value)) {
        emailError.textContent = 'Debe usar email institucional (@tecmilenio.mx)';
        emailError.classList.add('show');
        isValid = false;
    } else {
        emailError.classList.remove('show');
    }
    
    // Validar password
    if (!loginPassword.value.trim()) {
        passwordError.textContent = 'La contraseña es requerida';
        passwordError.classList.add('show');
        isValid = false;
    } else {
        passwordError.classList.remove('show');
    }
    
    return isValid;
}

/**
 * Valida formulario de registro
 * @returns {boolean}
 */
function validateRegisterForm() {
    let isValid = true;
    const nameError = document.getElementById('registerNameError');
    const emailError = document.getElementById('registerEmailError');
    const passwordError = document.getElementById('registerPasswordError');
    const roleError = document.getElementById('registerRoleError');
    
    // Validar nombre
    if (!registerName.value.trim()) {
        nameError.textContent = 'El nombre es requerido';
        nameError.classList.add('show');
        isValid = false;
    } else if (!isValidName(registerName.value)) {
        nameError.textContent = 'Ingresa tu nombre completo (mínimo 3 caracteres)';
        nameError.classList.add('show');
        isValid = false;
    } else {
        nameError.classList.remove('show');
    }
    
    // Validar email
    if (!registerEmail.value.trim()) {
        emailError.textContent = 'El email es requerido';
        emailError.classList.add('show');
        isValid = false;
    } else if (!isValidInstitutionalEmail(registerEmail.value)) {
        emailError.textContent = 'Debe usar email institucional (@tecmilenio.mx)';
        emailError.classList.add('show');
        isValid = false;
    } else {
        emailError.classList.remove('show');
    }
    
    // Validar password
    if (!registerPassword.value.trim()) {
        passwordError.textContent = 'La contraseña es requerida';
        passwordError.classList.add('show');
        isValid = false;
    } else if (!isValidPassword(registerPassword.value)) {
        passwordError.textContent = 'Mínimo 8 caracteres, incluir mayúscula y número';
        passwordError.classList.add('show');
        isValid = false;
    } else {
        passwordError.classList.remove('show');
    }
    
    // Validar rol
    if (!registerRole.value) {
        roleError.textContent = 'Selecciona un tipo de usuario';
        roleError.classList.add('show');
        isValid = false;
    } else {
        roleError.classList.remove('show');
    }
    
    return isValid;
}

/**
 * Realiza login
 * @param {string} email - Email del usuario
 * @param {string} password - Contraseña
 */
async function handleLogin(email, password) {
    try {
        show(loadingSpinner);
        hide(authForm);
        formError.style.display = 'none';
        
        const response = await fetchAPI(
            API_CONFIG.ENDPOINTS.AUTH.LOGIN,
            'POST',
            { email, password }
        );
        
        // Guardar token y datos
        setAuthToken(response.token, response.expiresIn);
        setStoredUser(response.usuario);
        
        // Mostrar dashboard
        showDashboard(response.usuario);
    } catch (error) {
        console.error('Error de login:', error);
        formError.textContent = error.message || 'Error al iniciar sesión. Verifica tus credenciales.';
        formError.style.display = 'block';
        hide(loadingSpinner);
        show(authForm);
    }
}

/**
 * Realiza registro
 * @param {object} data - Datos del usuario
 */
async function handleRegister(data) {
    try {
        show(loadingSpinner);
        hide(authForm);
        formError.style.display = 'none';
        
        const response = await fetchAPI(
            API_CONFIG.ENDPOINTS.AUTH.REGISTER,
            'POST',
            data
        );
        
        // Auto-login después de registro
        await handleLogin(data.email, data.password);
    } catch (error) {
        console.error('Error de registro:', error);
        let errorMessage = 'Error al registrarse.';
        
        if (error.status === 400) {
            errorMessage = error.data?.message || 'Email ya registrado o datos inválidos.';
        }
        
        formError.textContent = errorMessage;
        formError.style.display = 'block';
        hide(loadingSpinner);
        show(authForm);
    }
}

/**
 * Maneja el envío del formulario de autenticación
 */
authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    if (isLoginMode) {
        if (validateLoginForm()) {
            await handleLogin(loginEmail.value, loginPassword.value);
        }
    } else {
        if (validateRegisterForm()) {
            await handleRegister({
                nombre: registerName.value,
                email: registerEmail.value,
                password: registerPassword.value,
                rol: registerRole.value
            });
        }
    }
});

/**
 * Toggle botón
 */
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('toggleAuthBtn');
    if (btn) {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            toggleAuthMode();
        });
    }
});
