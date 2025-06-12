package com.mogatshoo.dev.admin.qanda.controller;

import com.mogatshoo.dev.admin.qanda.entity.QandAEntity;
import com.mogatshoo.dev.admin.qanda.repository.QandARepository;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/qanda")
public class QandAController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private QandARepository chatMessageRepository;
    
    @Autowired
    private MemberService memberService;

    // WebSocket 메시지 핸들러들 (기존)
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload QandAEntity qandAMessage) {
        qandAMessage.setIsRead(false);
        qandAMessage.setCreatedAt(LocalDateTime.now());

        chatMessageRepository.save(qandAMessage);

        messagingTemplate.convertAndSend("/topic/admin", qandAMessage);
        messagingTemplate.convertAndSend("/topic/room." + qandAMessage.getRoomId(), qandAMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload QandAEntity qandAMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", qandAMessage.getSender());
        headerAccessor.getSessionAttributes().put("roomId", qandAMessage.getRoomId());

        qandAMessage.setType(QandAEntity.MessageType.JOIN);
        qandAMessage.setContent(qandAMessage.getSender() + "님이 채팅에 참여했습니다.");
        qandAMessage.setIsRead(false);
        qandAMessage.setCreatedAt(LocalDateTime.now());

        chatMessageRepository.save(qandAMessage);
        messagingTemplate.convertAndSend("/topic/admin", qandAMessage);
    }

    @MessageMapping("/chat.adminReply")
    public void adminReply(@Payload QandAEntity qandAMessage) {
        qandAMessage.setType(QandAEntity.MessageType.CHAT);
        qandAMessage.setSender("관리자");
        qandAMessage.setIsRead(true);
        qandAMessage.setCreatedAt(LocalDateTime.now());

        chatMessageRepository.save(qandAMessage);
        
        // 🔥 수정: 사용자 + 모든 관리자에게 전달
        messagingTemplate.convertAndSend("/topic/room." + qandAMessage.getRoomId(), qandAMessage);
        messagingTemplate.convertAndSend("/topic/admin", qandAMessage);  // 추가!
    }

    @MessageMapping("/chat.markAsRead")
    public void markAsRead(@Payload String roomId) {
        List<QandAEntity> unreadMessages = chatMessageRepository.findByRoomIdAndIsReadFalse(roomId);
        for (QandAEntity message : unreadMessages) {
            message.setIsRead(true);
        }
        chatMessageRepository.saveAll(unreadMessages);
    }

    // REST API들 (새로 추가)
    
    // 활성 채팅방 목록 조회
    @GetMapping("/api/rooms")
    @ResponseBody
    public List<String> getActiveRooms() {
        return chatMessageRepository.findActiveRooms();
    }

    // 특정 채팅방의 메시지들 조회
    @GetMapping("/api/rooms/{roomId}/messages")
    @ResponseBody
    public List<QandAEntity> getRoomMessages(@PathVariable("roomId") String roomId) {
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    // 각 채팅방별 읽지 않은 메시지 개수 조회
    @GetMapping("/api/rooms/unread")
    @ResponseBody
    public Map<String, Long> getUnreadCounts() {
        List<Object[]> results = chatMessageRepository.countUnreadMessagesByRoom();
        Map<String, Long> unreadCounts = new HashMap<>();
        
        for (Object[] result : results) {
            String roomId = (String) result[0];
            Long count = (Long) result[1];
            unreadCounts.put(roomId, count);
        }
        
        return unreadCounts;
    }

    @GetMapping("/api/rooms/summary")
    @ResponseBody
    public List<Map<String, Object>> getRoomsSummary() {
        List<String> activeRooms = chatMessageRepository.findActiveRooms();
        List<Map<String, Object>> roomsSummary = new ArrayList<>();
        
        for (String roomId : activeRooms) { // roomId = memberId
            List<QandAEntity> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
            if (!messages.isEmpty()) {
                QandAEntity lastMessage = messages.get(messages.size() - 1);
                
                // 🔥 회원 정보 조회 추가
                MemberEntity member = memberService.findByMemberId(roomId);
                
                List<QandAEntity> unreadMessages = chatMessageRepository.findByRoomIdAndIsReadFalse(roomId);
                Long unreadCount = (long) unreadMessages.size();
                
                Map<String, Object> roomInfo = new HashMap<>();
                roomInfo.put("roomId", roomId);
                roomInfo.put("memberId", roomId);
                
                // 🔥 실제 회원 정보 추가
                if (member != null) {
                    roomInfo.put("memberName", member.getMemberName());
                    roomInfo.put("memberNickName", member.getMemberNickName());
                } else {
                    roomInfo.put("memberName", roomId);
                    roomInfo.put("memberNickName", roomId + "님");
                }
                
                roomInfo.put("lastMessage", lastMessage.getContent());
                roomInfo.put("lastTime", lastMessage.getCreatedAt());
                roomInfo.put("unreadCount", unreadCount);
                
                roomsSummary.add(roomInfo);
            }
        }
        
        return roomsSummary;
    }

    @GetMapping("/api/user/{username}/rooms")
    @ResponseBody
    public List<Map<String, Object>> getUserRooms(@PathVariable("username") String username) {
        List<String> allRooms = chatMessageRepository.findActiveRooms();
        List<Map<String, Object>> userRooms = new ArrayList<>();
        
        for (String roomId : allRooms) {
            if (roomId.equals(username)) { // 🔥 단순 비교로 변경
                List<QandAEntity> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
                if (!messages.isEmpty()) {
                    QandAEntity lastMessage = messages.get(messages.size() - 1);
                    
                    Map<String, Object> roomInfo = new HashMap<>();
                    roomInfo.put("roomId", roomId);
                    roomInfo.put("lastTime", lastMessage.getCreatedAt());
                    roomInfo.put("messageCount", messages.size());
                    
                    userRooms.add(roomInfo);
                }
            }
        }
        
        return userRooms;
    }

    // 사용자 정보 조회 API
    @GetMapping("/api/user/info")
    @ResponseBody
    public Map<String, Object> getUserInfo(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();
        
        if (authentication != null) {
            String memberId = authentication.getName();
            MemberEntity member = memberService.findByMemberId(memberId);
            
            if (member != null) {
                userInfo.put("memberId", memberId);
                userInfo.put("nickname", member.getMemberNickName());
                userInfo.put("memberName", member.getMemberName());
            } else {
                userInfo.put("memberId", memberId);
                userInfo.put("nickname", memberId); // 회원 정보가 없으면 ID 사용
            }
        } else {
            userInfo.put("memberId", "guest");
            userInfo.put("nickname", "guest");
        }
        
        return userInfo;
    }

    @GetMapping("/admin")
    public String adminQandA() {
        return "admin/qanda/admin-qanda";
    }
    @GetMapping("/user")
    public String userQandA() {
        return "qanda/user-qanda";
    }
}