// ===== 이메일 전송 관련 함수들을 맨 위에 전역으로 정의 =====

/**
 * 이메일 전송 모달 열기 - 전역 함수로 즉시 정의
 */
function openEmailModal(button) {
    console.log('openEmailModal 함수 호출됨');
    
    const serialNumber = button.getAttribute('data-serial-number');
    const winnerName = button.getAttribute('data-winner-name');
    
    console.log('이메일 모달 열기:', { serialNumber, winnerName });
    
    try {
        // 모달 요소들 확인
        const modalSerialNumberInput = document.getElementById('modalSerialNumber');
        const modalWinnerNameSpan = document.getElementById('modalWinnerName');
        const modalQuestionNumberSpan = document.getElementById('modalQuestionNumber');
        const emailSendForm = document.getElementById('emailSendForm');
        const emailModalElement = document.getElementById('emailModal');
        
        if (!modalSerialNumberInput) {
            console.error('modalSerialNumber 요소를 찾을 수 없습니다');
            alert('모달 요소를 찾을 수 없습니다. 페이지를 새로고침해주세요.');
            return;
        }
        
        if (!emailModalElement) {
            console.error('emailModal 요소를 찾을 수 없습니다');
            alert('이메일 모달을 찾을 수 없습니다. 페이지를 새로고침해주세요.');
            return;
        }
        
        // 모달 정보 설정
        modalSerialNumberInput.value = serialNumber;
        if (modalWinnerNameSpan) modalWinnerNameSpan.textContent = winnerName || '알 수 없음';
        if (modalQuestionNumberSpan) modalQuestionNumberSpan.textContent = serialNumber;
        
        // 폼 초기화
        if (emailSendForm) {
            emailSendForm.reset();
            modalSerialNumberInput.value = serialNumber; // reset 후 다시 설정
        }
        
        // Bootstrap 5 모달 표시
        if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            const emailModal = new bootstrap.Modal(emailModalElement);
            emailModal.show();
            console.log('모달 표시 완료');
        } else {
            console.error('Bootstrap이 로드되지 않았습니다');
            alert('Bootstrap이 로드되지 않았습니다. 페이지를 새로고침해주세요.');
        }
        
    } catch (error) {
        console.error('모달 열기 중 오류:', error);
        alert('모달을 열 수 없습니다. 페이지를 새로고침 해주세요.');
    }
}

/**
 * 모달에서 이메일 전송 - 전역 함수로 즉시 정의
 */
function sendEmailFromModal() {
    console.log('sendEmailFromModal 함수 호출됨');
    
    const form = document.getElementById('emailSendForm');
    const serialNumberInput = document.getElementById('modalSerialNumber');
    const winnerNameSpan = document.getElementById('modalWinnerName');
    
    if (!form || !serialNumberInput) {
        console.error('필수 요소들을 찾을 수 없습니다');
        showErrorNotification('전송 정보가 올바르지 않습니다.');
        return;
    }
    
    const serialNumber = serialNumberInput.value;
    const winnerName = winnerNameSpan ? winnerNameSpan.textContent : '';
    
    if (!serialNumber) {
        console.error('시리얼 넘버가 없습니다');
        showErrorNotification('질문 정보가 올바르지 않습니다.');
        return;
    }
    
    const formData = new FormData(form);
    console.log('이메일 전송 시작:', serialNumber);
    
    // 전송 버튼 비활성화
    const sendBtn = document.getElementById('sendEmailBtn');
    if (!sendBtn) {
        console.error('전송 버튼을 찾을 수 없습니다');
        return;
    }
    
    const originalBtnText = sendBtn.innerHTML;
    sendBtn.disabled = true;
    sendBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>전송 중...';
    
    // 진행 상황 모달 표시
    const sendingModalElement = document.getElementById('sendingModal');
    let sendingModal = null;
    
    if (sendingModalElement && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        sendingModal = new bootstrap.Modal(sendingModalElement);
        sendingModal.show();
    }
    
    // AJAX 요청으로 이메일 전송
    fetch('/admin/email/send-ajax', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        console.log('응답 상태:', response.status);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('이메일 전송 응답:', data);
        
        // 진행 상황 모달 숨기기
        if (sendingModal) {
            sendingModal.hide();
        }
        
        if (data.success) {
            // 성공 시 처리
            showSuccessNotification(data.message);
            
            // 모달 닫기
            const emailModalElement = document.getElementById('emailModal');
            if (emailModalElement && typeof bootstrap !== 'undefined') {
                const emailModal = bootstrap.Modal.getInstance(emailModalElement);
                if (emailModal) {
                    emailModal.hide();
                }
            }
            
            // 해당 행의 버튼 상태 업데이트
            updateEmailButtonStatus(serialNumber, true, winnerName);
            
        } else {
            // 실패 시 처리
            showErrorNotification(data.message || '이메일 전송에 실패했습니다.');
        }
    })
    .catch(error => {
        console.error('이메일 전송 오류:', error);
        if (sendingModal) {
            sendingModal.hide();
        }
        showErrorNotification('이메일 전송 중 네트워크 오류가 발생했습니다: ' + error.message);
    })
    .finally(() => {
        // 전송 버튼 복원
        if (sendBtn) {
            sendBtn.disabled = false;
            sendBtn.innerHTML = originalBtnText;
        }
    });
}

/**
 * 이메일 버튼 상태 업데이트 (전송 완료로 변경)
 */
function updateEmailButtonStatus(serialNumber, isSent, winnerName) {
    console.log('버튼 상태 업데이트:', { serialNumber, isSent });
    
    // 해당 행 찾기
    const rows = document.querySelectorAll('.voting-row');
    for (let row of rows) {
        const serialNumberCell = row.querySelector('.serial-number');
        if (!serialNumberCell) continue;
        
        const rowSerialNumber = serialNumberCell.textContent.trim();
        
        if (rowSerialNumber === serialNumber) {
            const actionsContainer = row.querySelector('.actions-container');
            const btnGroup = actionsContainer?.querySelector('.btn-group-vertical');
            
            if (isSent && btnGroup) {
                // 전송 완료 버튼으로 교체
                const newButtonHtml = `
                    <div>
                        <button class="btn btn-email-completed btn-sm" disabled title="이미 이메일이 전송되었습니다.">
                            <i class="fas fa-check-circle me-2"></i>전송완료
                        </button>
                        <small class="text-success d-block mt-1">
                            <i class="fas fa-info-circle me-1"></i>이메일 전송 완료
                        </small>
                    </div>
                `;
                
                // 기존 이메일 버튼 찾아서 교체
                const emailBtnContainer = btnGroup.querySelector('div:first-child');
                if (emailBtnContainer) {
                    emailBtnContainer.innerHTML = newButtonHtml;
                    console.log('버튼 상태 업데이트 완료');
                }
            }
            
            break;
        }
    }
}

/**
 * 성공 알림 표시
 */
function showSuccessNotification(message) {
    // 기존 알림 제거
    removeExistingNotifications();
    
    // 새 성공 알림 생성
    const notification = document.createElement('div');
    notification.className = 'alert alert-success alert-dismissible fade show';
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        min-width: 300px;
        max-width: 500px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    
    notification.innerHTML = `
        <i class="fas fa-check-circle me-2"></i>
        <strong>전송 완료!</strong> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    document.body.appendChild(notification);
    
    // 5초 후 자동 제거
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

/**
 * 오류 알림 표시
 */
function showErrorNotification(message) {
    // 기존 알림 제거
    removeExistingNotifications();
    
    // 새 오류 알림 생성
    const notification = document.createElement('div');
    notification.className = 'alert alert-danger alert-dismissible fade show';
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        min-width: 300px;
        max-width: 500px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    
    notification.innerHTML = `
        <i class="fas fa-exclamation-circle me-2"></i>
        <strong>전송 실패!</strong> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    document.body.appendChild(notification);
    
    // 10초 후 자동 제거
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 10000);
}

/**
 * 기존 알림 제거
 */
function removeExistingNotifications() {
    const existingAlerts = document.querySelectorAll('.alert[style*="position: fixed"]');
    existingAlerts.forEach(alert => alert.remove());
}

// ===== 페이지 크기 변경 =====
function changePageSize(newSize) {
    const url = new URL(window.location);
    url.searchParams.set('size', newSize);
    url.searchParams.set('page', '0'); // 첫 페이지로 이동
    window.location.href = url.toString();
}

// ===== 필터 초기화 =====
function resetFilters() {
    const statusFilter = document.getElementById('statusFilter');
    const rateFilter = document.getElementById('rateFilter');
    const emailFilter = document.getElementById('emailFilter');
    
    if (statusFilter) statusFilter.value = 'all';
    if (rateFilter) rateFilter.value = 'all';
    if (emailFilter) emailFilter.value = 'all';
    
    filterTable();
}

// ===== 업데이트된 테이블 필터링 =====
function filterTable() {
    const statusFilter = document.getElementById('statusFilter');
    const rateFilter = document.getElementById('rateFilter');
    const emailFilter = document.getElementById('emailFilter');
    
    if (!statusFilter || !rateFilter || !emailFilter) {
        console.error('필터 요소들을 찾을 수 없습니다');
        return;
    }
    
    const statusFilterValue = statusFilter.value;
    const rateFilterValue = rateFilter.value;
    const emailFilterValue = emailFilter.value;
    const rows = document.querySelectorAll('.voting-row');
    
    console.log('필터링 시작:', { statusFilterValue, rateFilterValue, emailFilterValue });
    console.log('총 행 수:', rows.length);
    
    let visibleCount = 0;
    
    rows.forEach((row, index) => {
        let showRow = true;
        
        // 상태 필터
        if (statusFilterValue !== 'all') {
            const rowStatus = row.getAttribute('data-voting-status');
            
            let statusMatch = false;
            switch (statusFilterValue) {
                case 'pending':
                    statusMatch = rowStatus === '보류';
                    break;
                case 'active':
                    statusMatch = rowStatus === '진행중';
                    break;
                case 'ended':
                    statusMatch = rowStatus === '종료';
                    break;
            }
            
            if (!statusMatch) {
                showRow = false;
            }
        }
        
        // 참여율 필터
        if (rateFilterValue !== 'all' && showRow) {
            const participationRate = parseFloat(row.getAttribute('data-participation-rate') || '0');
            
            if (rateFilterValue === 'high' && participationRate < 50) {
                showRow = false;
            } else if (rateFilterValue === 'medium' && (participationRate < 40 || participationRate >= 50)) {
                showRow = false;
            } else if (rateFilterValue === 'low' && participationRate >= 40) {
                showRow = false;
            }
        }
        
        // 이메일 필터
        if (emailFilterValue !== 'all' && showRow) {
            const emailEligible = row.getAttribute('data-email-eligible') === 'true';
            
            if (emailFilterValue === 'eligible' && !emailEligible) {
                showRow = false;
            } else if (emailFilterValue === 'not-eligible' && emailEligible) {
                showRow = false;
            }
        }
        
        // 행 표시/숨김 처리
        if (showRow) {
            row.style.display = '';
            visibleCount++;
        } else {
            row.style.display = 'none';
        }
    });
    
    console.log(`필터링 완료: ${visibleCount}개 행 표시`);
    
    // 결과가 없는 경우 메시지 표시
    const tableBody = document.querySelector('.voting-table tbody');
    let noResultsRow = document.getElementById('no-results-row');
    
    if (visibleCount === 0) {
        if (!noResultsRow && tableBody) {
            noResultsRow = document.createElement('tr');
            noResultsRow.id = 'no-results-row';
            noResultsRow.innerHTML = '<td colspan="8" class="text-center text-muted py-4">필터 조건에 맞는 결과가 없습니다.</td>';
            tableBody.appendChild(noResultsRow);
        }
    } else {
        if (noResultsRow) {
            noResultsRow.remove();
        }
    }
}

// ===== 로딩 표시/숨김 함수 =====
function showLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'flex';
    }
}

function hideLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'none';
    }
}

// ===== 통계 요약 정보 업데이트 함수 =====
function updateStatsSummary() {
    const rows = document.querySelectorAll('.voting-row');
    let totalEligible = 0;
    let totalEnded = 0;
    
    rows.forEach(row => {
        const status = row.getAttribute('data-voting-status');
        const emailEligible = row.getAttribute('data-email-eligible') === 'true';
        
        if (status === '종료') {
            totalEnded++;
            if (emailEligible) {
                totalEligible++;
            }
        }
    });
    
    console.log(`통계 요약: 종료된 질문 ${totalEnded}개 중 ${totalEligible}개가 이메일 발송 가능`);
}

// ===== DOMContentLoaded 이벤트 리스너 =====
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOMContentLoaded 이벤트 발생');
    console.log('이메일 기능 초기화 시작');
    
    // 함수 존재 여부 확인
    console.log('openEmailModal 함수:', typeof openEmailModal);
    console.log('sendEmailFromModal 함수:', typeof sendEmailFromModal);
    
    // Bootstrap 확인
    console.log('Bootstrap:', typeof bootstrap);
    
    // 필터 이벤트 리스너 등록
    const statusFilter = document.getElementById('statusFilter');
    const rateFilter = document.getElementById('rateFilter');
    const emailFilter = document.getElementById('emailFilter');

    if (statusFilter) statusFilter.addEventListener('change', filterTable);
    if (rateFilter) rateFilter.addEventListener('change', filterTable);
    if (emailFilter) emailFilter.addEventListener('change', filterTable);

    // 진행바 애니메이션
    setTimeout(() => {
        const progressFills = document.querySelectorAll('.progress-fill');
        progressFills.forEach(fill => {
            const width = fill.style.width;
            fill.style.width = '0%';
            setTimeout(() => {
                fill.style.width = width;
            }, 100);
        });
    }, 500);

    // 새로고침 폼에 현재 페이지 정보 추가
    const refreshForm = document.querySelector('form[action*="/refresh"]');
    if (refreshForm) {
        const currentUrl = new URL(window.location);
        const currentPage = currentUrl.searchParams.get('page') || '0';

        const pageInput = document.createElement('input');
        pageInput.type = 'hidden';
        pageInput.name = 'page';
        pageInput.value = currentPage;
        refreshForm.appendChild(pageInput);
    }
    
    // 모달 닫힐 때 폼 초기화
    const emailModal = document.getElementById('emailModal');
    if (emailModal) {
        emailModal.addEventListener('hidden.bs.modal', function() {
            const form = document.getElementById('emailSendForm');
            if (form) {
                form.reset();
            }
        });
        console.log('이메일 모달 이벤트 리스너 등록 완료');
    } else {
        console.error('emailModal 요소를 찾을 수 없습니다');
    }
    
    // 전송 완료된 버튼들에 툴팁 추가
    const completedButtons = document.querySelectorAll('.btn-email-completed');
    completedButtons.forEach(button => {
        button.setAttribute('title', '이미 이메일이 전송되었습니다.');
        if (window.bootstrap && bootstrap.Tooltip) {
            new bootstrap.Tooltip(button);
        }
    });
    
    // 이메일 전송 버튼 클릭 이벤트 확인
    const emailButtons = document.querySelectorAll('.email-send-btn');
    console.log('이메일 전송 버튼 개수:', emailButtons.length);
    
    emailButtons.forEach((button, index) => {
        console.log(`이메일 버튼 ${index + 1}:`, {
            serialNumber: button.getAttribute('data-serial-number'),
            winnerName: button.getAttribute('data-winner-name'),
            onclick: button.getAttribute('onclick')
        });
    });
    
    console.log('이메일 기능 초기화 완료');
});

// ===== 기타 이벤트 리스너들 =====

// 폼 제출시 로딩 표시
document.addEventListener('submit', function(e) {
    if (e.target.querySelector('button[type="submit"]')) {
        showLoading();
    }
});

// 페이지 로드 완료시 로딩 숨김 및 통계 업데이트
window.addEventListener('load', function() {
    hideLoading();
    updateStatsSummary();
});

// 에러 처리 및 사용자 경험 개선
window.addEventListener('error', function(e) {
    console.error('JavaScript 오류:', e.error);
    hideLoading();
});

// 브라우저 뒤로가기/앞으로가기 처리
window.addEventListener('popstate', function(e) {
    hideLoading();
});

// ===== 유틸리티 객체 =====
const VotingRateUtils = {
    // 이메일 발송 조건 체크
    checkEmailEligibility: function(participationRate, topVotedRate) {
        return participationRate >= 50.0 && topVotedRate >= 40.0;
    },
    
    // 참여율 등급 반환
    getParticipationGrade: function(rate) {
        if (rate >= 50) return 'high';
        if (rate >= 40) return 'medium';
        return 'low';
    },
    
    // 득표율 등급 반환
    getTopVotedGrade: function(rate) {
        if (rate >= 40) return 'high';
        if (rate >= 30) return 'medium';
        return 'low';
    },
    
    // 색상 반환
    getGradeColor: function(grade) {
        switch(grade) {
            case 'high': return '#10b981'; // 초록색
            case 'medium': return '#f59e0b'; // 주황색
            case 'low': return '#ef4444'; // 빨간색
            default: return '#6b7280'; // 회색
        }
    }
};

// ===== 추가 유틸리티 함수들 =====

// 이메일 전송 이력 페이지로 이동
function goToEmailHistory(serialNumber) {
    window.location.href = `/admin/email/history/${serialNumber}`;
}

// 이메일 전송 조건 상세 확인 (디버깅용)
function checkEmailEligibility(serialNumber) {
    fetch(`/admin/voting-status/email-eligibility/${serialNumber}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log('이메일 발송 조건 상세:', data.data);
                
                // 콘솔에 상세 정보 출력
                const details = data.data;
                console.table({
                    '질문번호': details.serialNumber,
                    '투표상태': details.votingStatus,
                    '투표종료여부': details.isVotingEnded ? '✅' : '❌',
                    '참여율': `${details.participationRate}%`,
                    '참여율충족': details.hasEnoughParticipation ? '✅' : '❌',
                    '득표율': `${details.topVotedRate}%`,
                    '득표율충족': details.hasEnoughTopVotedRate ? '✅' : '❌',
                    '전체조건': details.isOverallEligible ? '✅ 발송가능' : '❌ 발송불가'
                });
            }
        })
        .catch(error => {
            console.error('이메일 발송 조건 확인 오류:', error);
        });
}

// ===== 함수 디버깅 로그 =====
console.log('status.js 파일 로딩 완료');
console.log('전역 함수 등록 확인:', {
    openEmailModal: typeof openEmailModal,
    sendEmailFromModal: typeof sendEmailFromModal,
    updateEmailButtonStatus: typeof updateEmailButtonStatus,
    filterTable: typeof filterTable,
    resetFilters: typeof resetFilters,
    changePageSize: typeof changePageSize
});