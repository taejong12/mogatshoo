// 사이드바 클릭 문제 해결을 위한 별도의 JavaScript 파일
// 사이드바 상단 3개 메뉴 클릭 문제 해결을 위한 스크립트
// static/js/main/sidebar-fix.js

// 즉시 실행 함수로 스코프 보호
(function() {
    // 페이지 로드 후 실행
    function initSidebarFix() {
        console.log("사이드바 수정 스크립트 실행됨");
        
        // 상단 3개 메뉴 아이템 직접 선택 (순서대로)
        const topMenuItems = document.querySelectorAll('.sidebar-menu .menu-item');
        const topThreeMenus = [].slice.call(topMenuItems, 0, 3);
        
        // 상단 3개 메뉴에 특별한 처리 적용
        topThreeMenus.forEach((menuItem, index) => {
            console.log(`상단 메뉴 ${index + 1} 수정 중...`);
            
            // force-clickable 클래스 추가
            menuItem.classList.add('force-clickable');
            menuItem.style.cursor = 'pointer';
            
            // 링크 요소 가져오기
            const link = menuItem.querySelector('a');
            if (link) {
                // 원본 href 속성 저장
                const originalHref = link.getAttribute('href');
                
                // 클릭 이벤트 직접 추가 (이벤트 버블링 방지 및 직접 이동)
                menuItem.addEventListener('click', function(event) {
                    // 이벤트 전파 중지
                    event.stopPropagation();
                    
                    // Thymeleaf 구문 변환
                    let targetUrl = originalHref;
                    if (originalHref && originalHref.includes('@{')) {
                        // @{/path} 형식에서 실제 URL 추출
                        targetUrl = originalHref.replace('@{', '').replace('}', '');
                    }
                    
                    console.log(`메뉴 ${index + 1} 클릭됨, 이동할 URL: ${targetUrl}`);
                    window.location.href = targetUrl;
                    
                    return false;
                });
                
                // 링크 자체에도 클릭 이벤트 처리
                link.addEventListener('click', function(event) {
                    // 이벤트 전파 중지
                    event.stopPropagation();
                    
                    // Thymeleaf 구문 변환
                    let targetUrl = originalHref;
                    if (originalHref && originalHref.includes('@{')) {
                        // @{/path} 형식에서 실제 URL 추출
                        targetUrl = originalHref.replace('@{', '').replace('}', '');
                    }
                    
                    console.log(`링크 ${index + 1} 클릭됨, 이동할 URL: ${targetUrl}`);
                    window.location.href = targetUrl;
                    
                    return false;
                });
                
                // 중요: 아이콘과 텍스트에도 z-index 적용
                const icon = link.querySelector('.menu-icon');
                const text = link.querySelector('.menu-text');
                
                if (icon) {
                    icon.style.position = 'relative';
                    icon.style.zIndex = '999';
                    icon.style.pointerEvents = 'none'; // 아이콘 자체는 클릭 이벤트 받지 않음
                }
                
                if (text) {
                    text.style.position = 'relative';
                    text.style.zIndex = '999';
                    text.style.pointerEvents = 'none'; // 텍스트 자체는 클릭 이벤트 받지 않음
                }
            }
            
            // 표시 및 위치 관련 스타일 추가
            menuItem.style.position = 'relative';
            menuItem.style.zIndex = '900';
            menuItem.style.pointerEvents = 'auto';
        });
        
        // 첫 번째 메뉴 (홈)에 대한 특별 처리
        const homeMenu = topThreeMenus[0];
        if (homeMenu) {
            homeMenu.onclick = function() {
                console.log("홈 메뉴 클릭됨 - 직접 이동");
                window.location.href = '/';
            };
        }
        
        // 두 번째 메뉴 (투표)에 대한 특별 처리
        const voteMenu = topThreeMenus[1];
        if (voteMenu) {
            voteMenu.onclick = function() {
                console.log("투표 메뉴 클릭됨 - 직접 이동");
                window.location.href = '/vote';
            };
        }
        
        // 세 번째 메뉴 (오늘운세)에 대한 특별 처리
        const fortuneMenu = topThreeMenus[2];
        if (fortuneMenu) {
            fortuneMenu.onclick = function() {
                console.log("오늘운세 메뉴 클릭됨 - 직접 이동");
                window.location.href = '/fortune/start';
            };
        }
    }
    
    // 페이지 로드 완료 시 초기화 함수 실행
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSidebarFix);
    } else {
        // 이미 DOM이 로드된 상태라면 바로 실행
        initSidebarFix();
    }
    
    // 2초 후에도 한 번 더 실행 (혹시 늦게 로드되는 경우를 대비)
    setTimeout(initSidebarFix, 2000);
})();