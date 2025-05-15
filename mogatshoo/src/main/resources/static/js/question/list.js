/**
 * 질문 목록 페이지 기능
 */
document.addEventListener('DOMContentLoaded', function() {
    // 검색 기능
    const setupSearch = () => {
        const searchInput = document.getElementById('searchInput');
        const searchBtn = document.getElementById('searchBtn');
        const questionCards = document.querySelectorAll('.question-card');
        
        if (!searchInput || !searchBtn || !questionCards.length) return;
        
        const performSearch = () => {
            const searchTerm = searchInput.value.toLowerCase().trim();
            let hasResults = false;
            
            questionCards.forEach(card => {
                const questionTitle = card.querySelector('.question-title').textContent.toLowerCase();
                const options = card.querySelector('.option-preview').textContent.toLowerCase();
                const serialNumber = card.querySelector('.serial-number').textContent.toLowerCase();
                
                // 검색어가 질문 제목, 보기, 일련번호 중 하나에 포함되면 표시
                if (questionTitle.includes(searchTerm) || 
                    options.includes(searchTerm) || 
                    serialNumber.includes(searchTerm) || 
                    searchTerm === '') {
                    card.style.display = '';
                    hasResults = true;
                } else {
                    card.style.display = 'none';
                }
            });
            
            // 검색 결과가 없을 때 메시지 표시
            const emptyState = document.querySelector('.empty-state');
            const noResultsMsg = document.querySelector('.no-results-message');
            
            if (!hasResults && searchTerm !== '') {
                if (noResultsMsg) {
                    noResultsMsg.style.display = 'flex';
                } else {
                    const noResults = document.createElement('div');
                    noResults.className = 'empty-state no-results-message';
                    noResults.innerHTML = `
                        <i class="fas fa-search fa-4x"></i>
                        <p>"${searchTerm}"에 대한 검색 결과가 없습니다.</p>
                        <button class="btn btn-secondary" id="clearSearchBtn">
                            <i class="fas fa-times"></i> 검색 지우기
                        </button>
                    `;
                    document.querySelector('.card-container').appendChild(noResults);
                    
                    // 검색 지우기 버튼 이벤트 연결
                    document.getElementById('clearSearchBtn').addEventListener('click', () => {
                        searchInput.value = '';
                        performSearch();
                    });
                }
            } else {
                if (noResultsMsg) {
                    noResultsMsg.style.display = 'none';
                }
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
    
    // 질문 카드 호버 효과
    const setupCardHover = () => {
        const cards = document.querySelectorAll('.question-card');
        
        cards.forEach(card => {
            card.addEventListener('mouseover', () => {
                card.classList.add('hover');
            });
            
            card.addEventListener('mouseout', () => {
                card.classList.remove('hover');
            });
        });
    };
    
    // URL 파라미터 확인해서 필요시 알림 표시
    const checkUrlParams = () => {
        const status = getUrlParameter('status');
        const message = getUrlParameter('message');
        
        if (status && message) {
            const decodedMessage = decodeURIComponent(message);
            showNotification(decodedMessage, status);
        }
    };
    
    // 페이지 로드 시 실행되는 초기화 함수
    const initListPage = () => {
        setupSearch();
        setupCardHover();
        checkUrlParams();
    };
    
    initListPage();
});