function fu_addrSearch(){
    // 기존 오버레이가 있다면 제거
    const existingOverlay = document.querySelector('[id*="postcode"]');
    if (existingOverlay && existingOverlay.parentNode) {
        document.body.removeChild(existingOverlay.parentNode);
    }
    
    // 팝업스러운 모달 레이어 생성
    const overlay = document.createElement('div');
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        z-index: 9998;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 20px;
        box-sizing: border-box;
    `;
    
    const layer = document.createElement('div');
    layer.id = 'postcode-layer';
    layer.style.cssText = `
        background: white;
        border: 2px solid #c0c0c0;
        border-radius: 0;
        box-shadow: 2px 2px 0px #808080, 4px 4px 0px #404040;
        width: 100%;
        max-width: 740px;
        height: 90%;
        max-height: 700px;
        display: flex;
        flex-direction: column;
        position: relative;
        
        /* 모바일에서는 전체화면 */
        @media (max-width: 768px) {
            width: 100%;
            height: 100%;
            max-width: 100%;
            max-height: 100%;
            border-radius: 0;
        }
    `;
    
    // 헤더 생성 (Windows95 타이틀바 스타일)
    const header = document.createElement('div');
    header.style.cssText = `
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 2px 4px;
        background: linear-gradient(90deg, #0000ff 0%, #0080ff 100%);
        color: white;
        font-weight: bold;
        font-size: 11px;
        height: 20px;
        box-sizing: border-box;
    `;
    
    const title = document.createElement('span');
    title.textContent = '주소 검색';
    title.style.cssText = 'padding-left: 2px;';
    header.appendChild(title);
    
    // Windows95 스타일 닫기 버튼
    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '×';
    closeBtn.style.cssText = `
        width: 16px;
        height: 16px;
        background: #c0c0c0;
        border: 1px outset #c0c0c0;
        font-size: 12px;
        font-weight: bold;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: black;
        box-sizing: border-box;
    `;
    closeBtn.onclick = function() {
        document.body.removeChild(overlay);
    };
    closeBtn.onmousedown = function() {
        this.style.border = '1px inset #c0c0c0';
    };
    closeBtn.onmouseup = function() {
        this.style.border = '1px outset #c0c0c0';
    };
    header.appendChild(closeBtn);
    
    // 주소검색 영역
    const postcodeArea = document.createElement('div');
    postcodeArea.style.cssText = `
        flex: 1;
        width: 100%;
        height: calc(100% - 24px);
        border: 1px inset #c0c0c0;
        margin: 1px;
        box-sizing: border-box;
    `;
    
    overlay.appendChild(layer);
    layer.appendChild(header);
    layer.appendChild(postcodeArea);
    document.body.appendChild(overlay);
    
    // 오버레이 클릭 시 닫기 (모달 바깥쪽 클릭)
    overlay.onclick = function(e) {
        if (e.target === overlay) {
            document.body.removeChild(overlay);
        }
    };
    
    // 다음주소 embed 방식으로 실행
    new daum.Postcode({
        oncomplete: function(data) {
            // 주소 정보 입력
            document.getElementById('memberZipcode').value = data.zonecode;
            document.getElementById("memberAddress1").value = data.roadAddress;
            
            // 상세주소 입력란으로 포커스 이동
            document.getElementById('memberAddress2').focus();
            
            // 레이어 닫기
            document.body.removeChild(overlay);
        },
        onresize: function(size) {
            // 모바일에서 키보드가 올라올 때 대응
            postcodeArea.style.height = size.height + 'px';
        },
        width: '100%',
        height: '100%'
    }).embed(postcodeArea);
}