// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
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
});

// 페이지 크기 변경
function changePageSize(newSize) {
    const url = new URL(window.location);
    url.searchParams.set('size', newSize);
    url.searchParams.set('page', '0'); // 첫 페이지로 이동
    window.location.href = url.toString();
}

// 필터 초기화
function resetFilters() {
    document.getElementById('statusFilter').value = 'all';
    document.getElementById('rateFilter').value = 'all';
    document.getElementById('emailFilter').value = 'all';
    filterTable();
}

// 업데이트된 테이블 필터링 - 득표율 조건 추가
function filterTable() {
    const statusFilter = document.getElementById('statusFilter').value;
    const rateFilter = document.getElementById('rateFilter').value;
    const emailFilter = document.getElementById('emailFilter').value;
    const rows = document.querySelectorAll('.voting-row');
    
    console.log('필터링 시작:', { statusFilter, rateFilter, emailFilter });
    console.log('총 행 수:', rows.length);
    
    let visibleCount = 0;
    
    rows.forEach((row, index) => {
        let showRow = true;
        
        // 상태 필터
        if (statusFilter !== 'all') {
            const rowStatus = row.getAttribute('data-voting-status');
            console.log(`행 ${index}: 상태 = "${rowStatus}", 필터 = "${statusFilter}"`);
            
            let statusMatch = false;
            switch (statusFilter) {
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
                console.log(`행 ${index}: 상태 필터로 숨김`);
            }
        }
        
        // 참여율 필터 (업데이트된 조건: 50% 기준)
        if (rateFilter !== 'all' && showRow) {
            const participationRate = parseFloat(row.getAttribute('data-participation-rate') || '0');
            console.log(`행 ${index}: 참여율 = ${participationRate}%`);
            
            if (rateFilter === 'high' && participationRate < 50) {
                showRow = false;
                console.log(`행 ${index}: 참여율 필터(high)로 숨김 - ${participationRate}% < 50%`);
            } else if (rateFilter === 'medium' && (participationRate < 40 || participationRate >= 50)) {
                showRow = false;
                console.log(`행 ${index}: 참여율 필터(medium)로 숨김 - ${participationRate}% not in [40%, 50%)`);
            } else if (rateFilter === 'low' && participationRate >= 40) {
                showRow = false;
                console.log(`행 ${index}: 참여율 필터(low)로 숨김 - ${participationRate}% >= 40%`);
            }
        }
        
        // 이메일 필터 (참여율 50% + 득표율 40% 조건)
        if (emailFilter !== 'all' && showRow) {
            const emailEligible = row.getAttribute('data-email-eligible') === 'true';
            const participationRate = parseFloat(row.getAttribute('data-participation-rate') || '0');
            const topVotedRate = parseFloat(row.getAttribute('data-top-voted-rate') || '0');
            
            console.log(`행 ${index}: 이메일 가능 = ${emailEligible}, 참여율 = ${participationRate}%, 득표율 = ${topVotedRate}%`);
            
            if (emailFilter === 'eligible' && !emailEligible) {
                showRow = false;
                console.log(`행 ${index}: 이메일 필터(eligible)로 숨김`);
            } else if (emailFilter === 'not-eligible' && emailEligible) {
                showRow = false;
                console.log(`행 ${index}: 이메일 필터(not-eligible)로 숨김`);
            }
        }
        
        // 행 표시/숨김 처리
        if (showRow) {
            row.style.display = '';
            visibleCount++;
            console.log(`행 ${index}: 표시`);
        } else {
            row.style.display = 'none';
            console.log(`행 ${index}: 숨김`);
        }
    });
    
    console.log(`필터링 완료: ${visibleCount}개 행 표시`);
    
    // 결과가 없는 경우 메시지 표시
    const tableBody = document.querySelector('.voting-table tbody');
    let noResultsRow = document.getElementById('no-results-row');
    
    if (visibleCount === 0) {
        if (!noResultsRow) {
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

// 득표율 정보를 툴팁으로 표시하는 함수
function showTopVotedRateTooltip(element) {
    const row = element.closest('.voting-row');
    if (row) {
        const topVotedRate = row.getAttribute('data-top-voted-rate');
        const participationRate = row.getAttribute('data-participation-rate');
        const emailEligible = row.getAttribute('data-email-eligible') === 'true';
        
        const tooltip = document.createElement('div');
        tooltip.className = 'custom-tooltip';
        tooltip.innerHTML = `
            <div><strong>이메일 발송 조건</strong></div>
            <div>참여율: ${participationRate}% ${participationRate >= 50 ? '✅' : '❌'} (≥50%)</div>
            <div>득표율: ${topVotedRate}% ${topVotedRate >= 40 ? '✅' : '❌'} (≥40%)</div>
            <div class="mt-1"><strong>${emailEligible ? '✅ 발송 가능' : '❌ 발송 불가'}</strong></div>
        `;
        
        // 툴팁 스타일 적용
        tooltip.style.cssText = `
            position: absolute;
            background: rgba(0,0,0,0.9);
            color: white;
            padding: 10px;
            border-radius: 8px;
            font-size: 12px;
            z-index: 1000;
            max-width: 200px;
            pointer-events: none;
        `;
        
        document.body.appendChild(tooltip);
        
        // 마우스 위치에 따라 툴팁 위치 조정
        element.addEventListener('mousemove', function(e) {
            tooltip.style.left = (e.pageX + 10) + 'px';
            tooltip.style.top = (e.pageY - 10) + 'px';
        });
        
        // 마우스가 떠나면 툴팁 제거
        element.addEventListener('mouseleave', function() {
            if (tooltip && tooltip.parentNode) {
                tooltip.parentNode.removeChild(tooltip);
            }
        });
    }
}

// 통계 요약 정보 업데이트 함수
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

// 폼 제출시 로딩 표시
document.addEventListener('submit', function(e) {
    if (e.target.querySelector('button[type="submit"]')) {
        showLoading();
    }
});

// 로딩 표시/숨김 함수
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

// 득표율 관련 유틸리티 함수들
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