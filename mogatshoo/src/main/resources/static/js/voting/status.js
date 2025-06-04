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

// 테이블 필터링 - 개선된 버전
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
        
        // 참여율 필터
        if (rateFilter !== 'all' && showRow) {
            const participationRate = parseFloat(row.getAttribute('data-participation-rate') || '0');
            console.log(`행 ${index}: 참여율 = ${participationRate}%`);
            
            if (rateFilter === 'high' && participationRate < 40) {
                showRow = false;
                console.log(`행 ${index}: 참여율 필터(high)로 숨김`);
            } else if (rateFilter === 'low' && participationRate >= 40) {
                showRow = false;
                console.log(`행 ${index}: 참여율 필터(low)로 숨김`);
            }
        }
        
        // 이메일 필터
        if (emailFilter !== 'all' && showRow) {
            const emailEligible = row.getAttribute('data-email-eligible') === 'true';
            console.log(`행 ${index}: 이메일 가능 = ${emailEligible}`);
            
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
            noResultsRow.innerHTML = '<td colspan="7" class="text-center text-muted py-4">필터 조건에 맞는 결과가 없습니다.</td>';
            tableBody.appendChild(noResultsRow);
        }
    } else {
        if (noResultsRow) {
            noResultsRow.remove();
        }
    }
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

// 페이지 로드 완료시 로딩 숨김
window.addEventListener('load', hideLoading);

// 에러 처리 및 사용자 경험 개선
window.addEventListener('error', function(e) {
    console.error('JavaScript 오류:', e.error);
    hideLoading();
});

// 브라우저 뒤로가기/앞으로가기 처리
window.addEventListener('popstate', function(e) {
    hideLoading();
});