/**
 * 투표 페이지 JavaScript - 깔끔한 버전
 */

$(document).ready(function() {
    console.log('투표 페이지 로드 완료');
    
    // 전역 변수 초기화
    window.selectedOption = null;
    window.isVoting = false;
    
    // 확인 버튼 클릭 이벤트
    $(document).on('click', '#confirm-vote', function() {
        console.log('확인 버튼 클릭');
        if (!window.selectedOption) return;
        
        hidePopup();
        processVote();
    });
    
    // 취소 버튼 클릭 이벤트
    $(document).on('click', '#cancel-vote, #popup-close', function() {
        console.log('취소 버튼 클릭');
        cancelVote();
    });
    
    // 키보드 이벤트 (Y/N 키)
    $(document).on('keydown', function(e) {
        if ($('#win95-popup').is(':visible')) {
            if (e.key.toLowerCase() === 'y' || e.key === 'Enter') {
                $('#confirm-vote').click();
            } else if (e.key.toLowerCase() === 'n' || e.key === 'Escape') {
                $('#cancel-vote').click();
            }
        }
    });
});

/**
 * 사진 클릭 - 투표 확인 팝업 표시
 */
function selectOption(element) {
    console.log('사진 클릭됨');
    
    if (window.isVoting) {
        console.log('투표 처리 중 - 클릭 무시');
        return;
    }
    
    // 선택한 옵션 저장
    window.selectedOption = $(element);
    const optionText = $(element).find('.option-text').text();
    
    console.log('선택된 옵션:', optionText);
    
    // 팝업에 선택한 옵션 표시
    $('#selected-option-text').html(`<strong style="color: #000080;">${optionText}</strong>`);
    
    // 팝업 표시
    showPopup();
}

/**
 * 팝업 표시
 */
function showPopup() {
    console.log('투표 확인 팝업 표시');
    $('#win95-popup').css('display', 'flex').hide().fadeIn(300);
    $('#confirm-vote').focus();
}

/**
 * 팝업 숨김
 */
function hidePopup() {
    console.log('팝업 숨김');
    $('#win95-popup').fadeOut(200);
}

/**
 * 투표 취소 - 사진 선택 화면으로 돌아가기
 */
function cancelVote() {
    console.log('투표 취소');
    
    // 선택 초기화
    window.selectedOption = null;
    
    // 팝업 닫기
    hidePopup();
    
    // 화면 흔들기 효과
    $('.vote-container').addClass('shake');
    setTimeout(function() {
        $('.vote-container').removeClass('shake');
    }, 500);
}

/**
 * 투표 처리 - MySQL에 저장
 */
function processVote() {
    console.log('=== 투표 처리 시작 ===');
    
    if (!window.selectedOption) {
        showErrorMessage('선택된 옵션이 없습니다.');
        return;
    }
    
    // 중복 클릭 방지
    window.isVoting = true;
    
    // 선택한 옵션 정보 가져오기
    const memberId = window.selectedOption.data('member-id');
    const serialNumber = window.selectedOption.data('serial-number');
    const optionText = window.selectedOption.find('.option-text').text();
    
    console.log('투표 데이터:', {
        질문번호: serialNumber,
        선택된회원ID: memberId,
        선택된옵션: optionText
    });
    
    // 필수 데이터 검증
    if (!memberId || !serialNumber) {
        console.error('필수 데이터 누락');
        showErrorMessage('투표 데이터가 올바르지 않습니다.');
        window.isVoting = false;
        return;
    }
    
    // 로딩 표시
    showLoading();
    
    // 선택한 옵션 강조 효과
    window.selectedOption.css({
        'background-color': '#000080',
        'color': 'white',
        'border-color': '#ffffff #000080 #000080 #ffffff'
    });
    window.selectedOption.find('.option-text').css({
        'background-color': '#000080',
        'color': 'white'
    });
    
    // 서버에 투표 데이터 전송
    $.ajax({
        url: '/voting/submit',
        type: 'POST',
        data: {
            serialNumber: serialNumber,
            votedId: memberId
        },
        timeout: 15000,
        success: function(response) {
            console.log('서버 응답 성공:', response);
            handleVoteSuccess(response, optionText);
        },
        error: function(xhr, status, error) {
            console.error('서버 요청 실패:', {
                상태코드: xhr.status,
                응답내용: xhr.responseText,
                에러: error
            });
            handleVoteError(xhr, status, error);
        }
    });
}

/**
 * 투표 성공 처리
 */
function handleVoteSuccess(response, optionText) {
    console.log('=== 투표 성공 처리 ===');
    
    hideLoading();
    
    try {
        // 응답 데이터 처리
        if (typeof response === 'string') {
            response = JSON.parse(response);
        }
        
        if (response.success) {
            console.log('MySQL 저장 완료! 투표 ID:', response.voteId);
            
            // 성공 메시지 표시
            showSuccessMessage(`투표 완료! "${optionText}"에 투표하셨습니다.`);
            
            // 선택된 옵션에 성공 애니메이션
            window.selectedOption.addClass('vote-success');
            
            // 3초 후 다음 투표로 이동
            setTimeout(function() {
                moveToNextVote(response);
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
 * 다음 투표로 이동
 */
function moveToNextVote(response) {
    console.log('=== 다음 투표로 이동 ===');
    
    if (response.noMoreQuestions) {
        console.log('모든 투표 완료!');
        showSuccessMessage('모든 투표를 완료하셨습니다! 감사합니다.');
        
        setTimeout(function() {
            window.location.href = '/';
        }, 2000);
        
    } else {
        console.log('다음 질문으로 이동');
        
        // 페이드아웃 효과와 함께 이동
        $('.vote-container').fadeOut(500, function() {
            window.location.href = '/voting';
        });
    }
}

/**
 * 투표 오류 처리
 */
function handleVoteError(xhr, status, error) {
    console.log('=== 투표 오류 처리 ===');
    
    hideLoading();
    resetOptionStyle();
    
    // 오류 메시지 결정
    let errorMessage = '투표 처리 중 오류가 발생했습니다.';
    
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
        errorMessage = '로그인이 만료되었습니다. 다시 로그인해주세요.';
        setTimeout(function() {
            window.location.href = '/member/login';
        }, 2000);
    } else if (xhr.status === 500) {
        errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    } else if (status === 'timeout') {
        errorMessage = '요청 시간이 초과되었습니다. 네트워크를 확인해주세요.';
    }
    
    showErrorMessage(errorMessage);
    
    // 투표 상태 초기화
    window.isVoting = false;
}

/**
 * 선택 옵션 스타일 원복
 */
function resetOptionStyle() {
    if (window.selectedOption) {
        window.selectedOption.css({
            'background-color': '#c0c0c0',
            'color': 'black',
            'border-color': '#ffffff #808080 #808080 #ffffff'
        });
        window.selectedOption.find('.option-text').css({
            'background-color': '#ececec',
            'color': '#333'
        });
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
 * 성공 메시지 표시
 */
function showSuccessMessage(message) {
    const alertHtml = `
        <div class="alert alert-success" style="display: none;">
            <i class="fas fa-check-circle"></i>
            <span>${message}</span>
        </div>
    `;
    
    $('.vote-container').prepend(alertHtml);
    $('.alert').first().slideDown(300);
    
    setTimeout(function() {
        $('.alert').first().slideUp(300, function() {
            $(this).remove();
        });
    }, 5000);
}

/**
 * 오류 메시지 표시
 */
function showErrorMessage(message) {
    const alertHtml = `
        <div class="alert alert-error" style="display: none;">
            <i class="fas fa-times-circle"></i>
            <span>${message}</span>
        </div>
    `;
    
    $('.vote-container').prepend(alertHtml);
    $('.alert').first().slideDown(300);
    
    setTimeout(function() {
        $('.alert').first().slideUp(300, function() {
            $(this).remove();
        });
    }, 10000);
}

/**
 * 이미지 로딩 실패 처리
 */
function handleImageError(img) {
    console.warn('이미지 로드 실패:', img.src);
    
    const container = $(img).closest('.win95-pic-container');
    const errorDiv = $('<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #666; font-size: 14px; flex-direction: column;"><div style="font-size: 30px; margin-bottom: 10px;">📷</div><div>이미지 없음</div></div>');
    
    $(img).hide();
    container.append(errorDiv);
}