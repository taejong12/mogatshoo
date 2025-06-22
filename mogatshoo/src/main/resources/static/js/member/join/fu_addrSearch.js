function fu_addrSearch() {
    // PWA 환경인지 확인
    const isPWA = window.navigator.standalone === true || 
                  window.matchMedia('(display-mode: standalone)').matches ||
                  document.referrer.includes('android-app://');
    
    console.log('PWA 환경:', isPWA);
    
    // 다음 스크립트가 로드되어 있는지 확인
    if (typeof daum === 'undefined' || !daum.Postcode) {
        if (isPWA) {
            // PWA에서 스크립트 재로딩 시도
            loadDaumScriptForPWA();
        } else {
            alert('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
        }
        return;
    }
    
    // PWA에서는 embed 방식, 웹에서는 기존 방식
    if (isPWA) {
        openPostcodeForPWA();
    } else {
        openPostcodeForWeb();
    }
}

// PWA용 스크립트 재로딩
function loadDaumScriptForPWA() {
    console.log('PWA용 다음 스크립트 재로딩 시작');
    
    const script = document.createElement('script');
    script.src = 'https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
    script.crossOrigin = 'anonymous';
    
    script.onload = function() {
        console.log('PWA 스크립트 로드 성공');
        openPostcodeForPWA();
    };
    
    script.onerror = function() {
        console.error('PWA 스크립트 로드 실패');
        alert('주소 검색 서비스에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.');
    };
    
    document.head.appendChild(script);
}

// PWA용 embed 방식
function openPostcodeForPWA() {
    console.log('PWA용 주소찾기 실행');
    
    // 기존 모달 제거
    const existing = document.getElementById('pwa-postcode-modal');
    if (existing) existing.remove();
    
    // PWA용 모달 생성
    const overlay = document.createElement('div');
    overlay.id = 'pwa-postcode-modal';
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.8);
        z-index: 999999;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 10px;
    `;
    
    const modal = document.createElement('div');
    modal.style.cssText = `
        background: white;
        border-radius: 8px;
        width: 95%;
        max-width: 500px;
        height: 90%;
        max-height: 600px;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    `;
    
    const header = document.createElement('div');
    header.style.cssText = `
        background: #4CAF50;
        color: white;
        padding: 15px;
        font-weight: bold;
        display: flex;
        justify-content: space-between;
        align-items: center;
    `;
    
    header.innerHTML = `
        <span>주소 검색</span>
        <button onclick="document.getElementById('pwa-postcode-modal').remove()" 
                style="background: transparent; border: none; color: white; font-size: 20px; cursor: pointer;">✕</button>
    `;
    
    const container = document.createElement('div');
    container.style.cssText = `
        flex: 1;
        width: 100%;
    `;
    
    modal.appendChild(header);
    modal.appendChild(container);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    
    // PWA용 CSS 강제 적용 (크기 꽉 차게)
    const style = document.createElement('style');
    style.textContent = `
        #pwa-postcode-modal iframe {
            width: 100% !important;
            height: 100% !important;
            min-width: 100% !important;
            min-height: 100% !important;
            border: none !important;
        }
        #pwa-postcode-modal div[id*="__daum__layer"] {
            width: 100% !important;
            height: 100% !important;
            min-width: 100% !important;
            min-height: 100% !important;
        }
    `;
    document.head.appendChild(style);
    
    // 다음 주소찾기 embed (새로고침 로직 제거)
    try {
        new daum.Postcode({
            oncomplete: function(data) {
                console.log('PWA 주소 선택:', data);
                
                document.getElementById('memberZipcode').value = data.zonecode;
                document.getElementById('memberAddress1').value = data.roadAddress;
                document.getElementById('memberAddress2').focus();
                
                overlay.remove();
            },
            onclose: function(state) {
                if (state === 'FORCE_CLOSE') {
                    overlay.remove();
                }
            },
            width: '100%',
            height: '100%'
        }).embed(container);
        
    } catch (error) {
        console.error('PWA 주소찾기 오류:', error);
        alert('주소 검색 중 오류가 발생했습니다.');
        overlay.remove();
    }
}

// 웹용 기존 방식
function openPostcodeForWeb() {
    console.log('웹용 주소찾기 실행');
    
    try {
        // 기존 팝업 방식 (웹에서는 정상 작동)
        new daum.Postcode({
            oncomplete: function(data) {
                console.log('웹 주소 선택:', data);
                
                document.getElementById('memberZipcode').value = data.zonecode;
                document.getElementById('memberAddress1').value = data.roadAddress;
                document.getElementById('memberAddress2').focus();
            }
        }).open();
        
    } catch (error) {
        console.error('웹 주소찾기 오류:', error);
        // 웹에서도 실패하면 embed 방식으로 폴백
        openPostcodeForPWA();
    }
}