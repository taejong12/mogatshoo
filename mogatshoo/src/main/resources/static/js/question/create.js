/**
 * 질문 생성 페이지 기능
 */
document.addEventListener('DOMContentLoaded', function() {
    // 글자 수 카운터
    const setupCharCounter = () => {
        const questionTextarea = document.getElementById('question');
        const charCountEl = document.getElementById('questionCharCount');
        
        if (!questionTextarea || !charCountEl) return;
        
        // 초기 글자 수 설정
        charCountEl.textContent = questionTextarea.value.length;
        
        // 입력 이벤트 리스너
        questionTextarea.addEventListener('input', () => {
            const count = questionTextarea.value.length;
            charCountEl.textContent = count;
            
            // 글자 수 제한 (500자)
            if (count > 500) {
                questionTextarea.value = questionTextarea.value.substring(0, 500);
                charCountEl.textContent = 500;
                charCountEl.classList.add('error');
            } else {
                charCountEl.classList.remove('error');
            }
        });
    };
    
    // 취소 버튼 이벤트
    const setupCancelButton = () => {
        const cancelBtn = document.getElementById('cancelBtn');
        
        if (!cancelBtn) return;
        
        cancelBtn.addEventListener('click', (e) => {
            // 변경 사항이 있는지 확인
            const form = document.getElementById('questionForm');
            const hasChanges = Array.from(form.elements).some(el => {
                if (el.type === 'textarea' || el.type === 'text') {
                    return el.value.trim() !== '';
                }
                return false;
            });
            
            if (hasChanges) {
                if (!confirm('변경 사항이 저장되지 않습니다. 정말 취소하시겠습니까?')) {
                    e.preventDefault(); // 사용자가 취소를 선택하면 링크 이동 방지
                }
                // 확인을 선택하면 링크가 정상적으로 동작하여 목록 페이지로 이동
            }
            // 변경 사항이 없으면 링크가 정상적으로 동작하여 목록 페이지로 이동
        });
    };
    
    // 폼 유효성 검사
    const setupFormValidation = () => {
        const form = document.getElementById('questionForm');
        
        if (!form) return;
        
        form.addEventListener('submit', (e) => {
            let isValid = true;
            const requiredFields = form.querySelectorAll('[required]');
            
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.classList.add('error');
                    
                    // 부모 요소에 에러 메시지 추가
                    const parent = field.closest('.form-group');
                    let errorMsg = parent.querySelector('.error-message');
                    
                    if (!errorMsg) {
                        errorMsg = document.createElement('div');
                        errorMsg.className = 'error-message';
                        errorMsg.textContent = '이 필드는 필수입니다.';
                        parent.appendChild(errorMsg);
                    }
                } else {
                    field.classList.remove('error');
                    
                    // 에러 메시지 제거
                    const parent = field.closest('.form-group');
                    const errorMsg = parent.querySelector('.error-message');
                    if (errorMsg) {
                        errorMsg.remove();
                    }
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                // 첫 번째 에러 필드로 스크롤
                const firstError = form.querySelector('.error');
                if (firstError) {
                    firstError.focus();
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    };
    
    // 페이지 로드 시 실행되는 초기화 함수
    const initCreatePage = () => {
        setupCharCounter();
        setupCancelButton();
        setupFormValidation();
    };
    
    initCreatePage();
});