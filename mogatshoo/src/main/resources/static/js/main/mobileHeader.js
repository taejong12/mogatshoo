document.addEventListener('DOMContentLoaded', function() {
    console.log('📱 모바일 헤더 스크립트 로드됨 - 드롭다운 지원');

    // 🔥 드롭다운 요소들
    const dropdownToggle = document.getElementById('mobileDropdownToggle');
    const dropdownMenu = document.getElementById('mobileDropdownMenu');
    const dropdownOverlay = document.getElementById('mobileDropdownOverlay');
    
    // 기존 사이드바 요소들 (호환성 유지)
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const sidebar = document.querySelector('.sidebar-component, .sidebar');
    const overlay = document.getElementById('mobileOverlay');
    const body = document.body;

    // 🔥 요소 확인 로그
    console.log('📱 요소 확인:', {
        dropdown: {
            toggle: !!dropdownToggle,
            menu: !!dropdownMenu,
            overlay: !!dropdownOverlay
        },
        sidebar: {
            toggle: !!mobileMenuToggle,
            sidebar: !!sidebar,
            overlay: !!overlay
        }
    });

    // 🔥 드롭다운 우선, 없으면 사이드바 방식 사용
    if (dropdownToggle && dropdownMenu) {
        console.log('🎯 드롭다운 모드 활성화');
        setupDropdownMode();
    } else if (mobileMenuToggle && sidebar) {
        console.log('🎯 사이드바 모드 활성화');
        setupSidebarMode();
    } else {
        console.warn('⚠️ 모바일 메뉴 요소를 찾을 수 없습니다');
    }

    /**
     * 🔥 드롭다운 모드 설정
     */
    function setupDropdownMode() {
        // 드롭다운 토글 버튼
        dropdownToggle.addEventListener('click', handleDropdownToggle);

        // 오버레이 클릭
        if (dropdownOverlay) {
            dropdownOverlay.addEventListener('click', closeDropdown);
        }

        // 메뉴 아이템 클릭
        setupDropdownMenuItems();

        // 외부 클릭 처리
        document.addEventListener('click', handleDropdownOutsideClick);

        // ESC 키 처리
        document.addEventListener('keydown', handleDropdownKeydown);
    }

    /**
     * 드롭다운 토글 처리
     */
    function handleDropdownToggle(e) {
        e.preventDefault();
        e.stopPropagation();
        
        console.log('📱 드롭다운 토글 클릭');
        
        const isActive = dropdownMenu.classList.contains('active');
        
        if (isActive) {
            closeDropdown();
        } else {
            openDropdown();
        }
    }

    /**
     * 드롭다운 열기
     */
    function openDropdown() {
        console.log('📱 드롭다운 열기');
        
        dropdownMenu.classList.add('active');
        dropdownToggle.classList.add('active');
        if (dropdownOverlay) dropdownOverlay.classList.add('active');
        
        console.log('✅ 드롭다운 열기 완료');
    }

    /**
     * 드롭다운 닫기
     */
    function closeDropdown() {
        console.log('📱 드롭다운 닫기');
        
        dropdownMenu.classList.remove('active');
        dropdownToggle.classList.remove('active');
        if (dropdownOverlay) dropdownOverlay.classList.remove('active');
        
        console.log('✅ 드롭다운 닫기 완료');
    }

    /**
     * 드롭다운 메뉴 아이템 설정
     */
    function setupDropdownMenuItems() {
        const menuItems = document.querySelectorAll('.dropdown-item');
        
        menuItems.forEach(item => {
            item.addEventListener('click', function(e) {
                const href = this.getAttribute('href');
                
                // 로그아웃 아이템인 경우
                if (this.classList.contains('logout-item') || this.onclick) {
                    return; // onclick이 있으면 그대로 실행
                }
                
                // 일반 링크인 경우
                if (href && href !== '#') {
                    console.log('📱 드롭다운 메뉴 아이템 클릭:', href);
                    closeDropdown();
                }
            });
        });
    }

    /**
     * 드롭다운 외부 클릭 처리
     */
    function handleDropdownOutsideClick(e) {
        const isActive = dropdownMenu && dropdownMenu.classList.contains('active');
        
        if (isActive && 
            !dropdownMenu.contains(e.target) && 
            !dropdownToggle.contains(e.target)) {
            closeDropdown();
        }
    }

    /**
     * 드롭다운 키보드 처리
     */
    function handleDropdownKeydown(e) {
        const isActive = dropdownMenu && dropdownMenu.classList.contains('active');
        
        if (e.key === 'Escape' && isActive) {
            e.preventDefault();
            closeDropdown();
        }
    }

    /**
     * 🔥 사이드바 모드 설정 (기존 방식 - 호환성)
     */
    function setupSidebarMode() {
        // 모바일 메뉴 토글
        if (mobileMenuToggle) {
            mobileMenuToggle.addEventListener('click', handleMenuToggle);
        }

        // 오버레이 클릭
        if (overlay) {
            overlay.addEventListener('click', closeMobileMenu);
        }

        // 사이드바 내부 링크 클릭
        setupSidebarLinks();

        // 화면 크기 변경
        window.addEventListener('resize', handleResize);

        // ESC 키 처리
        document.addEventListener('keydown', handleKeydown);
    }

    function handleMenuToggle(e) {
        e.preventDefault();
        console.log('📱 사이드바 토글 클릭');
        
        const isActive = sidebar && sidebar.classList.contains('active');
        
        if (isActive) {
            closeMobileMenu();
        } else {
            openMobileMenu();
        }
    }

    function openMobileMenu() {
        console.log('📱 사이드바 열기');
        
        if (!sidebar) return;
        
        sidebar.classList.add('active');
        if (overlay) overlay.classList.add('active');
        body.classList.add('sidebar-active');
        if (mobileMenuToggle) mobileMenuToggle.classList.add('active');
    }

    function closeMobileMenu() {
        console.log('📱 사이드바 닫기');
        
        if (!sidebar) return;
        
        sidebar.classList.remove('active');
        if (overlay) overlay.classList.remove('active');
        body.classList.remove('sidebar-active');
        if (mobileMenuToggle) mobileMenuToggle.classList.remove('active');
    }

    function setupSidebarLinks() {
        const sidebarLinks = document.querySelectorAll('.sidebar-component a');
        
        sidebarLinks.forEach(link => {
            link.addEventListener('click', function() {
                const href = this.getAttribute('href');
                if (href && !href.startsWith('#')) {
                    setTimeout(() => {
                        closeMobileMenu();
                    }, 100);
                }
            });
        });
    }

    function handleResize() {
        if (window.innerWidth > 768) {
            if (dropdownMenu) closeDropdown();
            if (sidebar) closeMobileMenu();
        }
    }

    function handleKeydown(e) {
        if (e.key === 'Escape') {
            if (dropdownMenu && dropdownMenu.classList.contains('active')) {
                closeDropdown();
            } else if (sidebar && sidebar.classList.contains('active')) {
                closeMobileMenu();
            }
        }
    }

    /**
     * 🔥 로그아웃 함수들
     */
    window.showDropdownLogout = function() {
        if (dropdownMenu) closeDropdown();
        
        if (confirm('정말 로그아웃 하시겠습니까?')) {
            console.log('📱 로그아웃 실행');
            window.location.href = '/logout';
        }
    };

    window.showMobileLogoutModal = function() {
        if (typeof showDropdownLogout === 'function') {
            showDropdownLogout();
        } else if (confirm('정말 로그아웃 하시겠습니까?')) {
            window.location.href = '/logout';
        }
    };

    /**
     * 공개 API
     */
    window.MobileHeader = {
        openDropdown: dropdownMenu ? openDropdown : openMobileMenu,
        closeDropdown: dropdownMenu ? closeDropdown : closeMobileMenu,
        toggle: function() {
            if (dropdownMenu) {
                const isActive = dropdownMenu.classList.contains('active');
                if (isActive) closeDropdown();
                else openDropdown();
            } else if (sidebar) {
                const isActive = sidebar.classList.contains('active');
                if (isActive) closeMobileMenu();
                else openMobileMenu();
            }
        }
    };
});