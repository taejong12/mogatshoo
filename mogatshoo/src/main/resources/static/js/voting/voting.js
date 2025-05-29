/**
 * 투표 시스템 JavaScript - MySQL 완전 연동 버전
 */

// 전역 변수
let selectedOption = null;
let currentQuestion = null;
let isVoting = false;

$(document).ready(function() {
    console.log('=== 투표 시스템 초기화 ===');
    
    // 현재 질문 정보 추출
    const questionElement = document.querySelector('[data-serial-number]');
    if (questionElement) {
        currentQuestion = {
            serialNumber: questionElement.dataset.serialNumber,
            questionText: document.querySelector('.question-text').textContent
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
 * 사진 선택 - 투표 확인 모달 표시
 */
function selectPicture(element) {
    console.log('=== 사진 선택 ===');
    
    if (isVoting) {
        console.log('투표 처리 중 - 클릭 무시');
        return;
    }

    try {
        // 기존 선택 해제
        $('.picture-option').removeClass('selected');
        
        // 현재 선택 표시
        $(element).addClass('selected');
        
        // 선택한 옵션 저장
        selectedOption = {
            element: $(element),
            memberId: $(element).data('member-id'),
            serialNumber: $(element).data('serial-number'),
            optionIndex: $(element).data('option-index'),
            optionText: $(element).find('.option-text').text().trim(),
            pictureUrl: $(element).find('.option-image').attr('src')
        };
        
        console.log('선택된 옵션:', selectedOption);
        
        // 필수 데이터 검증
        if (!selectedOption.memberId || !selectedOption.serialNumber || !currentQuestion) {
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
        console.error('사진 선택 중 오류:', error);
        alert('사진 선택 중 오류가 발생했습니다.');
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
 * 투표 제출 - MySQL에 저장
 */
function submitVote() {
    console.log('=== 투표 제출 시작 ===');
    console.log('투표 데이터:', {
        질문번호: selectedOption.serialNumber,
        선택된회원ID: selectedOption.memberId,
        선택된옵션: selectedOption.optionText
    });
    
    // CSRF 토큰 가져오기
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    const ajaxSettings = {
        url: '/voting/submit',
        type: 'POST',
        data: {
            serialNumber: selectedOption.serialNumber,
            votedId: selectedOption.memberId
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
 * 투표 성공 처리
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
            
            // 3초 후 다음 동작
            setTimeout(function() {
                if (response.noMoreQuestions) {
                    console.log('🏁 모든 투표 완료');
                    showMessage('모든 투표를 완료하셨습니다! 감사합니다.', 'success');
                    setTimeout(function() {
                        window.location.href = '/voting';
                    }, 2000);
                } else {
                    console.log('➡️ 다음 질문으로 이동');
                    window.location.href = '/voting';
                }
            }, 3000);
            
        } else {
            throw new Error(response.error || '알 수 없는 서버 오류');
        }
        
    } catch (e) {
        console.error('응답 처리 중 오류:', e);
        handleVoteError({status: 500, responseText: e.message}, 'parsererror', e.message);
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
 * 메시지 표시
 */
function showMessage(text, type = 'success') {
    const alertHtml = `
        <div class="alert alert-${type}" style="display: none;">
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'times-circle'}"></i>
            <span>${text}</span>
        </div>
    `;
    
    $('#message-container').append(alertHtml);
    $('#message-container .alert').last().slideDown(300);
    
    // 자동 제거
    const timeout = type === 'error' ? 8000 : 5000;
    setTimeout(function() {
        $('#message-container .alert').first().slideUp(300, function() {
            $(this).remove();
        });
    }, timeout);
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
window.selectPicture = selectPicture;
window.confirmVote = confirmVote;
window.cancelVote = cancelVote;
window.handleImageError = handleImageError;

console.log('🚀 투표 시스템 JavaScript 로드 완료');