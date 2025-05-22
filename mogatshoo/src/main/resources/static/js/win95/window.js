// Windows 95 스타일 창 관리 스크립트
// static/js/win95/window.js

document.addEventListener('DOMContentLoaded', function() {
    // 윈도우 관리자 객체
    const Win95WindowManager = {
        windows: [],
        zIndex: 1000,
        
        // 새 창 생성
        createWindow: function(title, contentUrl, options = {}) {
            const windowId = 'win95-window-' + Date.now();
            
            // 기본 옵션 설정
            const defaultOptions = {
                width: 640,
                height: 480,
                top: 50 + (this.windows.length * 20),
                left: 50 + (this.windows.length * 20),
                resizable: true,
                maximizable: true,
                minimizable: true,
                closable: true
            };
            
            const windowOptions = {...defaultOptions, ...options};
            
            // 창 템플릿
            const windowTemplate = `
                <div id="${windowId}" class="win95-window" style="width: ${windowOptions.width}px; height: ${windowOptions.height}px; top: ${windowOptions.top}px; left: ${windowOptions.left}px; z-index: ${this.zIndex++};">
                    <div class="win95-title-bar">
                        <div class="win95-title-icon">
                            <img src="/img/icons/document.png" alt="문서" class="title-icon">
                        </div>
                        <div class="win95-title-text">${title}</div>
                        <div class="win95-controls">
                            ${windowOptions.minimizable ? '<button class="win95-control win95-minimize">_</button>' : ''}
                            ${windowOptions.maximizable ? '<button class="win95-control win95-maximize">□</button>' : ''}
                            ${windowOptions.closable ? '<button class="win95-control win95-close">×</button>' : ''}
                        </div>
                    </div>
                    <div class="win95-menu-bar">
                        <div class="win95-menu-item">파일</div>
                        <div class="win95-menu-item">편집</div>
                        <div class="win95-menu-item">보기</div>
                        <div class="win95-menu-item">도움말</div>
                    </div>
                    <div class="win95-window-content">
                        <div class="win95-loading">로딩 중...</div>
                    </div>
                    <div class="win95-statusbar">
                        <span>준비</span>
                    </div>
                </div>
            `;
            
            // 창을 DOM에 추가
            const contentArea = document.querySelector('.content-with-sidebar');
            if (contentArea) {
                contentArea.insertAdjacentHTML('beforeend', windowTemplate);
                
                const windowElement = document.getElementById(windowId);
                
                // 내용 로드
                if (contentUrl) {
                    this.loadContent(windowId, contentUrl);
                }
                
                // 이벤트 등록
                this.setupWindowEvents(windowId);
                
                // 창 목록에 추가
                this.windows.push({
                    id: windowId,
                    title: title,
                    contentUrl: contentUrl,
                    element: windowElement,
                    isMaximized: false,
                    isMinimized: false
                });
                
                // 창 활성화
                this.activateWindow(windowId);
                
                return windowId;
            }
            
            return null;
        },
        
        // 내용 로드 (AJAX 대신 새로운 HTML 요소 추가)
        loadContent: function(windowId, contentUrl) {
            const windowElement = document.getElementById(windowId);
            if (windowElement) {
                const contentElement = windowElement.querySelector('.win95-window-content');
                if (contentElement) {
                    // HTML 문자열로 직접 내용 삽입 (예시)
                    if (typeof contentUrl === 'string' && contentUrl.startsWith('<')) {
                        contentElement.innerHTML = contentUrl;
                    } 
                    // HTML 요소로 내용 삽입
                    else if (contentUrl instanceof HTMLElement) {
                        contentElement.innerHTML = '';
                        contentElement.appendChild(contentUrl);
                    }
                    // AJAX로 내용 로드 (실제 URL인 경우)
                    else if (typeof contentUrl === 'string') {
                        contentElement.innerHTML = '<div class="win95-loading">로딩 중...</div>';
                        
                        fetch(contentUrl)
                            .then(response => response.text())
                            .then(html => {
                                contentElement.innerHTML = html;
                            })
                            .catch(error => {
                                contentElement.innerHTML = `<div class="win95-error">로드 오류: ${error.message}</div>`;
                            });
                    }
                }
            }
        },
        
        // 창 이벤트 설정
        setupWindowEvents: function(windowId) {
            const windowElement = document.getElementById(windowId);
            if (!windowElement) return;
            
            const titleBar = windowElement.querySelector('.win95-title-bar');
            const closeButton = windowElement.querySelector('.win95-close');
            const maximizeButton = windowElement.querySelector('.win95-maximize');
            const minimizeButton = windowElement.querySelector('.win95-minimize');
            
            // 창 활성화
            windowElement.addEventListener('mousedown', () => {
                this.activateWindow(windowId);
            });
            
            // 창 닫기
            if (closeButton) {
                closeButton.addEventListener('click', () => {
                    this.closeWindow(windowId);
                });
            }
            
            // 창 최대화
            if (maximizeButton) {
                maximizeButton.addEventListener('click', () => {
                    this.toggleMaximize(windowId);
                });
            }
            
            // 창 최소화 (간단한 구현)
            if (minimizeButton) {
                minimizeButton.addEventListener('click', () => {
                    windowElement.style.display = 'none';
                    // 실제로는 작업 표시줄에 표시 구현 필요
                });
            }
            
            // 드래그 기능
            if (titleBar) {
                this.makeDraggable(windowId, titleBar);
            }
        },
        
        // 창 활성화
        activateWindow: function(windowId) {
            // 모든 창 비활성화
            this.windows.forEach(window => {
                if (window.element) {
                    window.element.classList.remove('win95-window-active');
                    window.element.style.zIndex = 1000;
                }
            });
            
            // 현재 창 활성화
            const windowElement = document.getElementById(windowId);
            if (windowElement) {
                windowElement.classList.add('win95-window-active');
                windowElement.style.zIndex = this.zIndex++;
            }
        },
        
        // 창 닫기
        closeWindow: function(windowId) {
            const windowElement = document.getElementById(windowId);
            if (windowElement) {
                windowElement.remove();
                
                // 창 목록에서 제거
                this.windows = this.windows.filter(window => window.id !== windowId);
            }
        },
        
        // 창 최대화/복원
        toggleMaximize: function(windowId) {
            const windowElement = document.getElementById(windowId);
            if (!windowElement) return;
            
            const window = this.windows.find(w => w.id === windowId);
            if (!window) return;
            
            if (!window.isMaximized) {
                // 창 위치 및 크기 저장
                window.prevState = {
                    top: windowElement.style.top,
                    left: windowElement.style.left,
                    width: windowElement.style.width,
                    height: windowElement.style.height
                };
                
                // 창 최대화
                windowElement.classList.add('win95-window-maximized');
                window.isMaximized = true;
            } else {
                // 창 복원
                windowElement.classList.remove('win95-window-maximized');
                
                if (window.prevState) {
                    windowElement.style.top = window.prevState.top;
                    windowElement.style.left = window.prevState.left;
                    windowElement.style.width = window.prevState.width;
                    windowElement.style.height = window.prevState.height;
                }
                
                window.isMaximized = false;
            }
        },
        
        // 드래그 기능
        makeDraggable: function(windowId, dragHandle) {
            const windowElement = document.getElementById(windowId);
            if (!windowElement || !dragHandle) return;
            
            let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
            
            dragHandle.onmousedown = (e) => {
                e.preventDefault();
                
                // 창 활성화
                this.activateWindow(windowId);
                
                // 최대화된 창은 드래그 불가
                const window = this.windows.find(w => w.id === windowId);
                if (window && window.isMaximized) return;
                
                pos3 = e.clientX;
                pos4 = e.clientY;
                
                document.onmouseup = closeDragElement;
                document.onmousemove = elementDrag;
            };
            
            function elementDrag(e) {
                e.preventDefault();
                
                pos1 = pos3 - e.clientX;
                pos2 = pos4 - e.clientY;
                pos3 = e.clientX;
                pos4 = e.clientY;
                
                windowElement.style.top = (windowElement.offsetTop - pos2) + "px";
                windowElement.style.left = (windowElement.offsetLeft - pos1) + "px";
            }
            
            function closeDragElement() {
                document.onmouseup = null;
                document.onmousemove = null;
            }
        }
    };
    
    // 전역으로 접근 가능하게 설정
    window.Win95WindowManager = Win95WindowManager;
    
    // 링크 클릭 시 윈도우 창 열기 이벤트 핸들러
    function setupWindowLinks() {
        const windowLinks = document.querySelectorAll('a[data-win95-window]');
        
        windowLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                
                const url = this.getAttribute('href');
                const title = this.getAttribute('data-win95-title') || this.textContent || '새 창';
                
                Win95WindowManager.createWindow(title, url);
            });
        });
    }
    
    // 링크 설정
    setupWindowLinks();
});

// 사이드바 파일 항목 등록 도우미 함수
function registerWin95FileItem(selector, contentUrl, title) {
    const fileItem = document.querySelector(selector);
    if (fileItem) {
        fileItem.addEventListener('click', function(e) {
            e.preventDefault();
            if (window.Win95WindowManager) {
                window.Win95WindowManager.createWindow(title || '문서', contentUrl);
            }
        });
    }
}