document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 모바일 사이드바 스크립트 로드됨!');
    
    // 요소들 선택
    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    const sidebarComponent = document.querySelector('.sidebar-component');
    const contentArea = document.querySelector('.content-with-sidebar');
    const body = document.body;
    
    // 사이드바 오버레이 생성 (모바일용)
    let sidebarOverlay = document.querySelector('.sidebar-overlay');
    if (!sidebarOverlay) {
        sidebarOverlay = document.createElement('div');
        sidebarOverlay.className = 'sidebar-overlay';
        document.body.appendChild(sidebarOverlay);
    }
    
    // 모바일 홈 버튼 생성
    let mobileHomeBtn = document.querySelector('.mobile-home-btn');
    if (!mobileHomeBtn) {
        mobileHomeBtn = document.createElement('a');
        mobileHomeBtn.className = 'mobile-home-btn';
        mobileHomeBtn.href = '/';
        mobileHomeBtn.innerHTML = '🏠';
        mobileHomeBtn.title = '홈으로';
        document.body.appendChild(mobileHomeBtn);
    }
    
    // 모바일인지 확인하는 함수
    function isMobile() {
        return window.innerWidth <= 768;
    }
    
    // 사이드바 열기
    function openSidebar() {
        console.log('📱 사이드바 열기');
        if (sidebarComponent) {
            sidebarComponent.classList.add('active');
        }
        if (sidebarOverlay) {
            sidebarOverlay.classList.add('active');
        }
        if (body) {
            body.classList.add('sidebar-active');
            // 모바일에서 스크롤 방지
            if (isMobile()) {
                body.style.overflow = 'hidden';
            }
        }
    }
    
    // 사이드바 닫기
    function closeSidebar() {
        console.log('📱 사이드바 닫기');
        if (sidebarComponent) {
            sidebarComponent.classList.remove('active');
        }
        if (sidebarOverlay) {
            sidebarOverlay.classList.remove('active');
        }
        if (body) {
            body.classList.remove('sidebar-active');
            // 스크롤 복원
            body.style.overflow = '';
        }
        if (contentArea) {
            contentArea.classList.remove('sidebar-active');
        }
    }
    
    // 사이드바 토글 버튼 이벤트
    if (sidebarToggleBtn) {
        sidebarToggleBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            console.log('🔄 토글 버튼 클릭');
            
            if (sidebarComponent && sidebarComponent.classList.contains('active')) {
                closeSidebar();
            } else {
                openSidebar();
            }
        });
        
        // 터치 이벤트도 추가 (모바일 대응)
        sidebarToggleBtn.addEventListener('touchstart', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            if (sidebarComponent && sidebarComponent.classList.contains('active')) {
                closeSidebar();
            } else {
                openSidebar();
            }
        }, { passive: false });
    }
    
    // 오버레이 클릭 시 사이드바 닫기
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', function(e) {
            e.preventDefault();
            closeSidebar();
        });
        
        sidebarOverlay.addEventListener('touchstart', function(e) {
            e.preventDefault();
            closeSidebar();
        }, { passive: false });
    }
    
    // 메뉴 아이템 클릭 시 사이드바 자동 닫기 (모바일에서만)
    const menuItems = document.querySelectorAll('.menu-item a, .auth-item a, .auth-profile a');
    menuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            console.log('📱 메뉴 아이템 클릭:', this.href);
            
            // 모바일에서만 사이드바 자동 닫기
            if (isMobile()) {
                setTimeout(() => {
                    closeSidebar();
                }, 100); // 약간의 지연을 두어 자연스럽게
            }
        });
        
        // 터치 이벤트도 추가
        item.addEventListener('touchstart', function(e) {
            if (isMobile()) {
                // 터치 피드백 (살짝 누르는 효과)
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 150);
            }
        }, { passive: true });
    });
    
    // 화면 크기 조절 시 사이드바 상태 관리
    window.addEventListener('resize', function() {
        console.log('🔄 화면 크기 변경:', window.innerWidth);
        
        if (window.innerWidth > 768) {
            // 데스크톱: 사이드바 상태 리셋
            closeSidebar();
            if (body) {
                body.style.overflow = '';
            }
        }
    });
    
    // 외부 영역 클릭 시 모바일에서 사이드바 닫기
    document.addEventListener('click', function(event) {
        if (!isMobile()) return;
        
        const isClickInsideSidebar = sidebarComponent && sidebarComponent.contains(event.target);
        const isClickOnToggleBtn = sidebarToggleBtn && sidebarToggleBtn.contains(event.target);
        const isClickOnHomeBtn = mobileHomeBtn && mobileHomeBtn.contains(event.target);
        
        if (!isClickInsideSidebar && !isClickOnToggleBtn && !isClickOnHomeBtn && 
            sidebarComponent && sidebarComponent.classList.contains('active')) {
            closeSidebar();
        }
    });
    
    // ESC 키로 사이드바 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && sidebarComponent && sidebarComponent.classList.contains('active')) {
            closeSidebar();
        }
    });
    
    // 메뉴 아이템 활성화 (현재 페이지 표시)
    function setActiveMenuItem() {
        const currentPath = window.location.pathname;
        console.log('🎯 현재 경로:', currentPath);
        
        // 모든 메뉴 아이템에서 활성 클래스 제거
        const allMenuItems = document.querySelectorAll('.menu-item, .auth-item, .auth-profile');
        allMenuItems.forEach(item => {
            item.classList.remove('active-menu-item');
            const menuIcon = item.querySelector('.menu-icon');
            if (menuIcon) {
                menuIcon.style.borderStyle = '';
                menuIcon.style.backgroundColor = '';
            }
        });
        
        // 모든 메뉴 아이템 링크 가져오기
        const menuLinks = document.querySelectorAll('.sidebar-menu .menu-item a, .sidebar-auth a');
        
        menuLinks.forEach(link => {
            const href = link.getAttribute('href');
            let fullPath = href;
            
            // Thymeleaf 구문 처리
            if (href && href.includes('@{')) {
                fullPath = href.replace('@{', '').replace('}', '');
            }
            
            // 경로 매칭
            if (fullPath === '/' && currentPath === '/') {
                link.closest('.menu-item, .auth-item, .auth-profile').classList.add('active-menu-item');
                console.log('✅ 홈 메뉴 활성화');
            } else if (fullPath !== '/' && currentPath.startsWith(fullPath)) {
                link.closest('.menu-item, .auth-item, .auth-profile').classList.add('active-menu-item');
                console.log('✅ 메뉴 활성화:', fullPath);
            }
        });
    }
    
    // 페이지 로드 시 현재 메뉴 아이템 활성화
    setActiveMenuItem();
    
    // 메뉴 아이템 호버 효과 (데스크톱에서만)
    menuItems.forEach(item => {
        // 마우스 진입 시
        item.addEventListener('mouseenter', function() {
            if (!isMobile()) {
                const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
                if (menuIcon && !this.closest('.menu-item, .auth-item, .auth-profile').classList.contains('active-menu-item')) {
                    menuIcon.style.borderStyle = 'outset';
                    menuIcon.style.backgroundColor = '#c0c0c0';
                    menuIcon.style.boxShadow = '1px 1px 0 #000000';
                }
            }
        });
        
        // 마우스 떠날 때
        item.addEventListener('mouseleave', function() {
            if (!isMobile()) {
                const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
                if (menuIcon && !this.closest('.menu-item, .auth-item, .auth-profile').classList.contains('active-menu-item')) {
                    menuIcon.style.borderStyle = '';
                    menuIcon.style.backgroundColor = '';
                    menuIcon.style.boxShadow = '';
                }
            }
        });
        
        // 클릭 효과
        item.addEventListener('mousedown', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon) {
                if (isMobile()) {
                    // 모바일: 스케일 효과
                    this.style.transform = 'scale(0.95)';
                } else {
                    // 데스크톱: 윈도우95 스타일
                    menuIcon.style.borderStyle = 'inset';
                    menuIcon.style.transform = 'translateY(1px)';
                    menuIcon.style.backgroundColor = '#ffffcc';
                }
            }
        });
        
        item.addEventListener('mouseup', function() {
            const menuIcon = this.querySelector('.menu-icon') || this.querySelector('.logout-icon');
            if (menuIcon) {
                if (isMobile()) {
                    // 모바일: 스케일 복원
                    this.style.transform = '';
                } else {
                    // 데스크톱: 원래대로
                    if (!this.closest('.menu-item, .auth-item, .auth-profile').classList.contains('active-menu-item')) {
                        if (this.matches(':hover')) {
                            menuIcon.style.borderStyle = 'outset';
                            menuIcon.style.backgroundColor = '#c0c0c0';
                        } else {
                            menuIcon.style.borderStyle = '';
                            menuIcon.style.backgroundColor = '';
                        }
                        menuIcon.style.transform = 'translateY(0)';
                    }
                }
            }
        });
    });
    
    console.log('✅ 모바일 사이드바 초기화 완료!');
});