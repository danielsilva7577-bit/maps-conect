// MAPS Connect - Service Worker para notificaciones y alertas en segundo plano
self.addEventListener('install', event => {
    self.skipWaiting();
});

self.addEventListener('activate', event => {
    event.waitUntil(self.clients.claim());
});

self.addEventListener('push', event => {
    let data = {};
    try {
        data = event.data ? event.data.json() : {};
    } catch (_) {
        data = { titulo: 'MAPS Connect', preview: event.data ? event.data.text() : 'Nueva notificación' };
    }

    const title = data.titulo || 'MAPS Connect';
    const options = {
        body: data.preview || data.contenido || 'Tienes una nueva actualización',
        icon: '/img/icon-192.png',
        badge: '/img/icon-192.png',
        data: {
            enlace: data.enlace || 'inicio.html'
        },
        vibrate: [100, 50, 100]
    };

    event.waitUntil(self.registration.showNotification(title, options));
});

self.addEventListener('notificationclick', event => {
    event.notification.close();
    const targetUrl = event.notification.data?.enlace || 'inicio.html';

    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clientList => {
            for (const client of clientList) {
                if ('focus' in client) {
                    client.focus();
                    if ('navigate' in client && targetUrl) {
                        client.navigate(targetUrl);
                    }
                    return;
                }
            }
            if (clients.openWindow) {
                return clients.openWindow(targetUrl);
            }
        })
    );
});