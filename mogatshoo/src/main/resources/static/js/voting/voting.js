/**
 * 투표 페이지 JavaScript
 * Windows 95 스타일 인터페이스 구현
 */

$(document).ready(function() {
    // 전역 변수
    let selectedOption = null;
    let isVoting = false;
    
    // 윈도우 95 시작 효과: 시작 화면을 보여주고 투표 컨텐츠는 숨김
    $('#start-screen').show();
    $('#vote-content').hide();
    $('#no-more-questions').hide();
    
    // 투표 시작 버튼 클릭 이벤트
    $('#start-voting').on('click', function() {
        // 윈도우 95 시작 효과 재생 (CSS 애니메이션)
        $('#start-screen').fadeOut(300, function() {
            // 시작 화면이 사라진 후 투표 컨텐츠 표시
            $('#vote-content').fadeIn(500);
            
            // 투표 진행률 설정 (임의의 값, 실제로는 서버에서 계산)
            // 전체 질문 중 몇 %를 완료했는지 표시
            updateProgress(30); // 예: 30% 진행
        });
    });
    
    // 사진 옵션 클릭 이벤트
    $('.picture-option').on('click', function() {
        if (isVoting) return; // 투표 처리 중이면 중복 클릭 방지
        
        // 선택한 옵션 저장
        selectedOption = $(this);
        const memberId = $(this).data('member-id');
        const optionText = $(this).find('.option-text').text();
        
        // 팝업에 선택한 옵션 표시
        $('#selected-option-text').text(`선택: ${optionText}`);
        
        // 팝업 표시
        $('#win95-popup').fadeIn(200);
        
        // 키보드 이벤트 리스너 추가 (Y/N 키로 응답)
        $(document).on('keydown.voteConfirm', function(e) {
            if (e.key.toLowerCase() === 'y') {
                $('#confirm-vote').click();
            } else if (e.key.toLowerCase() === 'n') {
                $('#cancel-vote').click();
            }
        });
    });
    
    // 투표 확인 버튼 클릭
    $('#confirm-vote').on('click', function() {
        if (!selectedOption) return;
        
        // 키보드 이벤트 리스너 제거
        $(document).off('keydown.voteConfirm');
        
        // 팝업 닫기
        $('#win95-popup').fadeOut(200);
        
        // 투표 처리 시작
        processVote();
    });
    
    // 투표 취소 버튼 클릭
    $('#cancel-vote, #popup-close').on('click', function() {
        // 키보드 이벤트 리스너 제거
        $(document).off('keydown.voteConfirm');
        
        // 선택 초기화
        selectedOption = null;
        
        // 팝업 닫기
        $('#win95-popup').fadeOut(200);
    });
    
    // 투표 처리 함수
    function processVote() {
        if (!selectedOption) return;
        
        // 중복 클릭 방지
        isVoting = true;
        
        // 선택한 옵션의 memberId 가져오기
        const memberId = selectedOption.data('member-id');
        
        // 폼에 선택한 값 설정
        $('#voted-id').val(memberId);
        
        // 로딩 표시
        $('#loading-overlay').fadeIn(200);
        
        // 폼 액션 URL 가져오기
        const formAction = $('#vote-form').attr('action');
        
        // AJAX로 투표 제출 - 폼 액션에 지정된 URL 사용
        $.ajax({
            url: formAction,  // 폼에 지정된 액션 URL 사용
            type: 'POST',
            data: $('#vote-form').serialize(),
            success: function(response) {
                console.log('투표 성공:', response);
                
                // 로딩 숨기기
                $('#loading-overlay').fadeOut(200);
                
                // Windows 95 스타일 효과: 선택한 옵션 강조 표시 (잠시 동안)
                selectedOption.css('background-color', '#000080');
                selectedOption.find('.option-text').css({
                    'background-color': '#000080',
                    'color': 'white'
                });
                
                // 잠시 후 다음 질문으로 이동 또는 완료 화면 표시
                setTimeout(function() {
                    if (response && response.noMoreQuestions) {
                        // 모든 질문에 투표했으면 완료 화면 표시
                        $('#vote-content').fadeOut(400, function() {
                            $('#no-more-questions').fadeIn(400);
                        });
                    } else {
                        // 다음 질문으로 페이지 새로고침
                        window.location.href = '/voting';
                    }
                }, 1000);
            },
            error: function(xhr, status, error) {
                console.error('투표 오류:', error);
                console.error('상태 코드:', xhr.status);
                console.error('응답 텍스트:', xhr.responseText);
                
                // 로딩 숨기기
                $('#loading-overlay').fadeOut(200);
                
                // 오류 메시지 표시 (Windows 95 스타일 경고창)
                alert('투표 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
                
                // 상태 초기화
                isVoting = false;
            }
        });
    }
    
    // 진행률 업데이트 함수
    function updateProgress(percent) {
        $('#vote-progress').css('width', percent + '%');
        $('#progress-text').text(`${percent}% 완료`);
    }
    
    // 페이지 로드 시 질문이 없는 경우 처리
    if ($('#vote-content').length === 0 || $('#vote-content').children().length === 0) {
        $('#start-screen').hide();
        $('#no-more-questions').show();
    }
    
    // 윈도우 95 스타일 드래그 효과 (선택 사항)
    $('.win95-window').each(function() {
        makeDraggable($(this));
    });
    
    // 윈도우 드래그 가능하게 만드는 함수
    function makeDraggable(element) {
        let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
        
        if (element.find('.win95-titlebar').length > 0) {
            // 제목 표시줄이 있으면 드래그 핸들로 사용
            element.find('.win95-titlebar').on('mousedown', dragMouseDown);
        } else {
            // 없으면 전체 엘리먼트를 드래그 핸들로 사용
            element.on('mousedown', dragMouseDown);
        }
        
        function dragMouseDown(e) {
            e.preventDefault();
            // 마우스 위치 가져오기
            pos3 = e.clientX;
            pos4 = e.clientY;
            
            // 드래그 이벤트 리스너 추가
            $(document).on('mousemove', elementDrag);
            $(document).on('mouseup', closeDragElement);
        }
        
        function elementDrag(e) {
            e.preventDefault();
            
            // 새 마우스 위치 계산
            pos1 = pos3 - e.clientX;
            pos2 = pos4 - e.clientY;
            pos3 = e.clientX;
            pos4 = e.clientY;
            
            // 엘리먼트 위치 설정
            const top = element.offset().top - pos2;
            const left = element.offset().left - pos1;
            
            // 팝업 창만 드래그 가능하도록 제한
            if (element.hasClass('win95-popup') || element.closest('.win95-popup').length > 0) {
                element.css({
                    top: top + 'px',
                    left: left + 'px',
                    position: 'absolute'
                });
            }
        }
        
        function closeDragElement() {
            // 드래그 이벤트 리스너 제거
            $(document).off('mousemove', elementDrag);
            $(document).off('mouseup', closeDragElement);
        }
    }
});