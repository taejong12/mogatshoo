// 🚨 브라우저 콘솔에서 실행하세요! (로고 클릭 가능 버전)
console.log('🚀 로고 클릭 가능한 모바일 사이드바!');

// 모바일인지 확인
function isMobile() {
    return window.innerWidth <= 768;
}

if (!isMobile()) {
    console.log('❌ 데스크톱에서는 모바일 사이드바가 안 보입니다. 화면을 모바일 크기로 줄이고 다시 실행하세요.');
} else {
    console.log('✅ 모바일 감지됨! 사이드바 생성 시작...');
    
    // 기존 요소들 제거
    const existing = document.querySelectorAll('.mobile-home-btn, .mobile-menu-btn, .mobile-sidebar-overlay, .mobile-sidebar-container');
    existing.forEach(el => el.remove());
    
    // 1. 홈 버튼 생성
    const homeBtn = document.createElement('a');
    homeBtn.href = '/';
    homeBtn.innerHTML = '🏠';
    homeBtn.style.cssText = `
        display: flex !important;
        position: fixed !important;
        top: 20px !important;
        right: 20px !important;
        width: 60px !important;
        height: 60px !important;
        background: linear-gradient(45deg, #FF6B6B, #4ECDC4) !important;
        border: 3px solid white !important;
        border-radius: 15px !important;
        z-index: 9999 !important;
        color: white !important;
        font-size: 24px !important;
        text-decoration: none !important;
        align-items: center !important;
        justify-content: center !important;
        box-shadow: 0 4px 15px rgba(0,0,0,0.3) !important;
    `;
    document.body.appendChild(homeBtn);
    console.log('✅ 홈 버튼 생성됨');
    
    // 2. 메뉴 버튼 생성
    const menuBtn = document.createElement('div');
    menuBtn.innerHTML = '☰';
    menuBtn.style.cssText = `
        display: flex !important;
        position: fixed !important;
        top: 20px !important;
        left: 20px !important;
        width: 60px !important;
        height: 60px !important;
        background: linear-gradient(45deg, #96CEB4, #FFEAA7) !important;
        border: 3px solid white !important;
        border-radius: 15px !important;
        z-index: 9999 !important;
        color: white !important;
        font-size: 24px !important;
        align-items: center !important;
        justify-content: center !important;
        box-shadow: 0 4px 15px rgba(0,0,0,0.3) !important;
        cursor: pointer !important;
    `;
    document.body.appendChild(menuBtn);
    console.log('✅ 메뉴 버튼 생성됨');
    
    // 3. 오버레이 생성
    const overlay = document.createElement('div');
    overlay.style.cssText = `
        display: none !important;
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        width: 100vw !important;
        height: 100vh !important;
        background: rgba(0,0,0,0.8) !important;
        z-index: 8888 !important;
        backdrop-filter: blur(5px) !important;
    `;
    document.body.appendChild(overlay);
    console.log('✅ 오버레이 생성됨');
    
    // 4. 사이드바 컨테이너 생성
    const sidebar = document.createElement('div');
    sidebar.style.cssText = `
        display: none !important;
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        width: 100vw !important;
        height: 100vh !important;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #667eea 100%) !important;
        z-index: 9000 !important;
        transform: translateX(-100%) !important;
        transition: transform 0.4s ease !important;
        overflow-y: auto !important;
        padding: 100px 30px 50px 30px !important;
        flex-direction: column !important;
        align-items: center !important;
        justify-content: flex-start !important;
    `;
    
    // 🔥 사이드바 내용 (로고 클릭 가능하게 수정)
    sidebar.innerHTML = `
        <div style="
            position: absolute;
            top: 30px;
            right: 30px;
            width: 40px;
            height: 40px;
            background: rgba(255,255,255,0.2);
            border: 2px solid rgba(255,255,255,0.5);
            border-radius: 20px;
            color: white;
            font-size: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
        " onclick="closeSidebar()">×</div>
        
        <div style="text-align: center; margin-bottom: 60px; padding: 20px;">
            <div style="
                width: 100px;
                height: 100px;
                background: rgba(255,255,255,0.2);
                border: 4px solid rgba(255,255,255,0.5);
                border-radius: 25px;
                margin: 0 auto 20px auto;
                display: flex;
                align-items: center;
                justify-content: center;
                box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                cursor: pointer;
            " onclick="goToVoting()">
                <img src="/img/icons/computer.png" style="width: 70px; height: 70px; filter: brightness(1.3);">
            </div>
            <a href="/voting" onclick="closeSidebar()" style="
                font-size: 28px;
                font-weight: bold;
                color: white;
                text-shadow: 0 2px 8px rgba(0,0,0,0.4);
                margin: 0;
                text-decoration: none;
                cursor: pointer;
                display: block;
                padding: 10px;
                border-radius: 10px;
                transition: background-color 0.2s ease;
            " onmouseover="this.style.backgroundColor='rgba(255,255,255,0.1)'" onmouseout="this.style.backgroundColor='transparent'">탈모왕중왕</a>
        </div>
        
        <div style="
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 25px;
            width: 100%;
            max-width: 350px;
            margin: 0 auto;
            padding: 0;
        ">
            <a href="/" onclick="closeSidebar()" style="
                width: 100%;
                height: 110px;
                background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.1) 100%);
                border: 2px solid rgba(255,255,255,0.5);
                border-radius: 25px;
                padding: 15px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.2);
                cursor: pointer;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-decoration: none;
                color: white;
                transition: transform 0.2s ease;
            " onmousedown="this.style.transform='scale(0.9)'" onmouseup="this.style.transform='scale(1)'" onmouseleave="this.style.transform='scale(1)'">
                <div style="
                    width: 55px;
                    height: 55px;
                    background: rgba(255,255,255,0.3);
                    border: 2px solid rgba(255,255,255,0.6);
                    border-radius: 18px;
                    margin-bottom: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">
                    <img src="/img/icons/computer.png" style="width: 40px; height: 40px;">
                </div>
                <div style="font-size: 12px; font-weight: bold; text-align: center; color: white;">홈</div>
            </a>
            
            <a href="/fortune/start" onclick="closeSidebar()" style="
                width: 100%;
                height: 110px;
                background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.1) 100%);
                border: 2px solid rgba(255,255,255,0.5);
                border-radius: 25px;
                padding: 15px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.2);
                cursor: pointer;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-decoration: none;
                color: white;
                transition: transform 0.2s ease;
            " onmousedown="this.style.transform='scale(0.9)'" onmouseup="this.style.transform='scale(1)'" onmouseleave="this.style.transform='scale(1)'">
                <div style="
                    width: 55px;
                    height: 55px;
                    background: rgba(255,255,255,0.3);
                    border: 2px solid rgba(255,255,255,0.6);
                    border-radius: 18px;
                    margin-bottom: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">
                    <img src="/img/icons/computer.png" style="width: 40px; height: 40px;">
                </div>
                <div style="font-size: 12px; font-weight: bold; text-align: center; color: white;">오늘운세</div>
            </a>
            
            <a href="/hairLossTest/testHair" onclick="closeSidebar()" style="
                width: 100%;
                height: 110px;
                background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.1) 100%);
                border: 2px solid rgba(255,255,255,0.5);
                border-radius: 25px;
                padding: 15px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.2);
                cursor: pointer;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-decoration: none;
                color: white;
                transition: transform 0.2s ease;
            " onmousedown="this.style.transform='scale(0.9)'" onmouseup="this.style.transform='scale(1)'" onmouseleave="this.style.transform='scale(1)'">
                <div style="
                    width: 55px;
                    height: 55px;
                    background: rgba(255,255,255,0.3);
                    border: 2px solid rgba(255,255,255,0.6);
                    border-radius: 18px;
                    margin-bottom: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">
                    <img src="/img/icons/computer.png" style="width: 40px; height: 40px;">
                </div>
                <div style="font-size: 12px; font-weight: bold; text-align: center; color: white;">탈모진단</div>
            </a>
            
            <a href="/hospitalMap/hospitals" onclick="closeSidebar()" style="
                width: 100%;
                height: 110px;
                background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.1) 100%);
                border: 2px solid rgba(255,255,255,0.5);
                border-radius: 25px;
                padding: 15px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.2);
                cursor: pointer;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-decoration: none;
                color: white;
                transition: transform 0.2s ease;
            " onmousedown="this.style.transform='scale(0.9)'" onmouseup="this.style.transform='scale(1)'" onmouseleave="this.style.transform='scale(1)'">
                <div style="
                    width: 55px;
                    height: 55px;
                    background: rgba(255,255,255,0.3);
                    border: 2px solid rgba(255,255,255,0.6);
                    border-radius: 18px;
                    margin-bottom: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">
                    <img src="/img/icons/computer.png" style="width: 40px; height: 40px;">
                </div>
                <div style="font-size: 12px; font-weight: bold; text-align: center; color: white;">병원지도</div>
            </a>
            
            <a href="/point/shop/list" onclick="closeSidebar()" style="
                width: 100%;
                height: 110px;
                background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.1) 100%);
                border: 2px solid rgba(255,255,255,0.5);
                border-radius: 25px;
                padding: 15px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.2);
                cursor: pointer;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-decoration: none;
                color: white;
                transition: transform 0.2s ease;
            " onmousedown="this.style.transform='scale(0.9)'" onmouseup="this.style.transform='scale(1)'" onmouseleave="this.style.transform='scale(1)'">
                <div style="
                    width: 55px;
                    height: 55px;
                    background: rgba(255,255,255,0.3);
                    border: 2px solid rgba(255,255,255,0.6);
                    border-radius: 18px;
                    margin-bottom: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">
                    <img src="/img/icons/computer.png" style="width: 40px; height: 40px;">
                </div>
                <div style="font-size: 12px; font-weight: bold; text-align: center; color: white;">포인트샵</div>
            </a>
            
            <a href="/qanda/user" onclick="closeSidebar()" style="
                width: 100%;
                height: 110px;
                background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.1) 100%);
                border: 2px solid rgba(255,255,255,0.5);
                border-radius: 25px;
                padding: 15px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.2);
                cursor: pointer;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-decoration: none;
                color: white;
                transition: transform 0.2s ease;
            " onmousedown="this.style.transform='scale(0.9)'" onmouseup="this.style.transform='scale(1)'" onmouseleave="this.style.transform='scale(1)'">
                <div style="
                    width: 55px;
                    height: 55px;
                    background: rgba(255,255,255,0.3);
                    border: 2px solid rgba(255,255,255,0.6);
                    border-radius: 18px;
                    margin-bottom: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">
                    <img src="/img/icons/computer.png" style="width: 40px; height: 40px;">
                </div>
                <div style="font-size: 12px; font-weight: bold; text-align: center; color: white;">Q & A</div>
            </a>
        </div>
    `;
    
    document.body.appendChild(sidebar);
    console.log('✅ 사이드바 생성됨');
    
    // 이벤트 리스너 설정
    menuBtn.addEventListener('click', function() {
        console.log('📱 메뉴 버튼 클릭됨');
        overlay.style.display = 'block';
        sidebar.style.display = 'flex';
        setTimeout(() => {
            sidebar.style.transform = 'translateX(0)';
        }, 10);
        document.body.style.overflow = 'hidden';
    });
    
    overlay.addEventListener('click', function() {
        closeSidebar();
    });
    
    // 🔥 로고 클릭 함수 추가
    window.goToVoting = function() {
        console.log('🔥 탈모왕중왕 로고 클릭됨 - /voting으로 이동');
        closeSidebar();
        setTimeout(() => {
            window.location.href = '/voting';
        }, 300);
    };
    
    // 닫기 함수를 전역으로 설정
    window.closeSidebar = function() {
        console.log('📱 사이드바 닫기');
        sidebar.style.transform = 'translateX(-100%)';
        setTimeout(() => {
            overlay.style.display = 'none';
            sidebar.style.display = 'none';
        }, 400);
        document.body.style.overflow = '';
    };
    
    console.log('🎉 모바일 사이드바 완성! 탈모왕중왕 로고 클릭 가능!');
    console.log('💡 로고와 로고 아이콘 모두 클릭하면 /voting으로 이동합니다!');
}