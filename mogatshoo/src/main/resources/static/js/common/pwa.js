// pwa.js
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function() {
        navigator.serviceWorker.register('/webApp/sw.js')
            .then(function(registration) {
                console.log('✅ Service Worker 등록 성공:', registration.scope);
            })
            .catch(function(error) {
                console.log('❌ Service Worker 등록 실패:', error);
            });
    });
}