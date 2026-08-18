# MAPS Connect - Frontend

Frontend minimalista con **HTML5**, **CSS3** y **JavaScript Vanilla** para la plataforma MAPS Connect.

## 📁 Estructura del Proyecto

```
frontend/
├── index.html                  # Página principal (login/dashboard)
├── css/
│   ├── styles.css             # Estilos principales
│   └── responsive.css         # Estilos responsivos
├── js/
│   ├── config.js              # Configuración y utilidades
│   ├── auth.js                # Lógica de autenticación
│   └── main.js                # Lógica principal de la app
├── pages/
│   ├── foro.html              # Módulo de foro
│   ├── tips.html              # Módulo de tips
│   ├── recursos.html          # Módulo de recursos
│   ├── circulos.html          # Módulo de círculos
│   ├── mensajes.html          # Módulo de mensajería
│   ├── empresarial.html       # Módulo empresarial
│   └── perfil.html            # Página de perfil
├── assets/                    # Imágenes y recursos estáticos
└── README.md                  # Este archivo
```

## 🎨 Diseño y Características

### Minimalista
- ✅ Contornos rectos (sin bordes redondeados)
- ✅ Paleta de colores profesional
- ✅ Interfaz limpia y enfocada

### Responsive
- ✅ Tablets (768px y menores)
- ✅ Mobile (480px y menores)
- ✅ Sidebar colapsable en dispositivos pequeños

### Accesibilidad
- ✅ Validaciones de formularios
- ✅ Mensajes de error claros
- ✅ Navegación intuitiva

## 🚀 Cómo Usar

### 1. Abrir en Visual Studio Code

```bash
# Desde el directorio raíz del proyecto
code frontend/
```

### 2. Iniciar Servidor Local

**Opción 1: Usar Live Server (Extensión VS Code)**
- Instalar extensión "Live Server"
- Hacer clic derecho en `index.html`
- Seleccionar "Open with Live Server"
- Se abrirá automáticamente en `http://localhost:5500`

**Opción 2: Python**
```bash
# Python 3
python -m http.server 8000

# Luego acceder a http://localhost:8000
```

**Opción 3: Node.js**
```bash
# Instalar http-server
npm install -g http-server

# Iniciar servidor
http-server
```

### 3. Verificar que el Backend está ejecutándose

```bash
curl http://localhost:8080/api/health
```

## 📝 Archivos Principales

### index.html
Página principal con:
- Formulario de login y registro
- Dashboard con estadísticas
- Navegación principal
- Manejo de autenticación

### css/styles.css
Estilos base:
- Variables CSS (colores, espaciados)
- Componentes reutilizables
- Temas de formularios, botones, tarjetas
- Animaciones

### css/responsive.css
Estilos responsivos:
- Media queries para tablets
- Media queries para mobile
- Sidebar colapsable
- Grid adaptable

### js/config.js
Configuración y utilidades:
- Endpoints de API
- Función `fetchAPI()` con manejo de errores
- Manejo de JWT tokens
- Validaciones (email, contraseña, nombre)
- Funciones de utilidad

### js/auth.js
Autenticación:
- Lógica de login
- Lógica de registro
- Toggle entre modos
- Validaciones de formulario

### js/main.js
Lógica principal:
- Inicialización de la app
- Control de vistas (auth vs dashboard)
- Logout
- Manejo global de errores

## 🔐 Autenticación

### Flujo de Login

1. Usuario ingresa email y contraseña
2. Frontend valida formato (@tecmilenio.mx)
3. Se envía POST a `/auth/login`
4. Backend retorna JWT token
5. Token se almacena en `localStorage`
6. Se carga el dashboard

### Flujo de Registro

1. Usuario completa formulario de registro
2. Validaciones en frontend (email, contraseña, nombre)
3. Se envía POST a `/auth/register`
4. Backend crea usuario y retorna token
5. Auto-login posterior

### Tokens JWT

El token se envía en todas las requests:
```javascript
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Se valida automáticamente en cada petición y se renueva si es necesario.

## 🎨 Colores y Paleta

```css
--primary-color: #1e40af;     /* Azul profesional */
--secondary-color: #7c3aed;   /* Púrpura */
--success-color: #10b981;     /* Verde */
--warning-color: #f59e0b;     /* Ámbar */
--danger-color: #ef4444;      /* Rojo */
--gray-100 a gray-900:        /* Escala de grises */
```

## 📱 Breakpoints Responsivos

- **Desktop**: > 768px (Sidebar 250px)
- **Tablet**: ≤ 768px (Sidebar 70px)
- **Mobile**: ≤ 480px (Sidebar 60px)

## 🔧 Desarrollo

### Agregar Nuevas Páginas

1. Crear archivo HTML en `pages/`
```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Página Nueva - MAPS Connect</title>
    <link rel="stylesheet" href="../css/styles.css">
    <link rel="stylesheet" href="../css/responsive.css">
</head>
<body>
    <!-- Incluir sidebar -->
    <!-- Contenido -->
    <script src="../js/config.js"></script>
    <script src="../js/main.js"></script>
</body>
</html>
```

2. Actualizar link en `index.html`:
```html
<li><a href="pages/nueva-pagina.html" class="nav-link">Nueva Página</a></li>
```

### Llamar Endpoints de API

```javascript
// GET
const data = await fetchAPI('/endpoint', 'GET');

// POST
const response = await fetchAPI('/endpoint', 'POST', {
    campo1: 'valor1',
    campo2: 'valor2'
});

// PUT
const updated = await fetchAPI('/endpoint/1', 'PUT', {
    nombre: 'nuevo nombre'
});

// DELETE
await fetchAPI('/endpoint/1', 'DELETE');
```

### Validaciones Disponibles

```javascript
// Email institucional
isValidInstitutionalEmail('usuario@tecmilenio.mx'); // true

// Contraseña (8+ chars, mayúscula, número)
isValidPassword('Password123'); // true

// Nombre completo
isValidName('Juan Pérez'); // true
```

## 🐛 Debugging

### Verificar Conexión con Backend

```javascript
// En la consola del navegador
fetch('http://localhost:8080/api/health')
    .then(r => r.json())
    .then(d => console.log(d))
    .catch(e => console.error(e));
```

### Ver Token JWT

```javascript
// En la consola
localStorage.getItem('authToken');
```

### Ver Datos de Usuario

```javascript
// En la consola
localStorage.getItem('currentUser');
```

## 📋 Próximas Implementaciones

- [ ] Módulo de Foro (dudas y respuestas)
- [ ] Módulo de Tips (consejos académicos)
- [ ] Repositorio de Recursos (apuntes)
- [ ] Círculos de Estudio
- [ ] Mensajería privada
- [ ] Semestre Empresarial
- [ ] Gestión de Perfil
- [ ] Búsqueda y filtros
- [ ] Notificaciones
- [ ] Paginación

## 🚦 Estado de Desarrollo

**Actual**: Autenticación básica (Login/Registro)  
**Próximo**: Módulos principales

## 📞 Soporte

Para problemas o preguntas, consulta:
- README.md del proyecto (raíz)
- Documentación de API Backend
- Especificación del Proyecto

---

**Última actualización**: Agosto 2024  
**Versión**: 1.0.0  
**Tecnología**: HTML5 | CSS3 | JavaScript Vanilla
