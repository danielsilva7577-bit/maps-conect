const Utils = {
    esc(str) {
        return String(str == null ? '' : str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    iniciales(nombre) {
        if (!nombre) return '?';
        const parts = String(nombre).trim().split(/\s+/);
        const first = parts[0] ? parts[0][0] : '';
        const second = parts[1] ? parts[1][0] : '';
        return (first + second).toUpperCase() || '?';
    },

    /** Avatar con foto si existe; si no, iniciales. clase = clases CSS del avatar. */
    avatarHtml(nombre, foto, clase = 'avatar', alt = '') {
        const safe = this.esc(alt || nombre || 'usuario');
        if (foto) {
            return `<img class="${this.esc(clase)} avatar-foto" src="${foto}" alt="${safe}">`;
        }
        return `<span class="${this.esc(clase)}" aria-hidden="true">${this.esc(this.iniciales(nombre))}</span>`;
    },

    /** Convierte una imagen seleccionada a data URL comprimida (para foto de perfil). */
    fotoDataUrl(archivo, max = 512, calidad = 0.85) {
        return new Promise((resolve, reject) => {
            if (!archivo) return reject(new Error('Selecciona una imagen.'));
            if (!/^image\/(png|jpe?g|webp)$/i.test(archivo.type)) {
                return reject(new Error('Formato no soportado. Usa PNG o JPG.'));
            }
            const reader = new FileReader();
            reader.onerror = () => reject(new Error('No se pudo leer la imagen.'));
            reader.onload = () => {
                const img = new Image();
                img.onerror = () => reject(new Error('La imagen no es válida.'));
                img.onload = () => {
                    const escala = Math.min(1, max / Math.max(img.width, img.height));
                    const w = Math.max(1, Math.round(img.width * escala));
                    const h = Math.max(1, Math.round(img.height * escala));
                    const canvas = document.createElement('canvas');
                    canvas.width = w;
                    canvas.height = h;
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', calidad));
                };
                img.src = reader.result;
            };
            reader.readAsDataURL(archivo);
        });
    },

    /** Cambia el estado visual del botón Seguir/Siguiendo según dataset.siguiendo. */
    pintarBotonSeguir(btn, siguiendo) {
        if (!btn) return;
        btn.dataset.siguiendo = String(siguiendo);
        btn.textContent = siguiendo ? 'Siguiendo' : 'Seguir';
        btn.classList.toggle('siguiendo', siguiendo);
    },

    /** Alterna el seguimiento: llama POST/DELETE /usuarios/{id}/seguir. */
    async alternarSeguir(btn, idUsuario) {
        if (!btn || !idUsuario) return;
        const siguiendo = btn.dataset.siguiendo === 'true';
        const siguiente = !siguiendo;
        btn.disabled = true;
        try {
            await API.request(`/usuarios/${idUsuario}/seguir`, {
                method: siguiente ? 'POST' : 'DELETE'
            });
            this.pintarBotonSeguir(btn, siguiente);
            return siguiente;
        } catch (e) {
            this.toast(e.message || 'No se pudo actualizar el seguimiento.', 'error');
            this.pintarBotonSeguir(btn, siguiendo);
            return siguiendo;
        } finally {
            btn.disabled = false;
        }
    },

    showAlert(containerId, message, type = 'error') {
        const container = document.getElementById(containerId);
        if (!container) return;
        container.innerHTML = `<div class="alert alert-${type}">${message}</div>`;
    },

    clearAlert(containerId) {
        const container = document.getElementById(containerId);
        if (container) container.innerHTML = '';
    },

    /** Muestra una notificación flotante temporal (toast). */
    toast(message, type = 'info', duration = 3500) {
        let root = document.getElementById('toast-root');
        if (!root) {
            root = document.createElement('div');
            root.id = 'toast-root';
            root.setAttribute('aria-live', 'polite');
            document.body.appendChild(root);
        }

        const el = document.createElement('div');
        el.className = `toast toast-${type}`;
        el.textContent = message;
        root.appendChild(el);

        // Animar entrada.
        requestAnimationFrame(() => el.classList.add('show'));

        setTimeout(() => {
            el.classList.remove('show');
            setTimeout(() => el.remove(), 250);
        }, duration);
    }
};
