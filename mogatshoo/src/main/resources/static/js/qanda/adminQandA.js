let stompClient = null;
let connected = false;
let activeRooms = new Map();
let currentRoomId = null;
let currentMessages = new Map();
let roomSubscriptions = new Map();
let memberInfoCache = new Map(); // 회원 정보 캐시
let adminSubscription = null; // 관리자 채널 구독 관리

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

		// activeRooms 초기화
		activeRooms.clear();
		currentMessages.clear();

		// 각 방의 정보와 메시지들 불러오기
		for (const roomInfo of roomsSummary) {
			const roomId = roomInfo.roomId; // 이제 roomId가 memberId

			// 해당 방의 메시지들 가져오기
			const messagesResponse = await fetch(`/qanda/api/rooms/${roomId}/messages`, {
				credentials: 'same-origin'
			});
			const messages = await messagesResponse.json();
			currentMessages.set(roomId, messages);

			// 마지막 메시지로 채팅 상태 확인
			let chatStatus = 'active';
			if (messages.length > 0) {
				const lastMessage = messages[messages.length - 1];
				if (lastMessage.type === 'END') {
					chatStatus = 'ended';
				}
			}

			// 방 정보 저장 (백엔드에서 이미 회원 정보 포함해서 보냄)
			activeRooms.set(roomId, {
				id: roomId,
				memberId: roomInfo.memberId,
				memberName: roomInfo.memberName, // 백엔드에서 조회한 실제 이름
				memberNickName: roomInfo.memberNickName, // 백엔드에서 조회한 실제 닉네임
				lastMessage: roomInfo.lastMessage,
				lastTime: new Date(roomInfo.lastTime),
				unreadCount: roomInfo.unreadCount,
				chatStatus: chatStatus // 채팅 상태 추가
			});
		}

		// 방 목록 렌더링
		renderRoomList();
		console.log('기존 데이터 로드 완료! activeRooms.size:', activeRoomes.size);

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

		// 관리자 채널 구독 (새로운 방 알림용)
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

// 새로운 방 메시지 처리 (관리자 채널에서 오는 메시지)
async function handleNewRoomMessage(message) {
	const roomId = message.roomId; // roomId = memberId

	// 새로운 방이면 추가 (실시간으로 새 문의가 들어온 경우)
	if (!activeRooms.has(roomId)) {
		// 새로운 방의 경우 기본 정보로 임시 추가
		const roomData = {
			id: roomId,
			memberId: roomId,
			memberName: message.sender, // 일단 sender를 이름으로 사용
			memberNickName: message.sender,
			lastMessage: '',
			lastTime: new Date(),
			unreadCount: 0,
			chatStatus: 'active'
		};
		activeRooms.set(roomId, roomData);
		
		// 새로운 방의 메시지 배열 초기화
		currentMessages.set(roomId, []);
	}

	// 현재 선택된 방이 아닌 경우에만 처리 (중복 방지)
	if (currentRoomId !== roomId) {
		// 메시지 저장
		currentMessages.get(roomId).push(message);
		
		// 방 목록 업데이트
		updateRoomInList(roomId, message);
	}
}

// 방별 메시지 처리 (개별 방 구독에서 오는 메시지)
function handleRoomMessage(message) {
	const roomId = message.roomId;
	
	// 메시지 저장
	if (!currentMessages.has(roomId)) {
		currentMessages.set(roomId, []);
	}
	currentMessages.get(roomId).push(message);

	// 방 목록 업데이트
	updateRoomInList(roomId, message);

	// 현재 선택된 방이면 메시지 표시
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

		// 채팅 상태 업데이트
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

	// 읽음 처리
	room.unreadCount = 0;
	renderRoomList();

	// 서버에도 읽음 처리 요청
	if (stompClient && connected) {
		stompClient.send('/app/chat.markAsRead', {}, roomId);
	}

	// 메인 채팅 영역 업데이트
	renderChatArea(room);

	// 기존 방 구독 해제
	if (roomSubscriptions.has(roomId)) {
		roomSubscriptions.get(roomId).unsubscribe();
		roomSubscriptions.delete(roomId);
	}

	// 해당 방 구독 (사용자 메시지만 받기 위해)
	if (stompClient && connected) {
		const subscription = stompClient.subscribe('/topic/room.' + roomId, function(message) {
			const chatMessage = JSON.parse(message.body);
			// 관리자가 보낸 메시지는 제외 (이미 로컬에서 표시됨)
			if (chatMessage.sender !== '관리자') {
				handleRoomMessage(chatMessage);
			}
		});
		roomSubscriptions.set(roomId, subscription);
	}

	// 모바일에서 채팅 열기
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
	
	// 메시지 컨테이너 초기화
	chatMessagesDiv.innerHTML = '';
	
	messages.forEach(message => {
		displayMessage(message);
	});

	// 엔터키 이벤트
	document.getElementById('adminMessageInput').addEventListener('keypress', function(e) {
		if (e.key === 'Enter') {
			sendAdminMessage();
		}
	});
}

// 메시지 표시
function displayMessage(message) {
	const chatMessagesDiv = document.getElementById('chatMessages');
	if (!chatMessagesDiv) return;

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
	chatMessagesDiv.scrollTop = chatMessagesDiv.scrollHeight;
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

		// 로컬에서 즉시 표시
		displayMessage(adminMessage);
		
		// 로컬 메시지 저장소에도 추가
		if (!currentMessages.has(currentRoomId)) {
			currentMessages.set(currentRoomId, []);
		}
		currentMessages.get(currentRoomId).push(adminMessage);

		// 서버에 전송
		stompClient.send('/app/chat.adminReply', {}, JSON.stringify(adminMessage));
		input.value = '';

		// 방 목록 업데이트
		updateRoomInList(currentRoomId, adminMessage);
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

// 페이지 로드 시 실행
window.addEventListener('load', async function() {
	console.log('페이지 로드 시작');

	// 먼저 기존 데이터 로드
	await loadExistingData();

	// 그 다음 웹소켓 연결
	connect();
});

// 페이지 언로드 시 연결 해제
window.addEventListener('beforeunload', function() {
	if (stompClient) {
		// 모든 구독 해제
		if (adminSubscription) {
			adminSubscription.unsubscribe();
		}
		roomSubscriptions.forEach(subscription => subscription.unsubscribe());
		roomSubscriptions.clear();
		
		stompClient.disconnect();
	}
});