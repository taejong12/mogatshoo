// ⭐ 모든 변수와 함수를 DOMContentLoaded 안으로 이동
document.addEventListener("DOMContentLoaded", () => {
	// 🔥 모바일 체크 함수
	function isMobileView() {
		return window.innerWidth <= 768;
	}

	// 🔥 모바일에서는 푸터 관련 기능 완전히 비활성화
	if (isMobileView()) {
		console.log('📱 모바일 환경 - 푸터 기능 비활성화');
		
		// 푸터 숨기기
		const footers = document.querySelectorAll('.footer, .footer-area');
		footers.forEach(footer => {
			if (footer) footer.style.display = 'none';
		});
		
		// 모바일에서는 푸터 관련 이벤트 리스너 등록하지 않음
		return;
	}

	// 아이프레임에서는 푸터 숨기기 (기존 코드)
	if (window.self !== window.top) {
		const footers = document.querySelectorAll('.footer, .footer-area');
		footers.forEach(footer => {
			if (footer) footer.style.display = 'none';
		});
		return;
	}

	// 🔥 데스크톱에서만 푸터 기능 활성화
	console.log('🖥️ 데스크톱 환경 - 푸터 기능 활성화');

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

	// 다른 곳 클릭 시 모든 모달 닫기
	document.addEventListener("click", (e) => {
		const startMenu = document.getElementById("start-menu");
		const footerModal = document.getElementById("modal");

		if (startMenu && !startMenu.contains(e.target) && !winButton?.contains(e.target) &&
			footerModal && !footerModal.contains(e.target) &&
			!e.target.closest('.footer-folder') && !e.target.closest('[onclick*="openModal"]')) {
			closeAllModals();
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

	// ⭐ 통합 모달 관리 함수들
	function closeAllModals() {
		console.log("🔄 모든 모달 닫기");
		hideStartMenu();
		closeModal();
		hideConfirmModal();
	}

	function openModal(type) {
		console.log("🦶 openModal 호출:", type);

		// 다른 모달들 먼저 닫기
		hideStartMenu();
		hideConfirmModal();

		if (footerModal && footerModalTitle && footerModalContent) {
			if (type === "terms") {
				footerModalTitle.textContent = "제작사";
				footerModalContent.innerHTML = `
                    <p>홈페이지를 사용하시면서 개선사항, 불만사항이 있으시면 문의 주세요.</p>
                    <ul>
                        <li>itmogatshoo@gmail.com</li>
                        <li>개발자를 준비하는 3명이 함께 만든 소중한 결과물 입니다.</li>
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

		// 다른 모달들 먼저 닫기
		hideStartMenu();
		hideConfirmModal();

		if (footerModal && footerModalTitle && footerModalContent) {
			if (type === "company") {
				footerModalTitle.textContent = "이용 약관";
				footerModalContent.innerHTML = `
            <p>홈페이지 이용시 동의한 사항</p>
            <li>데이터 수집 동의</li>
            <li>개인정보 보호 준수</li>
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
	window.closeAllModals = closeAllModals;

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
	const authData = document.getElementById('auth-data');
	const memberId = authData.dataset.memberId;
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
                <div class="start-menu-item-95" onclick="navigateTo('/point/shop/list')">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>포인트샵</span>
                </div>
                <hr class="win95-divider">
                <div class="start-menu-item-95" onclick="navigateToMypage('/member/mypage')">
                    <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                    <span>내정보</span>
                </div>
				<div class="start-menu-item-95" onclick="navigateTo('/qanda/user')">
				     <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
				     <span>Q & A</span>
				</div>
            <div class="start-menu-item-95" onclick="navigateTo('/point/detail/list?memberId=${memberId}')">
               <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
               <span>포인트 내역</span>
            </div>
            <div class="start-menu-item-95" onclick="navigateTo('/point/shop/buyList')">
                 <img src="/img/icons/computer.png" width="16" height="16" class="startImg">
                 <span>결제 내역</span>
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
		// 다른 모달들 먼저 닫기
		window.closeModal && window.closeModal();
		hideConfirmModal();
		showStartMenu();
	}
}

function showStartMenu() {
	// 다른 모달들 먼저 닫기
	window.closeModal && window.closeModal();
	hideConfirmModal();

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

	// 🔥 반응형일 때는 직접 페이지 이동
	if (isMobileView()) {
		console.log('📱 시작메뉴에서 반응형 직접 이동:', url);
		window.location.href = url;
		return;
	}

	// 🔥 데스크톱일 때만 사이드바 링크 찾아서 클릭 시도
	const sidebarLinks = document.querySelectorAll('.menu-item a');
	for (let link of sidebarLinks) {
		if (link.getAttribute('href') === url) {
			console.log('🖥️ 데스크톱에서 사이드바 링크 클릭:', url);
			link.click();
			return;
		}
	}

	// 사이드바 링크를 찾지 못했을 때 직접 이동
	console.log('🔄 사이드바 링크 없음, 직접 이동:', url);
	window.location.href = url;
}

// 🔥 마이페이지 네비게이션 함수 수정 (반응형 대응)
// 🔥 마이페이지 네비게이션 함수 수정 (반응형 대응)
function navigateToMypage() {
	hideStartMenu();

	// 🔥 반응형일 때는 memberId 파라미터와 함께 이동
	if (isMobileView()) {
		console.log('📱 시작메뉴에서 반응형 마이페이지 직접 이동');

		// memberId 가져오기
		const authData = document.getElementById('auth-data');
		const memberId = authData?.dataset?.memberId;

		if (memberId) {
			console.log('✅ 멤버ID 찾음:', memberId);
			window.location.href = `/member/mypage?memberId=${encodeURIComponent(memberId)}`;
		} else {
			console.log('❌ 멤버ID 없음, 로그인 페이지로 이동');
			window.location.href = '/member/login';
		}
		return;
	}

	// 🔥 데스크톱일 때만 기존 로직 수행
	const mypageLink = document.querySelector('a[href*="/member/mypage"]');
	if (mypageLink) {
		console.log('🖥️ 데스크톱에서 마이페이지 링크 클릭');
		mypageLink.click();
		return;
	}

	const sidebarLinks = document.querySelectorAll('.menu-item a');
	for (let link of sidebarLinks) {
		if (link.getAttribute('href')?.includes('/member/mypage')) {
			console.log('🖥️ 데스크톱에서 사이드바 마이페이지 링크 클릭');
			link.click();
			return;
		}
	}

	// 링크를 찾지 못했을 때 직접 이동
	console.log('🔄 마이페이지 링크 없음, 직접 이동');
	window.location.href = '/member/mypage';
}

function confirmLogout() {
	hideStartMenu();
	showConfirmModal();
}

function showConfirmModal() {
	// 다른 모달들 먼저 닫기
	hideStartMenu();
	window.closeModal && window.closeModal();

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

$(document).ready(function() {
	// 🚨 푸터 링크 전용 이벤트 핸들러
	$('.footer-link').click(function(e) {
		e.preventDefault(); // 기본 링크 동작 방지
		e.stopPropagation(); // 이벤트 버블링 중지

		const file = $(this).attr('href');
		const title = $(this).find('span').text() || '문서';

		console.log('🟡 푸터 링크 클릭:', file, title);

		// '/' 링크는 일반적인 페이지 이동을 수행합니다.
		if (file === '/') {
			window.location.href = file;
			return;
		}

		// 로그인/회원가입은 메인 윈도우에서 처리
		if (file.includes('/member/join') ||
			file.includes('/member/login') ||
			file.includes('/login') ||
			file.includes('/oauth2/') ||
			file.includes('/logout')) {

			console.log('🔴 푸터에서 메인 윈도우 처리:', file);
			window.location.href = file;
		} else {
			// 🔥 반응형 여부 체크 함수
			function isMobileView() {
				return window.innerWidth <= 768; // 768px 이하를 모바일로 간주
			}

			// 🔥 반응형일 때는 직접 페이지 이동, 데스크톱일 때는 iframe
			if (isMobileView()) {
				console.log('📱 반응형에서 직접 페이지 이동:', file);
				window.location.href = file;
			} else {
				// 포인트, 마이페이지는 iframe에서 처리 (데스크톱만)
				console.log('🔵 푸터에서 iframe 처리:', file);
				openFooterInIframe(file, title);
			}
		}
	});
});

// 푸터에서 iframe 열기 (사이드바와 동일한 방식) - 데스크톱 전용
function openFooterInIframe(file, title) {
	// iframe 관련 요소들이 존재하는지 확인
	const windowElement = $('#win95Window');
	const titleElement = $('.win95-title-text');
	const frameElement = $('#windowContentFrame');

	if (windowElement.length === 0 || titleElement.length === 0 || frameElement.length === 0) {
		console.log('❌ iframe 요소들을 찾을 수 없음. 직접 페이지 이동합니다.');
		window.location.href = file;
		return;
	}

	// 윈도우 제목 설정
	titleElement.text(title);

	// 윈도우 표시 및 위치 설정
	windowElement.css({
		'display': 'block',
		'position': 'fixed',
		'left': '150px',
		'top': '50px',
		'transform': 'none'
	});

	// iframe 소스 설정
	frameElement.attr('src', file);
}

// 푸터 로그아웃 모달 함수
function showFooterLogoutModal() {
	showConfirmModal();
}

// 🔥 반응형 여부 체크 함수 (전역에서 사용)
function isMobileView() {
	return window.innerWidth <= 768; // 768px 이하를 모바일로 간주
}

// 🔥 화면 크기 변경 시 푸터 표시/숨김 동적 처리
window.addEventListener('resize', () => {
	const footer = document.querySelector('.footer');
	if (!footer) return;

	if (window.innerWidth <= 768) {
		// 모바일로 변경될 때 푸터 숨기기
		footer.style.display = 'none';
		console.log('📱 모바일 모드로 전환 - 푸터 숨김');
	} else {
		// 데스크톱으로 변경될 때 푸터 보이기
		footer.style.display = 'flex';
		console.log('🖥️ 데스크톱 모드로 전환 - 푸터 표시');
	}
});

// footer.js에서 기존 openFooterInIframe 함수를 이것으로 교체

// 🔥 초간단 버전 - CSS가 알아서 처리하게 함
function openFooterInIframe(file, title) {
    console.log('🚀 초간단 iframe 열기:', file, title);
    
    // 윈도우 제목 설정
    $('.win95-title-text').text(title);
    
    // 윈도우 보이기 (CSS가 크기 알아서 처리)
    $('#win95Window').show();
    
    // iframe 소스 설정
    $('#windowContentFrame').attr('src', file);
    
    console.log('✅ 완료 - CSS가 반응형 처리함');
}

// 🔥 기존 복잡한 adjustWindowSize 함수들 모두 삭제하고
// 이것만 남겨두세요
$(document).ready(function() {
    console.log('📱🖥️ CSS 반응형 iframe 준비완료');
    
    // resize 이벤트도 필요 없음 - CSS가 알아서 처리
    console.log('🎯 모든 크기 조정은 CSS가 담당');
});