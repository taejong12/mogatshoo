/**
 * 질문 수정 페이지 JavaScript
 * Windows 95 스타일 질문 수정 폼의 동작을 제어합니다.
 */

console.log('Win95 스타일 detail.html 페이지가 로드되었습니다!');

// 라디오 버튼 이벤트 및 전체 페이지 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOMContentLoaded - 페이지 초기화 시작');
    
    // 필요한 DOM 요소들 가져오기
    const radioButtons = document.querySelectorAll('input[name="isPublicRadio"]');
    const hiddenInput = document.getElementById('isPublicInput');
    const currentStatus = document.getElementById('currentStatus');
    const statusMessage = document.getElementById('statusMessage');
    const messageContent = document.getElementById('messageContent');
    
    // 요소 존재 여부 확인
    console.log('라디오 버튼 개수:', radioButtons.length);
    console.log('숨겨진 입력 필드:', hiddenInput ? '존재' : '없음');
    console.log('현재 상태 표시:', currentStatus ? '존재' : '없음');
    
    // 라디오 버튼 이벤트 리스너 등록
    if (radioButtons.length > 0 && hiddenInput && currentStatus) {
        radioButtons.forEach(radio => {
            radio.addEventListener('change', function() {
                console.log('라디오 버튼 변경됨:', this.value);
                
                // 숨겨진 입력 필드 값 업데이트
                hiddenInput.value = this.value;
                
                // 현재 상태 텍스트 업데이트
                currentStatus.textContent = this.value === 'yes' ? '공개' : '비공개';
                
                // 상태 변경 메시지 표시
                const statusText = this.value === 'yes' ? '공개' : '비공개';
                const message = `상태가 ${statusText}로 변경됩니다. 저장하기를 클릭하세요.`;
                showMessage(message);
                
                console.log('공개상태 변경 완료:', this.value);
            });
        });
    } else {
        console.warn('라디오 버튼 또는 관련 요소를 찾을 수 없습니다.');
    }
    
    // 메시지 표시 함수
    function showMessage(message) {
        if (statusMessage && messageContent) {
            console.log('메시지 표시:', message);
            
            messageContent.textContent = message;
            statusMessage.style.display = 'block';
            
            // 3초 후 자동으로 숨김
            setTimeout(function() {
                statusMessage.style.display = 'none';
                console.log('메시지 자동 숨김');
            }, 3000);
        } else {
            console.warn('메시지 표시 요소를 찾을 수 없습니다.');
        }
    }
    
    // 이미지 로드 오류 처리
    const images = document.querySelectorAll('.image-preview img');
    console.log('이미지 요소 개수:', images.length);
    
    images.forEach(function(img, index) {
        img.onerror = function() {
            console.log(`이미지 ${index + 1} 로드 실패:`, this.src);
            
            // 이미지 숨기기
            this.style.display = 'none';
            
            // 다음 요소(에러 메시지) 표시
            if (this.nextElementSibling) {
                this.nextElementSibling.style.display = 'block';
                console.log(`이미지 ${index + 1} 에러 메시지 표시됨`);
            }
        };
        
        // 이미지 로드 성공 시 로그
        img.onload = function() {
            console.log(`이미지 ${index + 1} 로드 성공:`, this.src);
        };
    });
    
    // 폼 제출 전 유효성 검사
    const questionForm = document.querySelector('form');
    if (questionForm) {
        questionForm.addEventListener('submit', function(e) {
            console.log('폼 제출 시도');
            
            // 필수 필드 검사
            const questionText = document.getElementById('question');
            const option1 = document.getElementById('option1');
            const option2 = document.getElementById('option2');
            
            if (!questionText || !questionText.value.trim()) {
                alert('질문 내용을 입력해주세요.');
                e.preventDefault();
                return false;
            }
            
            if (!option1 || !option1.value.trim()) {
                alert('보기 1을 입력해주세요.');
                e.preventDefault();
                return false;
            }
            
            if (!option2 || !option2.value.trim()) {
                alert('보기 2를 입력해주세요.');
                e.preventDefault();
                return false;
            }
            
            console.log('폼 유효성 검사 통과 - 제출 진행');
            return true;
        });
    }
    
    // 삭제 버튼 확인 대화상자 개선
    const deleteBtn = document.getElementById('deleteBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function(e) {
            const serialNumber = this.href.match(/\/questions\/([^\/]+)\/delete/);
            const questionNumber = serialNumber ? serialNumber[1] : '이 질문';
            
            const confirmMessage = `정말 ${questionNumber}을(를) 삭제하시겠습니까?\n\n삭제된 질문은 복구할 수 없습니다.`;
            
            if (!confirm(confirmMessage)) {
                e.preventDefault();
                console.log('질문 삭제 취소됨');
                return false;
            }
            
            console.log('질문 삭제 확인됨:', questionNumber);
            return true;
        });
    }
    
    // 취소 버튼 확인 (변경사항이 있는 경우)
    const cancelBtn = document.querySelector('a[href="/questions"]');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', function(e) {
            // 폼이 변경되었는지 확인하는 간단한 로직
            const formChanged = checkFormChanged();
            
            if (formChanged) {
                const confirmMessage = '변경사항이 저장되지 않았습니다.\n정말 취소하시겠습니까?';
                
                if (!confirm(confirmMessage)) {
                    e.preventDefault();
                    console.log('취소 버튼 - 사용자가 머물기를 선택');
                    return false;
                }
            }
            
            console.log('취소 버튼 - 질문 목록으로 이동');
            return true;
        });
    }
    
    // 폼 변경 감지 함수 (간단한 버전)
    function checkFormChanged() {
        // 실제로는 더 정교한 변경 감지 로직을 구현할 수 있습니다.
        // 여기서는 간단히 라디오 버튼 상태만 확인
        const currentRadioValue = document.querySelector('input[name="isPublicRadio"]:checked')?.value;
        const originalValue = hiddenInput?.value;
        
        return currentRadioValue !== originalValue;
    }
    
    // 키보드 단축키 지원
    document.addEventListener('keydown', function(e) {
        // Ctrl+S: 폼 저장
        if (e.ctrlKey && e.key === 's') {
            e.preventDefault();
            if (questionForm) {
                console.log('Ctrl+S 단축키로 폼 제출');
                questionForm.submit();
            }
        }
        
        // ESC: 취소 (질문 목록으로)
        if (e.key === 'Escape') {
            if (cancelBtn) {
                console.log('ESC 키로 취소');
                cancelBtn.click();
            }
        }
    });
    
    console.log('페이지 초기화 완료');
});

// 페이지 언로드 시 정리 작업
window.addEventListener('beforeunload', function(e) {
    console.log('페이지 언로드 중...');
    // 필요한 경우 정리 작업 수행
});

// 전역 함수들 (필요한 경우 외부에서 호출 가능)
window.questionDetailPage = {
    // 메시지 표시 (외부에서 호출 가능)
    showMessage: function(message) {
        const statusMessage = document.getElementById('statusMessage');
        const messageContent = document.getElementById('messageContent');
        
        if (statusMessage && messageContent) {
            messageContent.textContent = message;
            statusMessage.style.display = 'block';
            
            setTimeout(function() {
                statusMessage.style.display = 'none';
            }, 3000);
        }
    },
    
    // 현재 상태 업데이트 (외부에서 호출 가능)
    updateStatus: function(isPublic) {
        const currentStatus = document.getElementById('currentStatus');
        const hiddenInput = document.getElementById('isPublicInput');
        
        if (currentStatus && hiddenInput) {
            hiddenInput.value = isPublic;
            currentStatus.textContent = isPublic === 'yes' ? '공개' : '비공개';
            
            // 해당 라디오 버튼도 체크
            const radioButton = document.querySelector(`input[name="isPublicRadio"][value="${isPublic}"]`);
            if (radioButton) {
                radioButton.checked = true;
            }
        }
    }
};

console.log('detail.js 로드 완료');