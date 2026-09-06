/**
 * MAPS Connect - Aviso de Privacidad y Términos y Condiciones
 * Renderiza un modal reutilizable con el contenido legal. Se abre con
 * AvisoPrivacidad.mostrar(). Se usa en login, registro y ajustes.
 */
const AvisoPrivacidad = {
    mostrar(titulo = 'Aviso de Privacidad') {
        let root = document.getElementById('aviso-privacidad-root');
        if (!root) {
            root = document.createElement('div');
            root.id = 'aviso-privacidad-root';
            document.body.appendChild(root);
        }

        root.innerHTML = `
            <div class="aviso-modal-overlay" id="aviso-modal-overlay">
                <div class="aviso-modal" role="dialog" aria-modal="true" aria-label="${titulo}">
                    <div class="aviso-modal-head">
                        <h3>${titulo}</h3>
                        <button type="button" class="aviso-modal-cerrar" aria-label="Cerrar">&times;</button>
                    </div>
                    <div class="aviso-modal-body">
                        <h4>1. Responsable</h4>
                        <p>MAPS Connect, comunidad académica del Tecnológico de Monterrey / Universidad Tecmilenio ("la Plataforma"), es responsable del tratamiento de los datos personales que nos proporcionas, conforme a la Ley Federal de Protección de Datos Personales en Posesión de los Particulares (México).</p>

                        <h4>2. Datos que recabamos</h4>
                        <p>Nombre(s), apellidos, matrícula o nómina, correo institucional, fotografía de perfil, semestre, carrera/campus, y la información que generes al usar la Plataforma (publicaciones, mensajes, círculos de estudio, asesorías y preferencias de la aplicación como el modo oscuro).</p>

                        <h4>3. Finalidades del tratamiento</h4>
                        <p>Los datos se utilizan para: (a) crear y administrar tu cuenta; (b) permitir la interacción entre la comunidad (foro, mensajes, círculos y asesorías); (c) mostrar tu perfil y reputación; (d) notificarte de eventos relevantes; y (e) mejorar el funcionamiento de la Plataforma.</p>

                        <h4>4. Transferencia de datos</h4>
                        <p>Tus datos no se transfieren a terceros ajenos a la Plataforma, salvo que la ley lo exija o que nos otorgues tu consentimiento expreso.</p>

                        <h4>5. Derechos ARCO</h4>
                        <p>Puedes ejercer tus derechos de Acceso, Rectificación, Cancelación y Oposición (ARCO) contactando al administrador a través de los medios institucionales o escribiendo a la cuenta de soporte de la Plataforma.</p>

                        <h4>6. Seguridad</h4>
                        <p>Implementamos medidas técnicas, administrativas y físicas para proteger tus datos personales contra daño, pérdida, alteración, destrucción, uso, acceso o tratamiento no autorizado.</p>

                        <h4>7. Conservación</h4>
                        <p>Tus datos se conservarán mientras tu cuenta esté activa o mientras se requiera para fines legales y de auditoría. Al dar de baja tu cuenta, los datos se eliminarán o se conservarán de forma anonimizada según lo dispuesto por la ley.</p>

                        <h4>8. Cambios al aviso</h4>
                        <p>Este aviso puede actualizarse. La versión vigente estará siempre disponible en esta página.</p>

                        <hr>

                        <h4>Términos y Condiciones</h4>
                        <p>Al crear tu cuenta aceptas utilizar la Plataforma exclusivamente con fines académicos y de convivencia universitaria, respetar a los demás miembros de la comunidad, no difundir contenido ofensivo o ajeno a la institución, y mantener la confidencialidad de tu contraseña. El mal uso de la Plataforma puede resultar en la suspensión o baja de tu cuenta.</p>
                    </div>
                    <div class="aviso-modal-foot">
                        <button type="button" class="btn btn-primary" id="aviso-modal-aceptar">Aceptar</button>
                    </div>
                </div>
            </div>
        `;

        const cerrar = () => root.innerHTML = '';
        root.querySelector('.aviso-modal-cerrar')?.addEventListener('click', cerrar);
        root.querySelector('#aviso-modal-aceptar')?.addEventListener('click', cerrar);
        root.querySelector('#aviso-modal-overlay')?.addEventListener('click', e => {
            if (e.target.id === 'aviso-modal-overlay') cerrar();
        });
    },

    /** Abre el aviso y marca el checkbox de aceptación al confirmar. */
    desdeCheckbox(checkboxId) {
        this.mostrar();
        const cb = document.getElementById(checkboxId);
        if (cb) document.getElementById('aviso-modal-aceptar')?.addEventListener('click', () => { cb.checked = true; });
    }
};
