# Frontend - MAPS Connect

Estructura lista para desarrollar. **HTML5 | CSS3 | JavaScript Vanilla**

## 📁 Estructura

```
frontend/
├── index.html          # Punto de entrada
├── css/
│   └── styles.css      # Estilos
├── js/
│   └── app.js          # Lógica principal + API
├── pages/              # Futuras páginas
└── assets/             # Imágenes
```

## 🚀 Iniciar

### 1. Abrir en Visual Studio Code
```bash
code frontend/
```

### 2. Live Server
- Instalar extensión "Live Server" en VS Code
- Clic derecho en `index.html` → "Open with Live Server"
- Abre en `http://localhost:5500`

### 3. Verificar Backend
```bash
curl http://localhost:8080/api/health
```

## 🔗 API Base URL
```javascript
http://localhost:8080/api
```

## 📝 Usar API

```javascript
// En app.js está disponible el objeto API
await API.login(email, password);
await API.register(nombre, email, password, rol);

// O hacer requests custom
await API.request('/endpoint', { method: 'POST', body: JSON.stringify({...}) });
```

## 🎨 Colores (en CSS)
```css
--primary-color: #1e40af
--secondary-color: #7c3aed
--success-color: #10b981
--danger-color: #ef4444
```

---

**¡Listo para programar! 🚀**
