/**
 * 투표 시스템 JavaScript - 질문 원본 이미지 사용 + 자동 다음 질문
 */

// 전역 변수
let selectedOption = null;
let currentQuestion = null;
let isVoting = false;

$(document).ready(function() {
    console.log('=== 투표 시스템 초기화 ===');
    
    // 현재 질문 정보 추출
    const questionElement = document.querySelector('[data-serial-number]');
    const serialNumberInput = document.querySelector('#current-serial-number');
    
    if (questionElement || serialNumberInput) {
        currentQuestion = {
            serialNumber: questionElement ? questionElement.dataset.serialNumber : serialNumberInput.value,
            questionText: document.querySelector('.question-text') ? document.querySelector('.question-text').textContent : ''
        };
        console.log('현재 질문:', currentQuestion);
    }
    
    console.log('투표 시스템 준비 완료');
});

/**
 * 투표 시작 (질문이 없을 때)
 */
function startVoting() {
    console.log('=== 투표 시작 ===');
    
    // 로딩 효과와 함께 페이지 이동
    $('#startSection').fadeOut(300, function() {
        window.location.href = '/voting';
    });
}

/**
 * 옵션 선택 - 투표 확인 모달 표시 (수정된 부분)
 */
function selectOption(element) {
    console.log('=== 옵션 선택 ===');
    
    if (isVoting) {
        console.log('투표 처리 중 - 클릭 무시');
        return;
    }

    try {
        // 기존 선택 해제
        $('.picture-option').removeClass('selected');
        
        // 현재 선택 표시
        $(element).addClass('selected');
        
        // 선택한 옵션 저장 (수정된 부분: optionId 사용)
        selectedOption = {
            element: $(element),
            optionId: $(element).data('option-id'), // option1, option2, option3, option4 형태
            serialNumber: $(element).data('serial-number'),
            optionText: $(element).find('.option-text').text().trim(),
            pictureUrl: $(element).find('.option-image').attr('src')
        };
        
        console.log('선택된 옵션:', selectedOption);
        
        // 필수 데이터 검증
        if (!selectedOption.optionId || !selectedOption.serialNumber || !currentQuestion) {
            alert('투표 데이터가 올바르지 않습니다. 페이지를 새로고침 해주세요.');
            return;
        }

        // 모달에 정보 설정
        $('#modal-picture').attr('src', selectedOption.pictureUrl);
        $('#vote-confirm-message').html(
            `<strong style="color: #667eea;">${selectedOption.optionText}</strong><br>에 투표하시겠습니까?`
        );
        
        // 모달 표시
        $('#vote-modal').fadeIn(300);
        
    } catch (error) {
        console.error('옵션 선택 중 오류:', error);
        alert('옵션 선택 중 오류가 발생했습니다.');
    }
}

/**
 * 투표 확인
 */
function confirmVote() {
    console.log('=== 투표 확인 ===');
    
    if (!selectedOption || !currentQuestion || isVoting) {
        console.error('투표 데이터 부족 또는 중복 요청');
        return;
    }
    
    isVoting = true;
    $('#vote-modal').fadeOut(200);
    
    // 로딩 표시
    showLoading();
    
    // 선택한 옵션 강조 효과
    selectedOption.element.css({
        'background-color': '#667eea',
        'color': 'white'
    });
    
    // 투표 제출
    submitVote();
}

/**
 * 투표 취소
 */
function cancelVote() {
    console.log('=== 투표 취소 ===');
    
    $('#vote-modal').fadeOut(200);
    
    // 선택 해제
    $('.picture-option').removeClass('selected');
    selectedOption = null;
}

/**
 * 투표 제출 - MySQL에 저장 (수정된 부분)
 */
function submitVote() {
    console.log('=== 투표 제출 시작 ===');
    console.log('투표 데이터:', {
        질문번호: selectedOption.serialNumber,
        선택된옵션ID: selectedOption.optionId, // option1, option2, option3, option4
        선택된옵션텍스트: selectedOption.optionText
    });
    
    // CSRF 토큰 가져오기
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    const ajaxSettings = {
        url: '/voting/submit',
        type: 'POST',
        data: {
            serialNumber: selectedOption.serialNumber,
            votedId: selectedOption.optionId // optionId를 votedId로 전송
        },
        timeout: 15000
    };
    
    // CSRF 토큰 추가
    if (header && token) {
        ajaxSettings.beforeSend = function(xhr) {
            xhr.setRequestHeader(header, token);
        };
    }

    // 서버에 투표 데이터 전송
    $.ajax(ajaxSettings)
        .done(function(response) {
            console.log('=== 투표 성공 ===');
            console.log('서버 응답:', response);
            handleVoteSuccess(response);
        })
        .fail(function(xhr, status, error) {
            console.error('=== 투표 실패 ===');
            console.error('오류 정보:', {
                상태코드: xhr.status,
                응답내용: xhr.responseText,
                에러: error
            });
            handleVoteError(xhr, status, error);
        });
}

/**
 * 투표 성공 처리 (수정된 부분: 자동 다음 질문)
 */
function handleVoteSuccess(response) {
    hideLoading();
    
    try {
        // 응답 데이터 처리
        if (typeof response === 'string') {
            response = JSON.parse(response);
        }
        
        if (response.success) {
            console.log('✅ MySQL 저장 완료! 투표 ID:', response.voteId);
            
            // 성공 메시지 표시
            showMessage(`🎉 투표 완료! "${selectedOption.optionText}"에 투표하셨습니다.`, 'success');
            
            // 선택된 옵션에 성공 애니메이션
            selectedOption.element.addClass('vote-success');
            
            // 2초 후 다음 질문 자동 로드
            setTimeout(function() {
                if (response.hasNextQuestion) {
                    console.log('➡️ 다음 질문 로드 중...');
                    loadNextQuestion();
                } else if (response.noMoreQuestions) {
                    console.log('🏁 모든 투표 완료');
                    showCompletionMessage(response.completionMessage || '모든 투표를 완료하셨습니다! 감사합니다.');
                } else {
                    // 일반적인 경우 다음 질문 시도
                    loadNextQuestion();
                }
            }, 2000);
            
        } else {
            throw new Error(response.error || '알 수 없는 서버 오류');
        }
        
    } catch (e) {
        console.error('응답 처리 중 오류:', e);
        handleVoteError({status: 500, responseText: e.message}, 'parsererror', e.message);
    }
}

/**
 * 다음 질문 자동 로드 (새로 추가된 기능)
 */
function loadNextQuestion() {
    console.log('=== 다음 질문 로드 시작 ===');
    
    // 로딩 표시
    showMessage('다음 질문을 불러오는 중...', 'info');
    
    $.ajax({
        url: '/voting/next-question',
        type: 'GET',
        timeout: 10000
    })
    .done(function(response) {
        console.log('다음 질문 로드 성공:', response);
        
        if (response.success) {
            // 질문 화면 업데이트
            updateQuestionDisplay(response.question, response.questionOptions);
            hideMessage();
            isVoting = false;
            selectedOption = null;
        } else if (response.noMoreQuestions) {
            showCompletionMessage(response.message || '모든 질문에 투표를 완료했습니다!');
        } else {
            console.error('다음 질문 로드 실패:', response);
            showMessage('다음 질문을 불러올 수 없습니다. 새로고침해주세요.', 'error');
            setTimeout(() => window.location.reload(), 3000);
        }
    })
    .fail(function(xhr, status, error) {
        console.error('다음 질문 로드 실패:', error);
        showMessage('네트워크 오류가 발생했습니다. 새로고침해주세요.', 'error');
        setTimeout(() => window.location.reload(), 3000);
    });
}

/**
 * 질문 화면 업데이트 (새로 추가된 기능)
 */
function updateQuestionDisplay(question, questionOptions) {
    console.log('=== 질문 화면 업데이트 ===');
    console.log('새 질문:', question);
    console.log('새 옵션들:', questionOptions);
    
    try {
        // 질문 내용 업데이트
        $('#question-text').text(question.question);
        $('#question-serial-display').text('질문 번호: ' + question.serialNumber);
        $('#current-serial-number').val(question.serialNumber);
        
        // 현재 질문 정보 업데이트
        currentQuestion = {
            serialNumber: question.serialNumber,
            questionText: question.question
        };
        
        // 옵션 컨테이너 업데이트
        const optionsContainer = $('#options-container');
        optionsContainer.empty();
        
        // 새 옵션들 생성
        questionOptions.forEach((option, index) => {
            const optionHtml = `
                <div class="option-item">
                    <div class="picture-option" 
                         data-serial-number="${question.serialNumber}"
                         data-option-id="${option.optionId}"
                         onclick="selectOption(this)">
                        <div class="pic-container">
                            <img src="${option.imageUrl}" 
                                 alt="투표 옵션 ${index + 1}"
                                 class="option-image"
                                 onload="this.style.opacity='1';"
                                 onerror="handleImageError(this);"
                                 style="opacity: 0; transition: opacity 0.3s ease;">
                        </div>
                        <div class="option-text">옵션 ${index + 1}</div>
                    </div>
                </div>
            `;
            optionsContainer.append(optionHtml);
        });
        
        // 페이지 맨 위로 부드럽게 스크롤
        $('html, body').animate({ scrollTop: 0 }, 500);
        
        console.log('질문 화면 업데이트 완료');
        
    } catch (error) {
        console.error('질문 화면 업데이트 중 오류:', error);
        showMessage('화면 업데이트 중 오류가 발생했습니다.', 'error');
    }
}

/**
 * 투표 오류 처리
 */
function handleVoteError(xhr, status, error) {
    hideLoading();
    resetOptionStyle();
    
    let errorMessage = '투표 처리 중 오류가 발생했습니다.';
    
    // 오류 메시지 결정
    try {
        if (xhr.responseText) {
            const errorResponse = JSON.parse(xhr.responseText);
            if (errorResponse.error) {
                errorMessage = errorResponse.error;
            }
        }
    } catch (e) {
        console.log('오류 응답 JSON 파싱 실패');
    }
    
    // HTTP 상태별 처리
    if (xhr.status === 401) {
        errorMessage = '🔐 로그인이 필요합니다. 로그인 페이지로 이동합니다.';
        setTimeout(function() {
            window.location.href = '/member/login';
        }, 2000);
    } else if (xhr.status === 500) {
        errorMessage = '🔧 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    } else if (status === 'timeout') {
        errorMessage = '⏱️ 요청 시간이 초과되었습니다. 네트워크를 확인해주세요.';
    }
    
    showMessage(errorMessage, 'error');
    
    // 투표 상태 초기화
    isVoting = false;
    selectedOption = null;
}

/**
 * 완료 메시지 표시 (수정된 부분)
 */
function showCompletionMessage(message) {
    console.log('=== 완료 메시지 표시 ===');
    
    const completionHtml = `
        <div class="vote-complete-container" style="display: none;">
            <div class="complete-message">
                <div class="icon">
                    <i class="fas fa-check-circle"></i>
                </div>
                <h3>🎉 투표 완료!</h3>
                <p>${message}</p>
                <div class="btn-group">
                    <a href="/" class="btn btn-primary">홈으로 가기</a>
                    <button class="btn btn-secondary" onclick="window.location.reload()">새로고침</button>
                </div>
            </div>
        </div>
    `;
    
    // 기존 투표 컨텐츠 숨기기
    $('#vote-content').fadeOut(300, function() {
        // 완료 메시지 추가 및 표시
        $('.vote-container').append(completionHtml);
        $('.vote-complete-container').fadeIn(500);
    });
}

/**
 * 선택 옵션 스타일 원복
 */
function resetOptionStyle() {
    if (selectedOption && selectedOption.element) {
        selectedOption.element.css({
            'background-color': '',
            'color': ''
        }).removeClass('selected');
    }
}

/**
 * 로딩 표시/숨김
 */
function showLoading() {
    $('#loading-overlay').fadeIn(200);
}

function hideLoading() {
    $('#loading-overlay').fadeOut(200);
}

/**
 * 메시지 표시 (수정된 부분)
 */
function showMessage(text, type = 'success') {
    // 기존 메시지 제거
    hideMessage();
    
    const alertHtml = `
        <div class="alert alert-${type}" id="current-message" style="display: none; position: fixed; top: 20px; left: 50%; transform: translateX(-50%); z-index: 9999; min-width: 300px; text-align: center;">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'times-circle' : 'info-circle'}"></i>
            <span>${text}</span>
        </div>
    `;
    
    $('body').append(alertHtml);
    $('#current-message').slideDown(300);
    
    // 자동 제거 (success: 3초, error: 6초, info: 2초)
    let timeout = 3000;
    if (type === 'error') timeout = 6000;
    else if (type === 'info') timeout = 2000;
    
    setTimeout(hideMessage, timeout);
}

/**
 * 메시지 숨김
 */
function hideMessage() {
    $('#current-message').slideUp(300, function() {
        $(this).remove();
    });
}

/**
 * 이미지 로딩 실패 처리
 */
function handleImageError(img) {
    console.warn('이미지 로드 실패:', img.src);
    
    // 기본 이미지로 대체
    const defaultImageSvg = 'data:image/svg+xml;base64,' + btoa(`
        <svg width="200" height="200" xmlns="http://www.w3.org/2000/svg">
            <rect width="200" height="200" fill="#f5f5f5"/>
            <text x="50%" y="45%" dominant-baseline="middle" text-anchor="middle" 
                  font-family="arial" font-size="24px" fill="#999">📷</text>
            <text x="50%" y="65%" dominant-baseline="middle" text-anchor="middle" 
                  font-family="arial" font-size="14px" fill="#999">이미지 없음</text>
        </svg>
    `);
    
    $(img).attr('src', defaultImageSvg);
    $(img).css('opacity', '1');
}

/**
 * 모달 외부 클릭 시 닫기
 */
$(document).on('click', '.vote-modal', function(e) {
    if (e.target === this) {
        cancelVote();
    }
});

/**
 * 키보드 이벤트 처리
 */
$(document).on('keydown', function(e) {
    if ($('#vote-modal').is(':visible')) {
        if (e.key === 'Enter') {
            confirmVote();
        } else if (e.key === 'Escape') {
            cancelVote();
        }
    }
});

// 전역 함수 등록 (HTML onclick에서 접근 가능하도록)
window.startVoting = startVoting;
window.selectOption = selectOption;
window.confirmVote = confirmVote;
window.cancelVote = cancelVote;
window.handleImageError = handleImageError;

console.log('투표 시스템 JavaScript 로드 완료 - 질문 원본 이미지 + 자동 다음 질문 버전');