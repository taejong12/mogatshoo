// 탈모 테스트 페이지 상단에 추가할 스크립트
// (기존 스크립트들보다 먼저 실행되도록 <head> 태그 안에 넣으세요)

(function() {
    console.log("🔍 아이프레임 감지 스크립트 실행");
    
    // 아이프레임 안에서 실행되는지 체크
    function isInIframe() {
        try {
            return window.self !== window.top;
        } catch (e) {
            return true; // 보안상 접근이 차단된 경우 아이프레임으로 간주
        }
    }
    
    // 페이지 로드 시 아이프레임 체크
    if (isInIframe()) {
        console.log("✅ 아이프레임 안에서 실행 중 - 정상 동작");
        
        // 아이프레임 전용 스타일 적용
        document.addEventListener('DOMContentLoaded', function() {
            // body에 아이프레임 클래스 추가
            document.body.classList.add('iframe-mode');
            
            // 사이드바나 네비게이션 숨기기
            const elementsToHide = [
                '.sidebar-component',
                '.sidebar', 
                'nav', 
                '.navigation',
                '.nav-bar',
                '.navbar',
                'header',
                '.header',
                'footer',
                '.footer'
            ];
            
            elementsToHide.forEach(selector => {
                const elements = document.querySelectorAll(selector);
                elements.forEach(el => {
                    if (el) {
                        el.style.display = 'none';
                    }
                });
            });
            
            // 메인 콘텐츠 전체 너비로 확장
            const mainContent = document.querySelector('.main-container, .container, main, .content');
            if (mainContent) {
                mainContent.style.width = '100%';
                mainContent.style.marginLeft = '0';
                mainContent.style.padding = '10px';
            }
        });
        
    } else {
        console.log("❌ 직접 접근 감지됨 - 메인 페이지로 리다이렉트");
        
        // 직접 접근한 경우 메인 페이지로 리다이렉트
        // 단, 개발자나 테스트 목적으로 직접 접근을 허용하려면 주석 처리
        
        // URL 파라미터로 직접 접근 허용 체크
        const urlParams = new URLSearchParams(window.location.search);
        const allowDirect = urlParams.get('direct') === 'true';
        
        if (!allowDirect) {
            // 메인 페이지로 부드럽게 리다이렉트
            setTimeout(() => {
                window.location.replace('/');
            }, 100);
            
            // 임시로 페이지 내용 숨기기
            document.addEventListener('DOMContentLoaded', function() {
                document.body.style.display = 'none';
                
                // 리다이렉트 메시지 표시
                const redirectMsg = document.createElement('div');
                redirectMsg.innerHTML = `
                    <div style="
                        position: fixed; 
                        top: 50%; 
                        left: 50%; 
                        transform: translate(-50%, -50%);
                        background: #2B2BD8;
                        color: white;
                        padding: 20px;
                        border-radius: 10px;
                        font-family: 'Press Start 2P', monospace;
                        font-size: 12px;
                        text-align: center;
                        z-index: 9999;
                        border: 3px solid #FFC107;
                    ">
                        <div>🔄 메인 페이지로 이동 중...</div>
                        <div style="margin-top: 10px; font-size: 10px;">잠시만 기다려주세요</div>
                    </div>
                `;
                document.body.appendChild(redirectMsg);
                document.body.style.display = 'block';
            });
        }
    }
})();

// CSS 추가 (아이프레임 모드용)
const iframeCSS = `
<style>
/* 아이프레임 모드 전용 스타일 */
.iframe-mode {
    margin: 0 !important;
    padding: 0 !important;
}

.iframe-mode .sidebar-component,
.iframe-mode .sidebar,
.iframe-mode nav,
.iframe-mode .navigation,
.iframe-mode .nav-bar,
.iframe-mode .navbar,
.iframe-mode header,
.iframe-mode .header,
.iframe-mode footer,
.iframe-mode .footer {
    display: none !important;
}

.iframe-mode .main-container,
.iframe-mode .container,
.iframe-mode main,
.iframe-mode .content {
    width: 100% !important;
    margin-left: 0 !important;
    padding: 10px !important;
    max-width: none !important;
}

/* 아이프레임에서 전체 화면 사용 */
.iframe-mode .pixel-bg {
    width: 100vw !important;
    height: 100vh !important;
    overflow: hidden !important;
}
</style>
`;

// CSS를 head에 동적 추가
document.addEventListener('DOMContentLoaded', function() {
    if (window.self !== window.top) { // 아이프레임 안에서만
        document.head.insertAdjacentHTML('beforeend', iframeCSS);
    }
});