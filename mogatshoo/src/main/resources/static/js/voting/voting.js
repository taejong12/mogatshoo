/**
 * 투표 페이지 JavaScript
 * Windows 95 스타일 인터페이스 구현
 */

$(document).ready(function() {
    // 전역 변수
    let selectedOption = null;
    let isVoting = false;
    
    // 페이지 로드 시 초기화
    initializePage();
    
    // 사진 옵션 클릭 이벤트
    $('.picture-option').on('click', function() {
        if (isVoting) return; // 투표 처리 중이면 중복 클릭 방지
        
        // 선택한 옵션 저장
        selectedOption = $(this);
        const memberId = $(this).data('member-id');
        const optionText = $(this).find('.option-text').text();
        
        // 팝업에 선택한 옵션 표시
        $('#selected-option-text').html(`<strong>${optionText}</strong>`);
        
        // 팝업 표시
        showPopup();
    });
    
    // 투표 확인 버튼 클릭
    $('#confirm-vote').on('click', function() {
        if (!selectedOption) return;
        
        // 키보드 이벤트 리스너 제거
        $(document).off('keydown.voteConfirm');
        
        // 팝업 닫기
        hidePopup();
        
        // 투표 처리 시작
        processVote();
    });
    
    // 투표 취소 버튼 클릭
    $('#cancel-vote, #popup-close').on('click', function() {
        cancelVote();
    });
    
    // 키보드 이벤트 (Y/N 키로 응답)
    $(document).on('keydown.voteConfirm', function(e) {
        if ($('#win95-popup').is(':visible')) {
            if (e.key.toLowerCase() === 'y' || e.key === 'Enter') {
                $('#confirm-vote').click();
            } else if (e.key.toLowerCase() === 'n' || e.key === 'Escape') {
                $('#cancel-vote').click();
            }
        }
    });
    
    // ESC 키로 팝업 닫기
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && $('#win95-popup').is(':visible')) {
            cancelVote();
        }
    });
    
    /**
     * 페이지 초기화
     */
    function initializePage() {
        // 알림 메시지 자동 숨김 (5초 후)
        setTimeout(function() {
            $('.alert').fadeOut(500);
        }, 5000);
        
        // 이미지 로딩 처리
        $('.option-image').each(function() {
            const img = $(this);
            const originalSrc = img.attr('src');
            
            // 이미지가 이미 로드 실패한 경우
            if (this.complete && this.naturalHeight === 0) {
                handleImageError(this);
            }
            
            // 이미지 경로 디버그 로그
            console.log('이미지 경로:', originalSrc);
        });
        
        // Windows 95 효과: 페이지 로드 애니메이션
        $('.win95-window').each(function(index) {
            $(this).css({
                'opacity': '0',
                'transform': 'translateY(20px)'
            }).delay(index * 100).animate({
                'opacity': '1'
            }, 300).css('transform', 'translateY(0px)');
        });
    }
    
    /**
     * 팝업 표시
     */
    function showPopup() {
        $('#win95-popup').fadeIn(200);
        $('#confirm-vote').focus(); // 확인 버튼에 포커스
        
        // 키보드 이벤트 리스너 추가
        $(document).on('keydown.voteConfirm', function(e) {
            if (e.key.toLowerCase() === 'y' || e.key === 'Enter') {
                $('#confirm-vote').click();
            } else if (e.key.toLowerCase() === 'n' || e.key === 'Escape') {
                $('#cancel-vote').click();
            }
        });
    }
    
    /**
     * 팝업 숨김
     */
    function hidePopup() {
        $('#win95-popup').fadeOut(200);
    }
    
    /**
     * 투표 취소
     */
    function cancelVote() {
        // 키보드 이벤트 리스너 제거
        $(document).off('keydown.voteConfirm');
        
        // 선택 초기화
        selectedOption = null;
        
        // 팝업 닫기
        hidePopup();
    }
    
    /**
     * 투표 처리 함수
     */
    function processVote() {
        if (!selectedOption) return;
        
        // 중복 클릭 방지
        isVoting = true;
        
        // 선택한 옵션의 memberId 가져오기
        const memberId = selectedOption.data('member-id');
        const serialNumber = selectedOption.data('serial-number');
        
        console.log('투표 처리 시작:', {
            memberId: memberId,
            serialNumber: serialNumber
        });
        
        // 폼에 선택한 값 설정
        $('#voted-id').val(memberId);
        
        // 로딩 표시
        showLoading();
        
        // Windows 95 스타일 효과: 선택한 옵션 강조 표시
        selectedOption.css({
            'background-color': '#000080',
            'color': 'white',
            'border-color': '#ffffff #000080 #000080 #ffffff'
        });
        selectedOption.find('.option-text').css({
            'background-color': '#000080',
            'color': 'white'
        });
        
        // AJAX로 투표 제출
        $.ajax({
            url: '/voting/submit',
            type: 'POST',
            data: {
                serialNumber: serialNumber,
                votedId: memberId
            },
            success: function(response) {
                console.log('투표 성공:', response);
                
                // 로딩 숨기기
                hideLoading();
                
                // 성공 메시지 표시
                showSuccessMessage('투표가 성공적으로 저장되었습니다!');
                
                // 2초 후 페이지 새로고침 (다음 질문으로)
                setTimeout(function() {
                    window.location.href = '/voting';
                }, 2000);
            },
            error: function(xhr, status, error) {
                console.error('투표 오류:', {
                    status: xhr.status,
                    statusText: xhr.statusText,
                    responseText: xhr.responseText,
                    error: error
                });
                
                // 로딩 숨기기
                hideLoading();
                
                // 선택 효과 원복
                resetOptionStyle();
                
                // 오류 메시지 표시
                let errorMessage = '투표 처리 중 오류가 발생했습니다.';
                if (xhr.status === 400) {
                    errorMessage = '잘못된 요청입니다. 다시 시도해주세요.';
                } else if (xhr.status === 401) {
                    errorMessage = '로그인이 필요합니다.';
                    setTimeout(function() {
                        window.location.href = '/member/login';
                    }, 2000);
                } else if (xhr.status === 500) {
                    errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                }
                
                showErrorMessage(errorMessage);
                
                // 투표 상태 초기화
                isVoting = false;
            }
        });
    }
    
    /**
     * 선택 옵션 스타일 원복
     */
    function resetOptionStyle() {
        if (selectedOption) {
            selectedOption.css({
                'background-color': '#c0c0c0',
                'color': 'black',
                'border-color': '#ffffff #808080 #808080 #ffffff'
            });
            selectedOption.find('.option-text').css({
                'background-color': '#ececec',
                'color': '#333'
            });
        }
    }
    
    /**
     * 로딩 표시
     */
    function showLoading() {
        $('#loading-overlay').fadeIn(200);
    }
    
    /**
     * 로딩 숨김
     */
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
        
        // 5초 후 자동 숨김
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
        
        // 10초 후 자동 숨김
        setTimeout(function() {
            $('.alert').first().slideUp(300, function() {
                $(this).remove();
            });
        }, 10000);
    }
    
    /**
     * Windows 95 스타일 드래그 효과 (팝업용)
     */
    function makeDraggable(element) {
        let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
        
        const titleBar = element.find('.win95-titlebar');
        if (titleBar.length > 0) {
            titleBar.on('mousedown', dragMouseDown);
        }
        
        function dragMouseDown(e) {
            e.preventDefault();
            pos3 = e.clientX;
            pos4 = e.clientY;
            
            $(document).on('mousemove', elementDrag);
            $(document).on('mouseup', closeDragElement);
        }
        
        function elementDrag(e) {
            e.preventDefault();
            
            pos1 = pos3 - e.clientX;
            pos2 = pos4 - e.clientY;
            pos3 = e.clientX;
            pos4 = e.clientY;
            
            const newTop = element.offset().top - pos2;
            const newLeft = element.offset().left - pos1;
            
            element.css({
                top: newTop + 'px',
                left: newLeft + 'px',
                position: 'absolute'
            });
        }
        
        function closeDragElement() {
            $(document).off('mousemove', elementDrag);
            $(document).off('mouseup', closeDragElement);
        }
    }
    
    // 팝업 윈도우를 드래그 가능하게 만들기
    makeDraggable($('#win95-popup .win95-window'));
    
    /**
     * 브라우저 호환성 확인
     */
    function checkBrowserCompatibility() {
        // jQuery 버전 확인
        if (typeof $ === 'undefined') {
            console.error('jQuery가 로드되지 않았습니다.');
            return false;
        }
        
        // 필수 기능 확인
        if (!window.FormData) {
            console.warn('이 브라우저는 일부 기능을 지원하지 않을 수 있습니다.');
        }
        
        return true;
    }
    
    // 브라우저 호환성 확인
    checkBrowserCompatibility();
    
    /**
     * 개발자 도구 감지 (선택사항)
     */
    function detectDevTools() {
        let devtools = {
            open: false,
            orientation: null
        };
        
        const threshold = 160;
        
        setInterval(function() {
            if (window.outerHeight - window.innerHeight > threshold || 
                window.outerWidth - window.innerWidth > threshold) {
                if (!devtools.open) {
                    devtools.open = true;
                    console.log('개발자 모드가 감지되었습니다.');
                }
            } else {
                devtools.open = false;
            }
        }, 500);
    }
    
    // 개발 환경에서만 개발자 도구 감지 실행
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        detectDevTools();
    }
});

/**
 * 이미지 로딩 실패 시 대체 처리 (전역 함수)
 */
function handleImageError(img) {
    console.warn('이미지 로드 실패:', img.src);
    
    // 이미지 요소를 감싸는 컨테이너 찾기
    const container = $(img).closest('.win95-pic-container');
    
    // 오류 표시 요소 생성
    const errorDiv = $('<div class="image-error"></div>');
    
    // 이미지 숨기고 오류 표시
    $(img).hide();
    container.append(errorDiv);
    
    // 페이드인 효과
    errorDiv.fadeIn(300);
}

/**
 * 페이지 언로드 시 정리 작업
 */
$(window).on('beforeunload', function() {
    // 진행 중인 투표가 있는지 확인
    if (window.isVoting) {
        return '투표가 진행 중입니다. 정말 페이지를 떠나시겠습니까?';
    }
});

/**
 * 페이지 가시성 변경 감지 (탭 변경 등)
 */
document.addEventListener('visibilitychange', function() {
    if (document.hidden) {
        console.log('페이지가 백그라운드로 이동했습니다.');
    } else {
        console.log('페이지가 다시 활성화되었습니다.');
    }
});

/**
 * 온라인/오프라인 상태 감지
 */
window.addEventListener('online', function() {
    console.log('인터넷 연결이 복구되었습니다.');
    $('.alert-error').fadeOut();
});

window.addEventListener('offline', function() {
    console.log('인터넷 연결이 끊어졌습니다.');
    const offlineAlert = `
        <div class="alert alert-warning">
            <i class="fas fa-wifi"></i>
            <span>인터넷 연결을 확인해주세요.</span>
        </div>
    `;
    $('.vote-container').prepend(offlineAlert);
});

/**
 * 터치 디바이스 지원
 */
if ('ontouchstart' in window) {
    // 터치 이벤트 추가
    $('.picture-option').on('touchstart', function() {
        $(this).addClass('touch-active');
    });
    
    $('.picture-option').on('touchend', function() {
        $(this).removeClass('touch-active');
    });
    
    // 터치용 CSS 클래스 추가
    $('body').addClass('touch-device');
}