# Frontend - MAPS Connect

Entorno listo para programar. La estructura de archivos está definida; el contenido de cada módulo lo implementan ustedes.

## Estructura

```
frontend/
├── index.html, login.html, registro.html
├── css/          base.css, layout.css, components.css
├── js/
│   ├── api.js     Cliente HTTP base
│   ├── auth.js    Sesión JWT
│   ├── utils.js   Helpers mínimos
│   ├── layout.js  Menú lateral
│   └── pages/     Un .js por pantalla
├── pages/         8 módulos (HTML vacío + layout)
└── assets/        Imágenes
```

## Iniciar

1. Live Server en `index.html` → `http://localhost:5500`
2. Backend en `http://localhost:8080/api`

## Por módulo

| Pantalla | HTML | JS |
|----------|------|-----|
| Inicio | `pages/inicio.html` | `js/pages/inicio.js` |
| Perfil | `pages/perfil.html` | `js/pages/perfil.js` |
| Comunidad y Recursos | `pages/comunidad.html` | `js/pages/comunidad.js` |
| Mensajes | `pages/mensajes.html` | `js/pages/mensajes.js` |
| Empresarial | `pages/empresarial.html` | `js/pages/empresarial.js` |
| Círculos | `pages/circulos.html` | `js/pages/circulos.js` |

> "Comunidad y Recursos" integra en pestañas Foro de Dudas, Repositorio de Apuntes y Tips Académicos. Cada pestaña solo renderiza contenido cuando el backend (`/foro`, `/recursos`, `/tips`) devuelve datos.

El contenido de cada pantalla va en `#main-content`.
