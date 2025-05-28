/**
 * 질문 생성 페이지 기능
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('질문 생성 페이지 초기화 시작');
    
    // 글자 수 카운터
    const setupCharCounter = () => {
        const questionTextarea = document.getElementById('questionText');
        const charCountEl = document.getElementById('questionCharCount');
        
        if (!questionTextarea || !charCountEl) {
            console.warn('글자 수 카운터 요소를 찾을 수 없습니다.');
            return;
        }
        
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
                console.log('글자 수 제한 (500자) 적용됨');
            } else {
                charCountEl.classList.remove('error');
            }
        });
        
        console.log('글자 수 카운터 설정 완료');
    };
    
    // 취소 버튼 이벤트
    const setupCancelButton = () => {
        const cancelBtn = document.getElementById('cancelBtn');
        
        if (!cancelBtn) {
            console.warn('취소 버튼을 찾을 수 없습니다.');
            return;
        }
        
        cancelBtn.addEventListener('click', (e) => {
            // 변경 사항이 있는지 확인
            const form = document.getElementById('questionForm');
            const questionText = document.getElementById('questionText');
            
            // 질문 내용이 입력되었는지만 확인 (옵션은 자동으로 설정되므로 제외)
            const hasChanges = questionText && questionText.value.trim() !== '';
            
            if (hasChanges) {
                if (!confirm('변경 사항이 저장되지 않습니다. 정말 취소하시겠습니까?')) {
                    e.preventDefault();
                    console.log('사용자가 취소를 선택');
                    return false;
                }
            }
            
            console.log('질문 목록으로 이동');
        });
        
        console.log('취소 버튼 이벤트 설정 완료');
    };
    
    // 폼 유효성 검사
    const setupFormValidation = () => {
        const form = document.getElementById('questionForm');
        
        if (!form) {
            console.error('폼을 찾을 수 없습니다.');
            return;
        }
        
        form.addEventListener('submit', (e) => {
            console.log('폼 제출 시도');
            let isValid = true;
            
            // 기존 에러 메시지 제거
            const existingErrors = form.querySelectorAll('.error-message');
            existingErrors.forEach(error => error.remove());
            
            // 기존 에러 클래스 제거
            const errorFields = form.querySelectorAll('.error');
            errorFields.forEach(field => field.classList.remove('error'));
            
            // 질문 내용 검증
            const questionText = document.getElementById('questionText');
            if (!questionText || !questionText.value.trim()) {
                isValid = false;
                if (questionText) {
                    questionText.classList.add('error');
                    showFieldError(questionText, '질문 내용을 입력해주세요.');
                }
                console.log('질문 내용 유효성 검사 실패');
            }
            
            // 옵션 1, 2 검증 (필수)
            const option1 = document.getElementById('option1');
            const option2 = document.getElementById('option2');
            
            if (!option1 || !option1.value.trim()) {
                isValid = false;
                if (option1) {
                    option1.classList.add('error');
                    showFieldError(option1, '보기 1이 설정되지 않았습니다.');
                }
                console.log('보기 1 유효성 검사 실패');
            }
            
            if (!option2 || !option2.value.trim()) {
                isValid = false;
                if (option2) {
                    option2.classList.add('error');
                    showFieldError(option2, '보기 2가 설정되지 않았습니다.');
                }
                console.log('보기 2 유효성 검사 실패');
            }
            
            // 폼 데이터 로그 (디버깅용)
            const formData = new FormData(form);
            console.log('=== 폼 제출 데이터 ===');
            for (let [key, value] of formData.entries()) {
                console.log(`${key}: ${value}`);
            }
            
            if (!isValid) {
                e.preventDefault();
                console.log('폼 유효성 검사 실패 - 제출 중단');
                
                // 첫 번째 에러 필드로 스크롤
                const firstError = form.querySelector('.error');
                if (firstError) {
                    firstError.focus();
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
                
                return false;
            }
            
            console.log('폼 유효성 검사 통과 - 제출 진행');
            return true;
        });
        
        console.log('폼 유효성 검사 설정 완료');
    };
    
    // 필드 에러 메시지 표시 함수
    const showFieldError = (field, message) => {
        const parent = field.closest('.form-group');
        if (!parent) return;
        
        // 기존 에러 메시지 제거
        const existingError = parent.querySelector('.error-message');
        if (existingError) {
            existingError.remove();
        }
        
        // 새 에러 메시지 생성
        const errorMsg = document.createElement('div');
        errorMsg.className = 'error-message';
        errorMsg.textContent = message;
        errorMsg.style.color = 'red';
        errorMsg.style.fontSize = '12px';
        errorMsg.style.marginTop = '5px';
        
        parent.appendChild(errorMsg);
    };
    
    // 이미지 로드 에러 처리
    const setupImageErrorHandling = () => {
        const images = document.querySelectorAll('.option-image');
        
        images.forEach((img, index) => {
            img.addEventListener('load', function() {
                console.log(`이미지 ${index + 1} 로드 성공:`, this.src);
            });
            
            img.addEventListener('error', function() {
                console.log(`이미지 ${index + 1} 로드 실패:`, this.src);
                this.style.display = 'none';
                
                // 에러 메시지 표시
                const errorDiv = this.nextElementSibling;
                if (errorDiv) {
                    errorDiv.style.display = 'block';
                }
            });
        });
        
        console.log('이미지 에러 처리 설정 완료');
    };
    
    // 키보드 단축키
    const setupKeyboardShortcuts = () => {
        document.addEventListener('keydown', function(e) {
            // Ctrl+S: 폼 저장
            if (e.ctrlKey && e.key === 's') {
                e.preventDefault();
                const form = document.getElementById('questionForm');
                if (form) {
                    console.log('Ctrl+S 단축키로 폼 제출');
                    form.submit();
                }
            }
            
            // ESC: 취소
            if (e.key === 'Escape') {
                const cancelBtn = document.getElementById('cancelBtn');
                if (cancelBtn) {
                    console.log('ESC 키로 취소');
                    cancelBtn.click();
                }
            }
        });
        
        console.log('키보드 단축키 설정 완료');
    };
    
    // 페이지 로드 시 실행되는 초기화 함수
    const initCreatePage = () => {
        setupCharCounter();
        setupCancelButton();
        setupFormValidation();
        setupImageErrorHandling();
        setupKeyboardShortcuts();
        
        console.log('질문 생성 페이지 초기화 완료');
    };
    
    initCreatePage();
});