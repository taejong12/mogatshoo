// ⭐ 모든 변수와 함수를 DOMContentLoaded 안으로 이동
document.addEventListener("DOMContentLoaded", () => {
    // 아이프레임에서는 푸터 숨기기
    if (window.self !== window.top) {
        const footers = document.querySelectorAll('.footer, .footer-area');
        footers.forEach(footer => {
            if (footer) footer.style.display = 'none';
        });
        return;
    }

    updateClock();
    setInterval(updateClock, 1000);
    
    // 시작 버튼 클릭 이벤트 추가
    const winButton = document.getElementById("win-button");
    
    if (winButton) {
        winButton.addEventListener("click", (e) => {
            e.stopPropagation();
            toggleStartMenu();
        });
    }
    
    // 다른 곳 클릭 시 시작 메뉴 닫기
    document.addEventListener("click", (e) => {
        const startMenu = document.getElementById("start-menu");
        if (startMenu && !startMenu.contains(e.target) && !winButton?.contains(e.target)) {
            hideStartMenu();
        }
    });

    // ⭐ 모달 요소들을 DOM 로드 후에 가져오기
    const footerModal = document.getElementById("modal");
    const footerModalTitle = document.getElementById("modal-title");
    const footerModalContent = document.getElementById("modal-content");
    
    console.log("🦶 푸터 모달 요소 확인:", {
        modal: !!footerModal,
        title: !!footerModalTitle,
        content: !!footerModalContent
    });

    // ⭐ 모달 함수들을 내부 함수로 정의
    function openModal(type) {
        console.log("🦶 openModal 호출:", type);
        
        if (footerModal && footerModalTitle && footerModalContent) {
            if (type === "terms") {
                footerModalTitle.textContent = "이용약관";
                footerModalContent.innerHTML = `
                    <p>이 사이트를 사용함으로써 다음 조건에 동의하는 것으로 간주됩니다...</p>
                    <ul>
                        <li>데이터 수집 동의</li>
                        <li>개인정보 보호 준수</li>
                    </ul>
                `;
            }
            footerModal.style.display = "block";
            console.log("✅ 모달 표시 성공");
        } else {
            console.log("❌ 모달 요소들을 찾을 수 없음");
        }
    }

    function openModal2(type) {
        console.log("🦶 openModal2 호출:", type);
        
        if (footerModal && footerModalTitle && footerModalContent) {
            if (type === "company") {
                footerModalTitle.textContent = "회사 소개";
                footerModalContent.innerHTML = `
                    <p>우리는 창의적이고 혁신적인 솔루션을 제공합니다.</p>
                    <p>주소: 서울특별시 어딘가</p>
                `;
            }
            footerModal.style.display = "block";
            console.log("✅ 모달2 표시 성공");
        } else {
            console.log("❌ 모달 요소들을 찾을 수 없음");
        }
    }

    function closeModal() {
        console.log("🦶 closeModal 호출");
        
        if (footerModal) {
            footerModal.style.display = "none";
            console.log("✅ 모달 닫기 성공");
        }
    }

    // ⭐ 전역 함수로 노출
    window.openModal = openModal;
    window.openModal2 = openModal2;
    window.closeModal = closeModal;
    
    // 나머지 기존 함수들도 전역으로 노출
    window.navigateTo = navigateTo;
    window.navigateToMypage = navigateToMypage;
    window.confirmLogout = confirmLogout;
    window.hideStartMenu = hideStartMenu;
    window.hideConfirmModal = hideConfirmModal;
    window.executeLogout = executeLogout;
});

// ⭐ 나머지 함수들은 DOMContentLoaded 밖에 정의 (전역에서 접근 가능하도록)
function updateClock() {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const clockElement = document.getElementById("clock");
    if (clockElement) {
        clockElement.textContent = `${hh}:${mm}`;
    }
}

function createStartMenu() {
    let existingMenu = document.getElementById("start-menu");
    if (existingMenu) {
        existingMenu.remove();
    }
    
    const isLoggedIn = document.querySelector('[sec\\:authorize="isAuthenticated()"]') !== null ||
                      document.querySelector('.footer-auth .logout-btn') !== null;
    
    const startMenu = document.createElement("div");
    startMenu.id = "start-menu";
    startMenu.className = "modal-window";
    startMenu.style.cssText = `
        display: none;
        width: 200px;
        bottom: 50px;
        left: 10px;
        z-index: 9999;
    `;
    
    let menuContent = '';
    
    if (isLoggedIn) {
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
        menuContent = `
            <div class="modal-header">
                <span>시작 메뉴</span>
                <button onclick="hideStartMenu()" style="background-color: #c0c0c0; border: 2px outset #ffffff; cursor: pointer; font-size: 12px;">×</button>
            </div>
            <div class="modal-content" style="text-align: center; padding: 15px;">
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
    if (winButton) {
        winButton.classList.add("pressed");
    }
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
    if (url === '/') {
        window.location.href = url;
        return;
    }
    
    const sidebarLinks = document.querySelectorAll('.menu-item a');
    for (let link of sidebarLinks) {
        if (link.getAttribute('href') === url) {
            link.click();
            return;
        }
    }
    window.location.href = url;
}

function navigateToMypage() {
    hideStartMenu();
    const mypageLink = document.querySelector('a[href*="/member/mypage"]');
    if (mypageLink) {
        mypageLink.click();
        return;
    }
    
    const sidebarLinks = document.querySelectorAll('.menu-item a');
    for (let link of sidebarLinks) {
        if (link.getAttribute('href')?.includes('/member/mypage')) {
            link.click();
            return;
        }
    }
    window.location.href = '/member/mypage';
}

function confirmLogout() {
    hideStartMenu();
    showConfirmModal();
}

function showConfirmModal() {
    let existingModal = document.getElementById("confirm-modal");
    if (existingModal) existingModal.remove();
    
    let existingOverlay = document.getElementById("modal-overlay");
    if (existingOverlay) existingOverlay.remove();

    const overlay = document.createElement("div");
    overlay.id = "modal-overlay";
    overlay.className = "modal-overlay";
    overlay.style.display = "block";

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
    if (confirmModal) confirmModal.remove();
    if (overlay) overlay.remove();
}

function executeLogout() {
    hideConfirmModal();
    const logoutForm = document.querySelector('.logout-form');
    if (logoutForm) {
        logoutForm.submit();
    } else {
        window.location.href = '/logout';
    }
}