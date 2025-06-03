// 필터링 기능
document.addEventListener('DOMContentLoaded', function() {
    const statusFilter = document.getElementById('statusFilter');
    const rateFilter = document.getElementById('rateFilter');

    if (statusFilter && rateFilter) {
        statusFilter.addEventListener('change', filterTable);
        rateFilter.addEventListener('change', filterTable);
    }

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

function filterTable() {
    const statusFilter = document.getElementById('statusFilter').value;
    const rateFilter = document.getElementById('rateFilter').value;
    const tbody = document.querySelector('.voting-table tbody');
    
    if (!tbody) return;
    
    const rows = tbody.querySelectorAll('tr');

    rows.forEach(row => {
        let showRow = true;
        const status = row.dataset.status;
        const rate = parseFloat(row.dataset.rate);

        // 상태 필터
        if (statusFilter !== 'all') {
            if (statusFilter === 'active' && status === 'yes') showRow = false;
            if (statusFilter === 'ended' && status === 'no') showRow = false;
        }

        // 투표율 필터
        if (rateFilter !== 'all') {
            if (rateFilter === 'high' && rate < 40) showRow = false;
            if (rateFilter === 'low' && rate >= 40) showRow = false;
        }

        row.style.display = showRow ? '' : 'none';
    });
}

// 페이지 크기 변경 함수
function changePageSize(size) {
    const currentUrl = new URL(window.location);
    currentUrl.searchParams.set('size', size);
    currentUrl.searchParams.set('page', '0'); // 첫 페이지로 이동
    window.location.href = currentUrl.toString();
}

// 폼 제출시 로딩 표시
document.addEventListener('submit', function(e) {
    if (e.target.querySelector('button[type="submit"]')) {
        showLoading();
    }
});

function showLoading() {
    document.getElementById('loadingOverlay').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

// 페이지 로드 완료시 로딩 숨김
window.addEventListener('load', hideLoading);