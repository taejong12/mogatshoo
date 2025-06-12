const CACHE_NAME = 'mogatshoo-v1.0';
const urlsToCache = [
  '/',
  '/css/',
  '/js/',
  '/img/',
  '/webApp/manifest.json'
];

// 서비스 워커 설치
self.addEventListener('install', function(event) {
  console.log('Service Worker 설치 중...');
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function(cache) {
        console.log('캐시 저장 완료');
        return cache.addAll(urlsToCache);
      })
      .catch(function(error) {
        console.log('캐시 저장 실패:', error);
      })
  );
});

// 네트워크 요청 가로채기
self.addEventListener('fetch', function(event) {
  event.respondWith(
    caches.match(event.request)
      .then(function(response) {
        // 캐시에 있으면 캐시에서 반환, 없으면 네트워크에서 가져오기
        if (response) {
          return response;
        }
        return fetch(event.request);
      })
      .catch(function(error) {
        console.log('Fetch 실패:', error);
      })
  );
});

// 오래된 캐시 정리
self.addEventListener('activate', function(event) {
  console.log('Service Worker 활성화');
  event.waitUntil(
    caches.keys().then(function(cacheNames) {
      return Promise.all(
        cacheNames.map(function(cacheName) {
          if (cacheName !== CACHE_NAME) {
            console.log('오래된 캐시 삭제:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});