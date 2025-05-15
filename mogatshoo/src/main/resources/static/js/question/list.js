/**
 * 질문 목록 페이지 기능
 */
document.addEventListener('DOMContentLoaded', function() {
    // 검색 기능
    const setupSearch = () => {
        const searchInput = document.getElementById('searchInput');
        const searchBtn = document.getElementById('searchBtn');
        const questionsTable = document.getElementById('questionsTable');
        
        if (!searchInput || !searchBtn || !questionsTable) return;
        
        const rows = questionsTable.querySelectorAll('tbody tr');
        
        const performSearch = () => {
            const searchTerm = searchInput.value.toLowerCase().trim();
            let hasResults = false;
            
            rows.forEach(row => {
                const serialNumber = row.querySelector('.serial-number').textContent.toLowerCase();
                const questionText = row.querySelector('.question-title').textContent.toLowerCase();
                const statusText = row.querySelector('.status').textContent.toLowerCase();
                
                if (serialNumber.includes(searchTerm) || 
                    questionText.includes(searchTerm) || 
                    statusText.includes(searchTerm) || 
                    searchTerm === '') {
                    row.style.display = '';
                    hasResults = true;
                } else {
                    row.style.display = 'none';
                }
            });
            
            // 검색 결과가 없을 때 메시지 표시 로직
            const tbody = questionsTable.querySelector('tbody');
            const noResultsRow = document.getElementById('noResultsRow');
            
            if (!hasResults && searchTerm !== '') {
                if (!noResultsRow) {
                    const newRow = document.createElement('tr');
                    newRow.id = 'noResultsRow';
                    newRow.innerHTML = `
                        <td colspan="5" class="empty-search">
                            <div class="empty-state">
                                <i class="fas fa-search fa-3x"></i>
                                <p>"${searchTerm}"에 대한 검색 결과가 없습니다.</p>
                                <button class="btn btn-secondary" id="clearSearchBtn">
                                    <i class="fas fa-times"></i> 검색 지우기
                                </button>
                            </div>
                        </td>
                    `;
                    tbody.appendChild(newRow);
                    
                    // 검색 지우기 버튼 이벤트 연결
                    document.getElementById('clearSearchBtn').addEventListener('click', () => {
                        searchInput.value = '';
                        performSearch();
                    });
                } else {
                    noResultsRow.style.display = '';
                    noResultsRow.querySelector('p').textContent = `"${searchTerm}"에 대한 검색 결과가 없습니다.`;
                }
            } else if (noResultsRow) {
                noResultsRow.style.display = 'none';
            }
        };
        
        // 검색 버튼 클릭 이벤트
        searchBtn.addEventListener('click', performSearch);
        
        // 엔터 키 이벤트
        searchInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
        
        // 검색 입력 필드 변경 감지 (실시간 검색)
        searchInput.addEventListener('input', performSearch);
    };
    
    // URL 파라미터 확인해서 필요시 알림 표시
    const checkUrlParams = () => {
        const urlParams = new URLSearchParams(window.location.search);
        const status = urlParams.get('status');
        const message = urlParams.get('message');
        
        if (status && message) {
            const decodedMessage = decodeURIComponent(message);
            showNotification(decodedMessage, status);
            
            // 상태 변경 감지 (공개/비공개 전환)
            if (decodedMessage.includes('공개 상태로 변경') || decodedMessage.includes('비공개 상태로 변경')) {
                const match = decodedMessage.match(/Q\d+/);
                if (match) {
                    const serialNumber = match[0];
                    highlightChangedRow(serialNumber);
                }
            }
        }
    };
    
    // 알림 메시지 표시
    const showNotification = (message, type = 'info') => {
        const container = document.getElementById('notificationContainer');
        if (!container) return;
        
        // 새 알림 요소 생성
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
                <div class="notification-message">${message}</div>
                <button class="notification-close">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
        
        // 알림 닫기 버튼 이벤트
        const closeBtn = notification.querySelector('.notification-close');
        closeBtn.addEventListener('click', () => {
            notification.classList.add('hide');
            setTimeout(() => {
                notification.remove();
            }, 300);
        });
        
        // 문서에 알림 추가
        container.appendChild(notification);
        
        // 애니메이션 효과를 위한 지연
        setTimeout(() => {
            notification.classList.add('show');
        }, 10);
        
        // 자동으로 5초 후 사라짐
        setTimeout(() => {
            notification.classList.add('hide');
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 5000);
    };
    
    // 상태가 변경된 행을 하이라이트
    const highlightChangedRow = (serialNumber) => {
        const row = document.getElementById(`row-${serialNumber}`);
        if (!row) return;
        
        // 행에 하이라이트 효과 추가
        row.classList.add('highlight');
        
        // 상태 표시에 애니메이션 효과 추가
        const statusElement = document.getElementById(`status-${serialNumber}`);
        if (statusElement) {
            statusElement.classList.add('changing');
            
            // 3초 후 애니메이션 효과 제거
            setTimeout(() => {
                statusElement.classList.remove('changing');
            }, 3000);
        }
        
        // 화면에 해당 행이 보이도록 스크롤
        row.scrollIntoView({ behavior: 'smooth', block: 'center' });
    };
    
    // URL 파라미터를 가져오는 헬퍼 함수
    const getUrlParameter = (name) => {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
    };
    
    // 페이지 로드 시 실행되는 초기화 함수
    const initListPage = () => {
        setupSearch();
        checkUrlParams();
    };
    
    initListPage();
});