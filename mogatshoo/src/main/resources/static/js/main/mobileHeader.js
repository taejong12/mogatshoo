document.addEventListener('DOMContentLoaded', function() {
    console.log('📱 고유 모바일 헤더 스크립트 로드됨');

    // 🔥 새로운 이름의 요소들
    const uniqueMenuBtn = document.getElementById('uniqueMenuBtn');
    const uniqueDropdownPanel = document.getElementById('uniqueDropdownPanel');
    const uniqueMenuOverlay = document.getElementById('uniqueMenuOverlay');
    
    console.log('📱 요소 확인:', {
        button: !!uniqueMenuBtn,
        panel: !!uniqueDropdownPanel,
        overlay: !!uniqueMenuOverlay
    });

    if (uniqueMenuBtn && uniqueDropdownPanel) {
        console.log('🎯 고유 드롭다운 모드 활성화');
        setupUniqueDropdown();
    } else {
        console.warn('⚠️ 고유 모바일 메뉴 요소를 찾을 수 없습니다');
    }

    /**
     * 🔥 고유 드롭다운 설정
     */
    function setupUniqueDropdown() {
        // 드롭다운 토글 버튼
        uniqueMenuBtn.addEventListener('click', handleUniqueToggle);

        // 오버레이 클릭
        if (uniqueMenuOverlay) {
            uniqueMenuOverlay.addEventListener('click', closeUniqueDropdown);
        }

        // 메뉴 아이템 클릭
        setupUniqueMenuItems();

        // 외부 클릭 처리
        document.addEventListener('click', handleUniqueOutsideClick);

        // ESC 키 처리
        document.addEventListener('keydown', handleUniqueKeydown);
    }

    /**
     * 고유 드롭다운 토글 처리
     */
    function handleUniqueToggle(e) {
        e.preventDefault();
        e.stopPropagation();
        
        console.log('📱 고유 드롭다운 토글 클릭');
        
        const isActive = uniqueDropdownPanel.classList.contains('unique-active');
        
        if (isActive) {
            closeUniqueDropdown();
        } else {
            openUniqueDropdown();
        }
    }

    /**
     * 고유 드롭다운 열기
     */
    function openUniqueDropdown() {
        console.log('📱 고유 드롭다운 열기');
        
        uniqueDropdownPanel.classList.add('unique-active');
        uniqueMenuBtn.classList.add('unique-active');
        if (uniqueMenuOverlay) uniqueMenuOverlay.classList.add('unique-active');
        
        console.log('✅ 고유 드롭다운 열기 완료');
    }

    /**
     * 고유 드롭다운 닫기
     */
    function closeUniqueDropdown() {
        console.log('📱 고유 드롭다운 닫기');
        
        uniqueDropdownPanel.classList.remove('unique-active');
        uniqueMenuBtn.classList.remove('unique-active');
        if (uniqueMenuOverlay) uniqueMenuOverlay.classList.remove('unique-active');
        
        console.log('✅ 고유 드롭다운 닫기 완료');
    }

    /**
     * 고유 드롭다운 메뉴 아이템 설정
     */
    function setupUniqueMenuItems() {
        const menuItems = document.querySelectorAll('.unique-menu-link');
        
        menuItems.forEach(item => {
            item.addEventListener('click', function(e) {
                const href = this.getAttribute('href');
                
                // 로그아웃 아이템인 경우
                if (this.classList.contains('unique-logout-link') || this.onclick) {
                    return; // onclick이 있으면 그대로 실행
                }
                
                // 일반 링크인 경우
                if (href && href !== '#') {
                    console.log('📱 고유 드롭다운 메뉴 아이템 클릭:', href);
                    closeUniqueDropdown();
                }
            });
        });
    }

    /**
     * 고유 드롭다운 외부 클릭 처리
     */
    function handleUniqueOutsideClick(e) {
        const isActive = uniqueDropdownPanel && uniqueDropdownPanel.classList.contains('unique-active');
        
        if (isActive && 
            !uniqueDropdownPanel.contains(e.target) && 
            !uniqueMenuBtn.contains(e.target)) {
            closeUniqueDropdown();
        }
    }

    /**
     * 고유 드롭다운 키보드 처리
     */
    function handleUniqueKeydown(e) {
        const isActive = uniqueDropdownPanel && uniqueDropdownPanel.classList.contains('unique-active');
        
        if (e.key === 'Escape' && isActive) {
            e.preventDefault();
            closeUniqueDropdown();
        }
    }

    /**
     * 🔥 고유 로그아웃 함수
     */
    window.showUniqueLogout = function() {
        if (uniqueDropdownPanel) closeUniqueDropdown();
        
        if (confirm('정말 로그아웃 하시겠습니까?')) {
            console.log('📱 고유 로그아웃 실행');
            window.location.href = '/logout';
        }
    };

    /**
     * 공개 API
     */
    window.UniqueMobileHeader = {
        open: openUniqueDropdown,
        close: closeUniqueDropdown,
        toggle: function() {
            const isActive = uniqueDropdownPanel.classList.contains('unique-active');
            if (isActive) closeUniqueDropdown();
            else openUniqueDropdown();
        }
    };
});