// 🔥 즉시 실행으로 DOM 준비 확인
		    (function() {
		        console.log('🚀 채팅 페이지 로딩 시작');
		        
		        let stompClient = null;
		        let username = null;
		        let roomId = null;
		        let connected = false;
		        let isSubscribed = false;
		        let messageIds = new Set();
		        let chatEnded = false;
		        let initialized = false;
		        
		        // 🔥 중복 방지를 위한 고유 ID 생성 함수
		        function generateMessageId(message) {
		            return `${message.type}_${message.sender}_${message.content}_${message.timestamp || Date.now()}`;
		        }

		        // 🔥 DOM 요소 확인 함수
		        function waitForElements() {
		            return new Promise((resolve) => {
		                function checkElements() {
		                    const elements = [
		                        document.getElementById('connectionStatus'),
		                        document.getElementById('statusInfo'),
		                        document.getElementById('messageInput'),
		                        document.getElementById('sendButton'),
		                        document.getElementById('endChatButton'),
		                        document.getElementById('messages'),
		                        document.getElementById('userInfo'),
		                        document.getElementById('inputContainer')
		                    ];
		                    
		                    const allReady = elements.every(el => el !== null);
		                    
		                    if (allReady) {
		                        resolve(elements);
		                    } else {
		                        setTimeout(checkElements, 50);
		                    }
		                }
		                checkElements();
		            });
		        }

		        // 🔥 메인 초기화 함수
		        async function initialize() {
		            if (initialized) {
		                return;
		            }
		            
		            try {
		                
		                // 1. DOM 요소들이 준비될 때까지 대기
		                const elements = await waitForElements();
		                
		                // 2. 전역 변수로 DOM 요소들 설정
		                window.chatElements = {
		                    connectionStatus: elements[0],
		                    statusInfo: elements[1],
		                    messageInput: elements[2],
		                    sendButton: elements[3],
		                    endChatButton: elements[4],
		                    messagesDiv: elements[5],
		                    userInfo: elements[6],
		                    inputContainer: elements[7]
		                };
		                
		                // 3. 즉시 UI 활성화 (사용자가 바로 볼 수 있도록)
		                elements[1].textContent = '연결됨';
		                elements[1].className = 'connection-status connected';
		                
		                // 4. 이벤트 리스너 설정
		                setupEventListeners();
		                
		                // 5. 사용자 정보 로드 (백그라운드)
		                setTimeout(async () => {
		                    await loadUserInfo();
		                    setupRoomId();
		                    await loadChatHistory();
		                    connect();
		                }, 100);
		                
		                initialized = true;
		                
		            } catch (error) {
		            }
		        }

		        // 🔥 이벤트 리스너 설정
		        function setupEventListeners() {
		            const { sendButton, endChatButton, messageInput } = window.chatElements;
		            
		            sendButton.addEventListener('click', sendMessage);
		            endChatButton.addEventListener('click', endChat);
		            
		            messageInput.addEventListener('keypress', function(e) {
		                if (e.key === 'Enter' && !e.shiftKey) {
		                    e.preventDefault();
		                    if (messageInput.value.trim()) {
		                        sendMessage();
		                    }
		                }
		            });

		            messageInput.addEventListener('input', function() {
		                // 자동 크기 조절
		                messageInput.style.height = 'auto';
		                messageInput.style.height = Math.min(messageInput.scrollHeight, 100) + 'px';
		                
		                // 채팅 종료 상태 안내
		                if (chatEnded && messageInput.value.trim()) {
		                    window.chatElements.statusInfo.textContent = 'Enter를 누르면 새로운 문의가 시작됩니다.';
		                    window.chatElements.statusInfo.className = 'connection-status';
		                } else if (chatEnded) {
		                    window.chatElements.statusInfo.textContent = '채팅이 종료되었습니다. 새 메시지를 입력하면 다시 시작됩니다.';
		                    window.chatElements.statusInfo.className = 'connection-status disconnected';
		                }
		            });
		        }

		        // 사용자 정보 로드
		        async function loadUserInfo() {
		            try {
		                console.log('👤 사용자 정보 로드 중...');
		                
		                // Thymeleaf 값 시도
		                username = '[[${#authentication.name}]]' || null;
		                
		                if (!username || username === '[[${#authentication.name}]]' || username === 'anonymousUser') {
		                    // API 호출
		                    const response = await fetch('/qanda/api/user/info', {
		                        credentials: 'same-origin'
		                    });
		                    if (response.ok) {
		                        const userInfoData = await response.json();
		                        username = userInfoData.memberId;
		                    }
		                }
		                
		                if (!username) {
		                    username = 'guest_' + Date.now();
		                    window.chatElements.userInfo.textContent = '게스트';
		                } else {
		                    window.chatElements.userInfo.textContent = `${username}님`;
		                }
		                
		            } catch (error) {
		                username = 'guest_' + Date.now();
		                window.chatElements.userInfo.textContent = '게스트';
		            }
		        }

		        function setupRoomId() {
		            roomId = username;
		        }

		        async function loadChatHistory() {
		            try {
		                
		                const response = await fetch(`/qanda/api/rooms/${roomId}/messages`, {
		                    credentials: 'same-origin'
		                });
		                
		                if (response.ok) {
		                    const messages = await response.json();
		                    
		                    if (messages.length > 0) {
		                        window.chatElements.messagesDiv.innerHTML = '';
		                        
		                        messages.forEach(message => {
		                            displayMessage(message, false);
		                        });
		                        
		                        const lastMessage = messages[messages.length - 1];
		                        if (lastMessage.type === 'END') {
		                            chatEnded = true;
		                            disableChatInput('채팅이 종료되었습니다. 새 메시지를 입력하면 다시 시작됩니다.');
		                        }
		                    }
		                }
		                
		                
		            } catch (error) {
		            }
		        }

		        function connect() {
		            if (stompClient && connected) {
		                return;
		            }
		            
		            
		            const socket = new SockJS('/ws');
		            stompClient = Stomp.over(socket);
		            
		            stompClient.connect({}, function(frame) {
		                console.log('✅ WebSocket 연결 성공');
		                connected = true;
		                updateConnectionStatus('연결됨', 'connected');
		                subscribeToRoom();
		            }, function(error) {
		                connected = false;
		                updateConnectionStatus('연결 실패', 'disconnected');
		                
		                setTimeout(() => {
		                    if (!connected) {
		                        connect();
		                    }
		                }, 5000);
		            });
		        }

		        function subscribeToRoom() {
		            if (!stompClient || !connected || !roomId || isSubscribed) return;
		            
		            try {
		                stompClient.subscribe('/topic/room.' + roomId, function(message) {
		                    const chatMessage = JSON.parse(message.body);
		                    
		                    // 🔥 중복 체크: 내가 보낸 메시지는 서버에서 받지 않음
		                    if (chatMessage.sender === username && 
		                        (chatMessage.type === 'END' || chatMessage.type === 'RESTART' || chatMessage.type === 'CHAT')) {
		                        return;
		                    }
		                    
		                    if (displayMessage(chatMessage, true)) {
		                        if (chatMessage.sender === '관리자' && chatEnded) {
		                            chatEnded = false;
		                            enableChatInput();
		                        }
		                    }
		                });
		                
		                isSubscribed = true;
		                
		            } catch (error) {
		            }
		        }

		        function sendMessage() {
		            const content = window.chatElements.messageInput.value.trim();
		            if (!content || !connected || !roomId) return;

		            if (chatEnded) {
		                const now = new Date();
		                const timeString = now.toLocaleString('ko-KR', {
		                    year: 'numeric', month: 'long', day: 'numeric',
		                    hour: '2-digit', minute: '2-digit'
		                });

		                const restartMessage = {
		                    type: 'RESTART',
		                    content: `---- ${timeString} 새로운 문의가 시작되었습니다 ----`,
		                    sender: username, roomId: roomId,
		                    timestamp: new Date().getTime()
		                };

		                // 🔥 로컬에서 즉시 표시
		                displayMessage(restartMessage, false);
		                // 서버에 전송
		                stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(restartMessage));
		                
		                chatEnded = false;
		                enableChatInput();
		            }

		            window.chatElements.sendButton.disabled = true;
		            
		            const chatMessage = {
		                type: 'CHAT', content: content, sender: username,
		                roomId: roomId, timestamp: new Date().getTime()
		            };

		            try {
		                // 🔥 내 메시지는 로컬에서 즉시 표시
		                displayMessage(chatMessage, false);
		                
		                // 서버에 전송
		                stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(chatMessage));
		                window.chatElements.messageInput.value = '';
		                window.chatElements.messageInput.style.height = 'auto';
		            } catch (error) {
		            } finally {
		                setTimeout(() => {
		                    if (!chatEnded) {
		                        window.chatElements.sendButton.disabled = false;
		                    }
		                }, 500);
		            }
		        }

		        function endChat() {
		            if (!connected || !roomId || chatEnded) return;

		            if (confirm('채팅을 종료하시겠습니까?')) {
		                const now = new Date();
		                const timeString = now.toLocaleString('ko-KR', {
		                    year: 'numeric', month: 'long', day: 'numeric',
		                    hour: '2-digit', minute: '2-digit'
		                });

		                const endMessage = {
		                    type: 'END',
		                    content: `---- ${timeString} 채팅이 종료되었습니다. 오늘도 행복한 하루 보내세요! ----`,
		                    sender: username, roomId: roomId,
		                    timestamp: new Date().getTime()
		                };

		                // 🔥 로컬에서 즉시 표시 (한 번만)
		                displayMessage(endMessage, false);
		                
		                // 서버에 전송
		                stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(endMessage));
		                
		                chatEnded = true;
		                disableChatInput('채팅이 종료되었습니다. 새 메시지를 입력하면 다시 시작됩니다.');
		            }
		        }

		        function displayMessage(message, checkDuplicate = false) {
		            if (message.type === 'JOIN') return false;
		            
		            // 🔥 고유 ID로 중복 체크 강화
		            const messageId = generateMessageId(message);
		            
		            if (checkDuplicate && messageIds.has(messageId)) {
		                return false;
		            }
		            
		            if (checkDuplicate) {
		                messageIds.add(messageId);
		            }
		            
		            const messageDiv = document.createElement('div');
		            
		            if (message.type === 'END' || message.type === 'RESTART') {
		                messageDiv.className = 'message system';
		                messageDiv.innerHTML = `<div class="message-bubble">${message.content}</div>`;
		            } else {
		                messageDiv.className = 'message ' + (message.sender === username ? 'mine' : 'other');
		                
		                const bubble = document.createElement('div');
		                bubble.className = 'message-bubble';
		                bubble.textContent = message.content;

		                const time = document.createElement('div');
		                time.className = 'message-time';
		                const messageTime = message.createdAt ? new Date(message.createdAt) : new Date();
		                time.textContent = messageTime.toLocaleTimeString('ko-KR', {
		                    hour: '2-digit', minute: '2-digit'
		                });

		                messageDiv.appendChild(bubble);
		                messageDiv.appendChild(time);
		            }
		            
		            window.chatElements.messagesDiv.appendChild(messageDiv);
		            window.chatElements.messagesDiv.scrollTop = window.chatElements.messagesDiv.scrollHeight;
		            
		            return true;
		        }

		        function disableChatInput(message) {
		            window.chatElements.sendButton.disabled = true;
		            window.chatElements.endChatButton.disabled = true;
		            window.chatElements.statusInfo.textContent = message;
		            window.chatElements.statusInfo.className = 'connection-status disconnected';
		        }

		        function enableChatInput() {
		            window.chatElements.messageInput.disabled = false;
		            window.chatElements.sendButton.disabled = false;
		            window.chatElements.endChatButton.disabled = false;
		            
		            if (connected) {
		                window.chatElements.statusInfo.textContent = '연결됨';
		                window.chatElements.statusInfo.className = 'connection-status connected';
		            }
		        }

		        function updateConnectionStatus(text, className) {
		            window.chatElements.connectionStatus.textContent = text;
		            
		            if (!chatEnded) {
		                window.chatElements.statusInfo.textContent = text;
		                window.chatElements.statusInfo.className = 'connection-status ' + className;
		            }
		        }

		        // 🔥 여러 시점에서 초기화 시도
		        // 1. DOM이 준비되는 즉시
		        if (document.readyState === 'loading') {
		            document.addEventListener('DOMContentLoaded', initialize);
		        } else {
		            // 이미 DOM이 준비됨
		            setTimeout(initialize, 50);
		        }
		        
		        // 2. 완전 로드 후에도 시도 (백업)
		        window.addEventListener('load', function() {
		            setTimeout(function() {
		                if (!initialized) {
		                    console.log('🔄 백업 초기화 실행');
		                    initialize();
		                }
		            }, 100);
		        });
		        
		        // 3. 페이지 언로드 시 정리
		        window.addEventListener('beforeunload', function() {
		            if (stompClient) {
		                stompClient.disconnect();
		            }
		        });

		    })(); // 즉시 실행 함수 끝