const CACHE_NAME = 'mogatshoo-v1.0';
const urlsToCache = [
    '/',
    '/css/',
    '/js/',
    '/img/',
    '/webApp/manifest.json'
];

// 다음 주소찾기 관련 도메인들
const DAUM_DOMAINS = [
    't1.daumcdn.net',
    'ssl.daumcdn.net', 
    'postcode.map.daum.net',
	'postcode-search.map.daum.net' 
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

// 네트워크 요청 가로채기 (다음 API는 우회)
self.addEventListener('fetch', function(event) {
    const url = event.request.url;
    
    // 다음 주소찾기 관련 요청은 Service Worker 우회
    if (DAUM_DOMAINS.some(domain => url.includes(domain))) {
        console.log('다음 API 우회:', url);
        return; // Service Worker가 처리하지 않고 브라우저가 직접 처리
    }
    
    // 나머지 요청은 기존 캐싱 전략 적용
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