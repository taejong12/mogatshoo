/**
* 공통 JavaScript 함수
*/

// ⭐ DOMContentLoaded 중복 방지 - 이미 로드되었는지 확인
function waitForDOM(callback) {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', callback);
    } else {
        // DOM이 이미 로드된 경우 즉시 실행
        callback();
    }
}

// ⭐ 사이드바 스크립트와의 충돌 방지
function safeInitialize() {
    console.log("🔧 layout.js 초기화 시작");
    
    // 사이드바가 이미 초기화되었는지 확인
    if (window.sidebarInitialized) {
        console.log("⚠️ 사이드바가 이미 초기화됨 - layout 초기화 지연");
        // 사이드바 초기화 후 실행
        setTimeout(() => {
            initCommon();
        }, 100);
    } else {
        // 일반적인 초기화
        initCommon();
    }
}

waitForDOM(safeInitialize);

// 모바일 메뉴 토글 기능
const setupMobileMenu = () => {
    const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
    const mainNav = document.querySelector('.main-nav');

    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', () => {
            mainNav.classList.toggle('active');
        });
        console.log("📱 모바일 메뉴 토글 설정 완료");
    }
};

// 페이지 로드 시 실행되는 공통 함수들
const initCommon = () => {
    console.log("🚀 공통 초기화 시작");
    
    try {
        setupMobileMenu();
        console.log("✅ 공통 초기화 완료");
    } catch (error) {
        console.error("❌ 공통 초기화 오류:", error);
    }
};

/**
* 알림 메시지 표시 함수
* @param {string} message - 표시할 메시지
* @param {string} type - 알림 유형 ('success', 'error', 'info')
*/
function showNotification(message, type = 'info') {
    // 이미 존재하는 알림이 있으면 제거
    const existingNotif = document.querySelector('.notification');
    if (existingNotif) {
        existingNotif.remove();
    }

    // 새 알림 요소 생성
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
            <span>${message}</span>
        </div>
        <button class="notification-close">
            <i class="fas fa-times"></i>
        </button>
    `;

    // 알림 닫기 버튼 이벤트
    const closeBtn = notification.querySelector('.notification-close');
    closeBtn?.addEventListener('click', () => {
        notification.classList.add('hide');
        setTimeout(() => {
            notification.remove();
        }, 300);
    });

    // 알림 자동 제거 타이머
    setTimeout(() => {
        if (document.body.contains(notification)) {
            notification.classList.add('hide');
            setTimeout(() => {
                if (document.body.contains(notification)) {
                    notification.remove();
                }
            }, 300);
        }
    }, 4000);

    // 문서에 알림 요소 추가
    document.body.appendChild(notification);

    // 애니메이션을 위한 타이밍
    setTimeout(() => {
        notification.classList.add('show');
    }, 10);
}

/**
* URL 파라미터 가져오기
* @param {string} name - 파라미터 이름
* @returns {string|null} 파라미터 값 또는 null
*/
function getUrlParameter(name) {
    const url = window.location.search;
    const urlParams = new URLSearchParams(url);
    return urlParams.get(name);
}