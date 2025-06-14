let stompClient = null;
let connected = false;
let activeRooms = new Map();
let currentRoomId = null;
let currentMessages = new Map();
let roomSubscriptions = new Map();
let memberInfoCache = new Map();
let adminSubscription = null;
let isUserScrolling = false; // 사용자가 스크롤 중인지 체크
let scrollTimeout = null; // 스크롤 타이머

const chatRooms = document.getElementById('chatRooms');
const mainChat = document.getElementById('mainChat');
const connectionStatus = document.getElementById('connectionStatus');
const statusBar = document.getElementById('statusBar');

// 빠른 답변 템플릿
const quickReplies = [
	"안녕하세요! 무엇을 도와드릴까요?",
	"네, 확인해보겠습니다.",
	"조금만 기다려주세요.",
	"추가로 궁금한 점이 있으시면 언제든 문의해주세요.",
	"감사합니다."
];

// 채팅 스크롤 관리 함수들
function scrollToBottom(force = false) {
    const chatMessagesDiv = document.getElementById('chatMessages');
    if (!chatMessagesDiv) {
        console.log('chatMessagesDiv를 찾을 수 없음');
        return;
    }
    
    // 사용자가 스크롤 중이고 강제가 아니라면 스크롤하지 않음
    if (isUserScrolling && !force) {
        console.log('사용자 스크롤 중이므로 자동 스크롤 생략');
        return;
    }
    
    console.log('스크롤 시작 - scrollHeight:', chatMessagesDiv.scrollHeight, 'clientHeight:', chatMessagesDiv.clientHeight);
    
    // 여러 방법으로 강제 스크롤 시도
    const scrollToBottomImmediate = () => {
        const maxScroll = chatMessagesDiv.scrollHeight - chatMessagesDiv.clientHeight;
        chatMessagesDiv.scrollTop = maxScroll;
        console.log('스크롤 실행 - scrollTop:', chatMessagesDiv.scrollTop, 'maxScroll:', maxScroll);
    };
    
    // 즉시 실행
    scrollToBottomImmediate();
    
    // requestAnimationFrame으로 한번 더
    requestAnimationFrame(() => {
        scrollToBottomImmediate();
    });
    
    // 추가 안전장치 - 조금 더 기다린 후 다시 시도
    setTimeout(() => {
        scrollToBottomImmediate();
    }, 50);
    
    // 최종 안전장치
    setTimeout(() => {
        scrollToBottomImmediate();
    }, 200);
}

function isScrolledToBottom() {
    const chatMessagesDiv = document.getElementById('chatMessages');
    if (!chatMessagesDiv) return true;
    
    const threshold = 50; // 50px 이내면 하단으로 간주
    return chatMessagesDiv.scrollHeight - chatMessagesDiv.clientHeight <= chatMessagesDiv.scrollTop + threshold;
}

function setupScrollHandler() {
    const chatMessagesDiv = document.getElementById('chatMessages');
    if (!chatMessagesDiv) return;
    
    chatMessagesDiv.addEventListener('scroll', () => {
        // 사용자가 스크롤 중임을 표시
        isUserScrolling = true;
        
        // 스크롤이 하단에 가까우면 자동 스크롤 재활성화
        if (isScrolledToBottom()) {
            isUserScrolling = false;
        }
        
        // 스크롤 타이머 리셋
        clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(() => {
            // 3초 후 자동 스크롤 재활성화 (사용자가 더 이상 스크롤하지 않을 때)
            if (isScrolledToBottom()) {
                isUserScrolling = false;
            }
        }, 3000);
    });
    
    // 터치 이벤트 추가 (모바일)
    chatMessagesDiv.addEventListener('touchstart', () => {
        isUserScrolling = true;
    });
    
    chatMessagesDiv.addEventListener('touchend', () => {
        setTimeout(() => {
            if (isScrolledToBottom()) {
                isUserScrolling = false;
            }
        }, 100);
    });
}

// 페이지 로드 시 기존 문의 내역 불러오기
async function loadExistingData() {
	try {
		console.log('기존 데이터 로드 시작...');

		const response = await fetch('/qanda/api/rooms/summary', {
			credentials: 'same-origin'
		});

		if (!response.ok) {
			throw new Error(`HTTP ${response.status}: ${response.statusText}`);
		}

		const roomsSummary = await response.json();
		console.log('로드된 방 정보:', roomsSummary);

		activeRooms.clear();
		currentMessages.clear();

		for (const roomInfo of roomsSummary) {
			const roomId = roomInfo.roomId;

			const messagesResponse = await fetch(`/qanda/api/rooms/${roomId}/messages`, {
				credentials: 'same-origin'
			});
			const messages = await messagesResponse.json();
			currentMessages.set(roomId, messages);

			let chatStatus = 'active';
			if (messages.length > 0) {
				const lastMessage = messages[messages.length - 1];
				if (lastMessage.type === 'END') {
					chatStatus = 'ended';
				}
			}

			activeRooms.set(roomId, {
				id: roomId,
				memberId: roomInfo.memberId,
				memberName: roomInfo.memberName,
				memberNickName: roomInfo.memberNickName,
				lastMessage: roomInfo.lastMessage,
				lastTime: new Date(roomInfo.lastTime),
				unreadCount: roomInfo.unreadCount,
				chatStatus: chatStatus
			});
		}

		renderRoomList();
		console.log('기존 데이터 로드 완료! activeRooms.size:', activeRooms.size);

	} catch (error) {
		console.error('기존 데이터 로드 실패:', error);
	}
}

// 웹소켓 연결
function connect() {
	const socket = new SockJS('/ws');
	stompClient = Stomp.over(socket);

	stompClient.connect({}, function(frame) {
		console.log('Connected: ' + frame);
		connected = true;
		updateConnectionStatus('온라인', 'connected');

		adminSubscription = stompClient.subscribe('/topic/admin', function(message) {
			const chatMessage = JSON.parse(message.body);
			handleNewRoomMessage(chatMessage);
		});

	}, function(error) {
		console.log('Connection error: ' + error);
		connected = false;
		updateConnectionStatus('연결 실패', 'disconnected');
		setTimeout(connect, 5000);
	});
}

// 연결 상태 업데이트
function updateConnectionStatus(text, className) {
	connectionStatus.textContent = text;
	statusBar.textContent = `상태: ${text}`;
	statusBar.className = 'connection-status ' + className;
}

// 새로운 방 메시지 처리
async function handleNewRoomMessage(message) {
	const roomId = message.roomId;

	if (!activeRooms.has(roomId)) {
		const roomData = {
			id: roomId,
			memberId: roomId,
			memberName: message.sender,
			memberNickName: message.sender,
			lastMessage: '',
			lastTime: new Date(),
			unreadCount: 0,
			chatStatus: 'active'
		};
		activeRooms.set(roomId, roomData);
		currentMessages.set(roomId, []);
	}

	if (currentRoomId !== roomId) {
		currentMessages.get(roomId).push(message);
		updateRoomInList(roomId, message);
	}
}

// 방별 메시지 처리
function handleRoomMessage(message) {
	const roomId = message.roomId;
	
	if (!currentMessages.has(roomId)) {
		currentMessages.set(roomId, []);
	}
	currentMessages.get(roomId).push(message);

	updateRoomInList(roomId, message);

	if (currentRoomId === roomId) {
		displayMessage(message);
	}
}

// 방 목록에서 방 정보 업데이트
function updateRoomInList(roomId, message) {
	const room = activeRooms.get(roomId);
	if (room) {
		room.lastMessage = message.content;
		room.lastTime = new Date(message.createdAt || new Date());

		if (message.type === 'END') {
			room.chatStatus = 'ended';
		} else if (message.type === 'RESTART' || message.type === 'CHAT') {
			room.chatStatus = 'active';
		}

		if (currentRoomId !== roomId) {
			room.unreadCount++;
		}

		renderRoomList();
	}
}

// 방 목록 렌더링
function renderRoomList() {
	const sortedRooms = Array.from(activeRooms.values())
		.sort((a, b) => b.lastTime - a.lastTime);

	if (sortedRooms.length === 0) {
		chatRooms.innerHTML = `
                    <div class="empty-rooms">
                        <div style="font-size: 24px; margin-bottom: 10px;">💬</div>
                        <div>새로운 문의를 기다리고 있습니다</div>
                    </div>
                `;
		return;
	}

	chatRooms.innerHTML = sortedRooms.map(room => {
		const statusText = room.chatStatus === 'ended' ? '종료됨' : '진행중';
		const statusClass = room.chatStatus === 'ended' ? 'ended' : 'active';

		return `
                    <div class="room-item ${currentRoomId === room.id ? 'active' : ''}" 
                         onclick="selectRoom('${room.id}')">
                        <div class="room-user">${room.memberNickName}</div>
                        <div class="room-member-info">회원ID: ${room.memberId}</div>
                        <div class="room-last-message">${room.lastMessage}</div>
                        <div class="room-status ${statusClass}">${statusText}</div>
                        <div class="room-time">${formatTime(room.lastTime)}</div>
                        ${room.unreadCount > 0 ? `<div class="unread-badge">${room.unreadCount}</div>` : ''}
                    </div>
                `;
	}).join('');
}

// 방 선택
function selectRoom(roomId) {
	console.log('selectRoom 호출됨, roomId:', roomId);

	currentRoomId = roomId;
	const room = activeRooms.get(roomId);

	if (!room) {
		console.error('방 정보를 찾을 수 없음:', roomId);
		return;
	}

	// 스크롤 상태 리셋
	isUserScrolling = false;
	clearTimeout(scrollTimeout);

	room.unreadCount = 0;
	renderRoomList();

	if (stompClient && connected) {
		stompClient.send('/app/chat.markAsRead', {}, roomId);
	}

	renderChatArea(room);

	if (roomSubscriptions.has(roomId)) {
		roomSubscriptions.get(roomId).unsubscribe();
		roomSubscriptions.delete(roomId);
	}

	if (stompClient && connected) {
		const subscription = stompClient.subscribe('/topic/room.' + roomId, function(message) {
			const chatMessage = JSON.parse(message.body);
			if (chatMessage.sender !== '관리자') {
				handleRoomMessage(chatMessage);
			}
		});
		roomSubscriptions.set(roomId, subscription);
	}

	document.getElementById('adminContainer').classList.add('chat-open');

	console.log('selectRoom 완료, currentRoomId:', currentRoomId);
}

// 채팅 영역 렌더링
function renderChatArea(room) {
	mainChat.innerHTML = `
                <div class="chat-header">
                    <div class="chat-user-info">${room.memberNickName}</div>
                    <div class="chat-user-status">회원ID: ${room.memberId} | 회원명: ${room.memberName}</div>
                </div>
                
                <div class="chat-messages" id="chatMessages"></div>
                
                <div class="admin-input-container">
                    <div class="quick-replies">
                        ${quickReplies.map(reply =>
		`<button class="quick-reply-btn" onclick="sendQuickReply('${reply}')">${reply}</button>`
	).join('')}
                    </div>
                    <div class="admin-input">
                        <input type="text" class="admin-message-input" id="adminMessageInput" 
                               placeholder="메시지를 입력하세요..." maxlength="500">
                        <button class="admin-send-button" id="adminSendButton" onclick="sendAdminMessage()">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
                            </svg>
                        </button>
                    </div>
                </div>
            `;

	// 기존 메시지들 표시
	const messages = currentMessages.get(room.id) || [];
	const chatMessagesDiv = document.getElementById('chatMessages');
	
	chatMessagesDiv.innerHTML = '';
	
	// 메시지를 하나씩 순서대로 추가
	messages.forEach((message, index) => {
		displayMessage(message, false); // 초기 로드시에는 개별 스크롤하지 않음
	});

	// DOM이 완전히 렌더링된 후 스크롤
	setTimeout(() => {
		console.log('메시지 로드 완료, 스크롤 시작');
		scrollToBottom(true);
		setupScrollHandler();
		addMobileBackButton();
		setupMobileFocus();
		setupMobileKeyboardHandler();
	}, 100);

	// 더 확실하게 하기 위한 추가 시도
	setTimeout(() => {
		scrollToBottom(true);
	}, 200);

	// 엔터키 이벤트
	const messageInput = document.getElementById('adminMessageInput');
	messageInput.addEventListener('keypress', function(e) {
		if (e.key === 'Enter' && !e.shiftKey) {
			e.preventDefault();
			sendAdminMessage();
		}
	});
	
	// 입력창에 포커스
	setTimeout(() => {
		messageInput.focus();
	}, 150);
}

// 메시지 표시 (스크롤 최적화)
function displayMessage(message, shouldScroll = true) {
	const chatMessagesDiv = document.getElementById('chatMessages');
	if (!chatMessagesDiv) return;

	const wasScrolledToBottom = isScrolledToBottom();

	const messageDiv = document.createElement('div');

	if (message.type === 'JOIN') {
		messageDiv.className = 'message system';
		messageDiv.innerHTML = `
                    <div class="message-bubble">${message.content}</div>
                `;
	} else if (message.type === 'END' || message.type === 'RESTART') {
		messageDiv.className = 'message system';
		messageDiv.innerHTML = `
                    <div class="message-bubble">${message.content}</div>
                `;
	} else {
		const isAdmin = message.sender === '관리자';
		messageDiv.className = 'message ' + (isAdmin ? 'admin' : 'user');

		const messageTime = message.createdAt ? new Date(message.createdAt) : new Date();

		messageDiv.innerHTML = `
                    ${!isAdmin ? `<div class="message-sender">${message.sender}</div>` : ''}
                    <div class="message-bubble">${message.content}</div>
                    <div class="message-time">${formatTime(messageTime)}</div>
                `;
	}

	chatMessagesDiv.appendChild(messageDiv);

	// 스크롤 조건: 
	// 1. shouldScroll이 true이고
	// 2. (사용자가 스크롤 중이 아니거나 하단에 있었거나 관리자 메시지인 경우)
	if (shouldScroll && (!isUserScrolling || wasScrolledToBottom || message.sender === '관리자')) {
		// 메시지 추가 후 즉시 스크롤
		setTimeout(() => {
			scrollToBottom(true);
		}, 10);
		
		// 추가 안전장치
		setTimeout(() => {
			scrollToBottom(true);
		}, 100);
	}
}

// 관리자 메시지 전송
function sendAdminMessage() {
	const input = document.getElementById('adminMessageInput');
	const content = input.value.trim();

	if (!content || !currentRoomId) {
		console.log('메시지 전송 실패 - content:', content, 'currentRoomId:', currentRoomId);
		return;
	}

	const adminMessage = {
		type: 'CHAT',
		content: content,
		sender: '관리자',
		roomId: currentRoomId,
		createdAt: new Date().toISOString()
	};

	if (stompClient && connected) {
		console.log('관리자 메시지 전송:', adminMessage);

		// 관리자 메시지는 항상 하단으로 스크롤
		isUserScrolling = false;
		
		displayMessage(adminMessage);
		
		if (!currentMessages.has(currentRoomId)) {
			currentMessages.set(currentRoomId, []);
		}
		currentMessages.get(currentRoomId).push(adminMessage);

		stompClient.send('/app/chat.adminReply', {}, JSON.stringify(adminMessage));
		input.value = '';

		updateRoomInList(currentRoomId, adminMessage);
		
		// 입력창에 다시 포커스
		input.focus();
	}
}

// 빠른 답변 전송
function sendQuickReply(content) {
	const input = document.getElementById('adminMessageInput');
	input.value = content;
	sendAdminMessage();
}

// 시간 포맷팅
function formatTime(date) {
	return date.toLocaleTimeString('ko-KR', {
		hour: '2-digit',
		minute: '2-digit'
	});
}

// 모바일에서 뒤로가기 버튼 추가
function addMobileBackButton() {
	if (window.innerWidth <= 768 && currentRoomId) {
		const chatHeader = document.querySelector('.chat-header');
		if (chatHeader && !chatHeader.querySelector('.mobile-back-btn')) {
			const backButton = document.createElement('button');
			backButton.className = 'mobile-back-btn';
			backButton.innerHTML = '← 목록';
			backButton.onclick = closeMobileChat;
			backButton.style.cssText = `
				position: absolute;
				left: 10px;
				top: 50%;
				transform: translateY(-50%);
				background: #667eea;
				color: white;
				border: none;
				padding: 8px 12px;
				border-radius: 6px;
				font-size: 14px;
				cursor: pointer;
			`;
			chatHeader.style.position = 'relative';
			chatHeader.appendChild(backButton);
		}
	}
}

// 모바일 채팅 닫기
function closeMobileChat() {
	document.getElementById('adminContainer').classList.remove('chat-open');
	currentRoomId = null;
	isUserScrolling = false;
	clearTimeout(scrollTimeout);
}

// 윈도우 리사이즈 처리
function handleResize() {
	if (window.innerWidth > 768) {
		document.getElementById('adminContainer').classList.remove('chat-open');
	}
	
	if (currentRoomId) {
		addMobileBackButton();
		// 리사이즈 후 스크롤 재조정
		setTimeout(() => {
			if (!isUserScrolling) {
				scrollToBottom(true);
			}
		}, 100);
	}
}

// 채팅 메시지 영역 클릭 시 입력창 포커스 (모바일)
function setupMobileFocus() {
	const chatMessagesDiv = document.getElementById('chatMessages');
	const messageInput = document.getElementById('adminMessageInput');
	
	if (chatMessagesDiv && messageInput && window.innerWidth <= 768) {
		chatMessagesDiv.addEventListener('click', (e) => {
			// 메시지나 링크를 클릭한 게 아니라면 입력창에 포커스
			if (e.target === chatMessagesDiv || e.target.classList.contains('message') || 
				e.target.classList.contains('message-bubble')) {
				messageInput.focus();
			}
		});
	}
}

// 키보드 나타날 때 스크롤 조정 (모바일)
function setupMobileKeyboardHandler() {
	if (window.innerWidth <= 768) {
		const messageInput = document.getElementById('adminMessageInput');
		if (messageInput) {
			messageInput.addEventListener('focus', () => {
				// 키보드가 나타나면 잠시 후 스크롤 조정
				setTimeout(() => {
					if (!isUserScrolling) {
						scrollToBottom(true);
					}
				}, 300);
			});
		}
	}
}

// 페이지 가시성 변경 시 처리 (백그라운드에서 돌아왔을 때)
function setupVisibilityHandler() {
	document.addEventListener('visibilitychange', () => {
		if (!document.hidden && currentRoomId && !isUserScrolling) {
			// 페이지가 다시 보일 때 스크롤 위치 확인 후 조정
			setTimeout(() => {
				scrollToBottom(true);
			}, 100);
		}
	});
}

// 새로고침 전 경고 (타이핑 중일 때)
function setupBeforeUnloadHandler() {
	let isTyping = false;
	
	window.addEventListener('beforeunload', (e) => {
		if (isTyping && currentRoomId) {
			e.preventDefault();
			e.returnValue = '작성 중인 메시지가 있습니다. 페이지를 나가시겠습니까?';
		}
		
		// 연결 해제
		if (stompClient) {
			if (adminSubscription) {
				adminSubscription.unsubscribe();
			}
			roomSubscriptions.forEach(subscription => subscription.unsubscribe());
			roomSubscriptions.clear();
			stompClient.disconnect();
		}
	});
	
	// 타이핑 상태 감지
	document.addEventListener('input', (e) => {
		if (e.target && e.target.id === 'adminMessageInput') {
			isTyping = e.target.value.trim().length > 0;
		}
	});
}

// 자동 재연결 처리
function setupAutoReconnect() {
	let reconnectAttempts = 0;
	const maxReconnectAttempts = 5;
	
	function attemptReconnect() {
		if (reconnectAttempts < maxReconnectAttempts) {
			reconnectAttempts++;
			console.log(`재연결 시도 ${reconnectAttempts}/${maxReconnectAttempts}`);
			updateConnectionStatus(`재연결 중... (${reconnectAttempts}/${maxReconnectAttempts})`, 'disconnected');
			
			setTimeout(() => {
				connect();
			}, 2000 * reconnectAttempts); // 점진적 지연
		} else {
			updateConnectionStatus('연결 실패 - 새로고침해주세요', 'disconnected');
		}
	}
	
	// 연결 성공 시 재시도 횟수 리셋
	window.addEventListener('websocket-connected', () => {
		reconnectAttempts = 0;
	});
	
	return attemptReconnect;
}

// 페이지 로드 시 실행
window.addEventListener('load', async function() {
	console.log('페이지 로드 시작');

	// 먼저 기존 데이터 로드
	await loadExistingData();

	// 그 다음 웹소켓 연결
	connect();

	// 이벤트 핸들러 설정
	window.addEventListener('resize', handleResize);
	setupVisibilityHandler();
	setupBeforeUnloadHandler();
	
	// 자동 재연결 설정
	const attemptReconnect = setupAutoReconnect();
	
	// 웹소켓 연결 함수 수정하여 자동 재연결 포함
	const originalConnect = connect;
	connect = function() {
		const socket = new SockJS('/ws');
		stompClient = Stomp.over(socket);

		stompClient.connect({}, function(frame) {
			console.log('Connected: ' + frame);
			connected = true;
			updateConnectionStatus('온라인', 'connected');
			
			// 연결 성공 이벤트 발생
			window.dispatchEvent(new CustomEvent('websocket-connected'));

			adminSubscription = stompClient.subscribe('/topic/admin', function(message) {
				const chatMessage = JSON.parse(message.body);
				handleNewRoomMessage(chatMessage);
			});

		}, function(error) {
			console.log('Connection error: ' + error);
			connected = false;
			attemptReconnect();
		});
	};
});

// 전역 함수로 노출 (HTML에서 호출)
window.selectRoom = selectRoom;
window.sendAdminMessage = sendAdminMessage;
window.sendQuickReply = sendQuickReply;
window.closeMobileChat = closeMobileChat;