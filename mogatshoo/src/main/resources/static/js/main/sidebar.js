document.addEventListener('DOMContentLoaded', function() {
    // 사이드바 토글 버튼 이벤트 리스너
    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    const sidebarComponent = document.querySelector('.sidebar-component');
    const contentArea = document.querySelector('.content-with-sidebar');
    const body = document.body;
    
    if (sidebarToggleBtn) {
        sidebarToggleBtn.addEventListener('click', function() {
            sidebarComponent.classList.toggle('active');
            body.classList.toggle('sidebar-active');
            if (contentArea) {
                contentArea.classList.toggle('sidebar-active');
            }
        });
    }
    
    // 화면 크기 조절 시 사이드바 상태 관리
    window.addEventListener('resize', function() {
        if (window.innerWidth > 768) {
            // 큰 화면에서는 사이드바 항상 표시
            if (sidebarComponent) {
                sidebarComponent.classList.remove('active');
            }
            if (body) {
                body.classList.remove('sidebar-active');
            }
            if (contentArea) {
                contentArea.classList.remove('sidebar-active');
            }
        }
    });
    
    // 메뉴 아이템 활성화 (현재 페이지 표시)
    function setActiveMenuItem() {
        const currentPath = window.location.pathname;
        
        // 모든 메뉴 아이템 링크 가져오기 (사이드바 메뉴, 인증 영역 메뉴 포함)
        const menuLinks = document.querySelectorAll('.sidebar-menu .menu-item a, .sidebar-auth a');
        
        menuLinks.forEach(link => {
            // href 속성 가져오기
            const href = link.getAttribute('href');
            
            // Thymeleaf 구문을 처리
            let fullPath = href;
            if (href && href.includes('@{')) {
                // Thymeleaf 구문에서 경로 추출 시도
                fullPath = href.replace('@{', '').replace('}', '');
            }
            
            // href가 루트 경로(/)인 경우 정확히 루트 경로와 일치할 때만 활성화
            if (fullPath === '/' && currentPath === '/') {
                link.closest('.menu-item, .auth-item, .auth-profile').classList.add('active-menu-item');
                
                // 클릭 효과 (눌려있는 상태) 추가
                const menuIcon = link.querySelector('.menu-icon');
                if (menuIcon) {
                    menuIcon.style.borderStyle = 'inset';
                    menuIcon.style.backgroundColor = '#ffffcc';
                }
            } 
            // 현재 경로가 링크의 href로 시작하는 경우 (서브 페이지 포함)
            else if (fullPath !== '/' && currentPath.startsWith(fullPath)) {
                link.closest('.menu-item, .auth-item, .auth-profile').classList.add('active-menu-item');
                
                // 클릭 효과 (눌려있는 상태) 추가
                const menuIcon = link.querySelector('.menu-icon');
                if (menuIcon) {
                    menuIcon.style.borderStyle = 'inset';
                    menuIcon.style.backgroundColor = '#ffffcc';
                }
            }
        });
    }
    
    // 페이지 로드 시 현재 메뉴 아이템 활성화
    setActiveMenuItem();
    
    // 메뉴 아이템 클릭 및 호버 효과 (윈도우 95/98 스타일)
    const menuItems = document.querySelectorAll('.menu-item a, .auth-item a, .auth-profile a, .logout-btn');
    
    menuItems.forEach(item => {
        // 마우스 진입 시 테두리 표시 (활성화된 항목이 아닌 경우만)
        item.addEventListener('mouseenter', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon && !this.closest('.menu-item, .auth-item, .auth-profile').classList.contains('active-menu-item')) {
                menuIcon.style.borderStyle = 'outset';
                menuIcon.style.backgroundColor = '#c0c0c0';
                menuIcon.style.boxShadow = '1px 1px 0 #000000';
            }
        });
        
        // 마우스 떠날 때 테두리 제거 (활성화된 항목이 아닌 경우만)
        item.addEventListener('mouseleave', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon && !this.closest('.menu-item, .auth-item, .auth-profile').classList.contains('active-menu-item')) {
                menuIcon.style.borderStyle = '';
                menuIcon.style.backgroundColor = '';
                menuIcon.style.boxShadow = '';
                menuIcon.style.transform = 'translateY(0)';
            }
        });
        
        // 마우스 클릭 시 눌림 효과
        item.addEventListener('mousedown', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon) {
                menuIcon.style.borderStyle = 'inset';
                menuIcon.style.transform = 'translateY(1px)';
                menuIcon.style.backgroundColor = '#ffffcc';
            }
        });
        
        // 마우스 클릭 해제 시 효과 제거 (활성화된 항목이 아닌 경우만)
        item.addEventListener('mouseup', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon && !this.closest('.menu-item, .auth-item, .auth-profile').classList.contains('active-menu-item')) {
                if (this.matches(':hover')) {
                    // 여전히 호버 상태면 호버 스타일 적용
                    menuIcon.style.borderStyle = 'outset';
                    menuIcon.style.backgroundColor = '#c0c0c0';
                    menuIcon.style.transform = 'translateY(0)';
                } else {
                    // 호버 상태가 아니면 모든 스타일 제거
                    menuIcon.style.borderStyle = '';
                    menuIcon.style.backgroundColor = '';
                    menuIcon.style.transform = 'translateY(0)';
                }
            }
        });
    });
    
    // 외부 영역 클릭 시 모바일에서 사이드바 닫기
    document.addEventListener('click', function(event) {
        const isClickInsideSidebar = sidebarComponent && sidebarComponent.contains(event.target);
        const isClickOnToggleBtn = sidebarToggleBtn && sidebarToggleBtn.contains(event.target);
        
        if (window.innerWidth <= 768 && !isClickInsideSidebar && !isClickOnToggleBtn && 
            sidebarComponent && sidebarComponent.classList.contains('active')) {
            sidebarComponent.classList.remove('active');
            if (body) {
                body.classList.remove('sidebar-active');
            }
            if (contentArea) {
                contentArea.classList.remove('sidebar-active');
            }
        }
    });
});