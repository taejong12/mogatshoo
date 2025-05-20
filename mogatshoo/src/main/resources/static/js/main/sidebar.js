// 윈도우 95/98 스타일 사이드바 JavaScript
// static/js/main/sidebar.js

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
        const menuItems = document.querySelectorAll('.menu-item a');
        
        menuItems.forEach(item => {
            // href 속성 가져오기
            const href = item.getAttribute('href');
            
            // Thymeleaf 구문을 처리 (개발 환경에서는 직접 비교가 필요)
            let fullPath = href;
            if (href && href.includes('@{')) {
                // Thymeleaf 구문에서 경로 추출 시도
                fullPath = href.replace('@{', '').replace('}', '');
            }
            
            // 현재 경로와 비교하여 활성화
            if (currentPath === fullPath || (fullPath !== '/' && currentPath.startsWith(fullPath))) {
                item.parentElement.classList.add('active-menu-item');
                
                // 클릭 효과 (눌려있는 상태) 추가
                const menuIcon = item.querySelector('.menu-icon');
                if (menuIcon) {
                    menuIcon.style.borderStyle = 'inset';
                    menuIcon.style.backgroundColor = '#ffffcc';
                }
            }
        });
    }
    
    // 페이지 로드 시 현재 메뉴 아이템 활성화
    setActiveMenuItem();
    
    // 메뉴 아이템 클릭 효과 (윈도우 95/98 스타일)
    const menuItems = document.querySelectorAll('.menu-item a, .auth-item a, .auth-profile a, .logout-btn');
    
    menuItems.forEach(item => {
        item.addEventListener('mousedown', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon) {
                menuIcon.style.borderStyle = 'inset';
                menuIcon.style.transform = 'translateY(1px)';
            }
        });
        
        item.addEventListener('mouseup', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon && !this.parentElement.classList.contains('active-menu-item')) {
                menuIcon.style.borderStyle = 'outset';
                menuIcon.style.transform = 'translateY(0)';
            }
        });
        
        item.addEventListener('mouseleave', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon && !this.parentElement.classList.contains('active-menu-item')) {
                menuIcon.style.borderStyle = 'outset';
                menuIcon.style.transform = 'translateY(0)';
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