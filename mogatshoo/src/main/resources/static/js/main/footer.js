document.addEventListener("DOMContentLoaded", () => {
    updateClock();
    setInterval(updateClock, 1000);
    
    // 시작 버튼 클릭 이벤트 추가
    const winButton = document.getElementById("win-button");
    
    winButton.addEventListener("click", (e) => {
        e.stopPropagation();
        toggleStartMenu();
    });
    
    // 다른 곳 클릭 시 시작 메뉴 닫기
    document.addEventListener("click", (e) => {
        const startMenu = document.getElementById("start-menu");
        if (startMenu && !startMenu.contains(e.target) && !winButton.contains(e.target)) {
            hideStartMenu();
        }
    });
});

function updateClock() {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    document.getElementById("clock").textContent = `${hh}:${mm}`;
}

function createStartMenu() {
    // 시작 메뉴가 이미 있다면 제거하고 새로 생성 (상태 변경 반영)
    let existingMenu = document.getElementById("start-menu");
    if (existingMenu) {
        existingMenu.remove();
    }
    
    // 로그인 상태 확인 (Spring Security 기반)
    const isLoggedIn = document.querySelector('[sec\\:authorize="isAuthenticated()"]') !== null ||
                      document.querySelector('.footer-auth .logout-btn') !== null;
    
    // 시작 메뉴 HTML 생성 (이용약관과 동일한 모달 스타일)
    const startMenu = document.createElement("div");
    startMenu.id = "start-menu";
    startMenu.className = "modal-window"; // 이용약관과 동일한 클래스 사용
    startMenu.style.cssText = `
        display: none;
        width: 200px;
        bottom: 50px;
        left: 10px;
        z-index: 9999;
    `;
    
    let menuContent = '';
    
    if (isLoggedIn) {
        // 로그인된 상태의 모달 스타일 메뉴 (이용약관과 동일한 구조)
        menuContent = `
            <div class="modal-header">
                <span>시작 메뉴</span>
                <button onclick="hideStartMenu()" style="background-color: #c0c0c0; border: 2px outset #ffffff; cursor: pointer; font-size: 12px;">×</button>
            </div>
            <div class="modal-content">
                <div class="start-menu-item-95" onclick="navigateTo('/')">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>홈</span>
                </div>
                <div class="start-menu-item-95" onclick="navigateTo('/fortune/start')">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>오늘운세</span>
                </div>
                <div class="start-menu-item-95" onclick="navigateTo('/hairLossTest/testHair')">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>탈모진단</span>
                </div>
                <div class="start-menu-item-95" onclick="navigateTo('/hospitalMap/hospitals')">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>병원지도</span>
                </div>
                <hr class="win95-divider">
                <div class="start-menu-item-95" onclick="navigateToMypage()">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>내정보</span>
                </div>
                <hr class="win95-divider">
                <div class="start-menu-item-95" onclick="confirmLogout()">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>로그아웃</span>
                </div>
            </div>
        `;
    } else {
        // 로그아웃된 상태의 모달 스타일 메뉴 (이용약관과 동일한 구조)
        menuContent = `
            <div class="modal-header">
                <span>시작 메뉴</span>
                <button onclick="hideStartMenu()" style="background-color: #c0c0c0; border: 2px outset #ffffff; cursor: pointer; font-size: 12px;">×</button>
            </div>
            <div class="modal-content" style="text-align: center; padding: 15px;">
                <div style="margin-bottom: 10px;">
                </div>
                <p style="margin: 10px 0; color: #000080; font-weight: bold;">로그인을 해주세요</p>
                <hr class="win95-divider">
                <div class="start-menu-item-95" onclick="navigateTo('/member/login')">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>로그인</span>
                </div>
                <div class="start-menu-item-95" onclick="navigateTo('/member/join')">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>회원가입</span>
                </div>
            </div>
        `;
    }
    
    startMenu.innerHTML = menuContent;
    document.body.appendChild(startMenu);
    
    return startMenu;
}

function toggleStartMenu() {
    const startMenu = createStartMenu();
    const winButton = document.getElementById("win-button");
    
    if (startMenu.style.display === "block") {
        hideStartMenu();
    } else {
        showStartMenu();
    }
}

function showStartMenu() {
    const startMenu = createStartMenu();
    const winButton = document.getElementById("win-button");
    
    startMenu.style.display = "block";
    winButton.classList.add("pressed");
}

function hideStartMenu() {
    const startMenu = document.getElementById("start-menu");
    const winButton = document.getElementById("win-button");
    
    if (startMenu) {
        startMenu.style.display = "none";
    }
    if (winButton) {
        winButton.classList.remove("pressed");
    }
}

function navigateTo(url) {
    hideStartMenu();
    
    // 메인 페이지나 홈은 직접 이동
    if (url === '/') {
        window.location.href = url;
        return;
    }
    
    // 다른 페이지들은 사이드바의 iframe 시스템 활용
    // 사이드바의 링크를 프로그래밍적으로 클릭
    const sidebarLinks = document.querySelectorAll('.menu-item a');
    for (let link of sidebarLinks) {
        if (link.getAttribute('href') === url) {
            link.click();
            return;
        }
    }
    
    // 사이드바에 링크가 없으면 직접 이동
    window.location.href = url;
}

function navigateToMypage() {
    hideStartMenu();
    
    // 마이페이지 링크 찾기 (푸터에서)
    const mypageLink = document.querySelector('a[href*="/member/mypage"]');
    if (mypageLink) {
        mypageLink.click();
        return;
    }
    
    // 사이드바에서 마이페이지 링크 찾기
    const sidebarLinks = document.querySelectorAll('.menu-item a');
    for (let link of sidebarLinks) {
        if (link.getAttribute('href')?.includes('/member/mypage')) {
            link.click();
            return;
        }
    }
    
    // 직접 이동 (인증된 사용자의 마이페이지로)
    window.location.href = '/member/mypage';
}

function confirmLogout() {
    hideStartMenu();
    showConfirmModal();
}

function showConfirmModal() {
    // 모달이 이미 있다면 제거
    let existingModal = document.getElementById("confirm-modal");
    if (existingModal) {
        existingModal.remove();
    }
    
    let existingOverlay = document.getElementById("modal-overlay");
    if (existingOverlay) {
        existingOverlay.remove();
    }

    // 배경 오버레이 생성
    const overlay = document.createElement("div");
    overlay.id = "modal-overlay";
    overlay.className = "modal-overlay";
    overlay.style.display = "block";

    // 확인 모달 생성
    const confirmModal = document.createElement("div");
    confirmModal.id = "confirm-modal";
    confirmModal.style.display = "block";
    
    confirmModal.innerHTML = `
        <div class="confirm-header">
            <img src="/img/icons/computer.png" alt="확인">
            <span>시스템 종료</span>
        </div>
        <div class="confirm-content">
            <div class="confirm-message">
                정말 로그아웃 하시겠습니까?
            </div>
            <div class="confirm-buttons">
                <button class="confirm-btn" onclick="executeLogout()">확인</button>
                <button class="confirm-btn" onclick="hideConfirmModal()">취소</button>
            </div>
        </div>
    `;

    document.body.appendChild(overlay);
    document.body.appendChild(confirmModal);
}

function hideConfirmModal() {
    const confirmModal = document.getElementById("confirm-modal");
    const overlay = document.getElementById("modal-overlay");
    
    if (confirmModal) {
        confirmModal.remove();
    }
    if (overlay) {
        overlay.remove();
    }
}

function executeLogout() {
    hideConfirmModal();
    
    // 기존 로그아웃 폼이 있다면 제출
    const logoutForm = document.querySelector('.logout-form');
    if (logoutForm) {
        logoutForm.submit();
    } else {
        window.location.href = '/logout';
    }
}

function openModal(type) {
    const modal = document.getElementById("modal");
    const modalTitle = document.getElementById("modal-title");
    const modalContent = document.getElementById("modal-content");

    if (type === "terms") {
        modalTitle.textContent = "이용약관";
        modalContent.innerHTML = `
            <p>이 사이트를 사용함으로써 다음 조건에 동의하는 것으로 간주됩니다...</p>
            <ul>
                <li>데이터 수집 동의</li>
                <li>개인정보 보호 준수</li>
            </ul>
        `;
    } else if (type === "company") {
        modalTitle.textContent = "회사 소개";
        modalContent.innerHTML = `
            <p>우리는 창의적이고 혁신적인 솔루션을 제공합니다.</p>
            <p>주소: 서울특별시 어딘가</p>
        `;
    }

    modal.style.display = "block";
}

function closeModal() {
    document.getElementById("modal").style.display = "none";
}