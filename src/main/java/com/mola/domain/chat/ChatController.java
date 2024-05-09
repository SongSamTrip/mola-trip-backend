package com.mola.domain.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate template;

    @MessageMapping(value = "/chat/{id}")
    public void sendMessage(@DestinationVariable("id") String tripPlanId, String message) {
        ChatMessage chatMessage = chatMessageService.saveMessage(createChatMessage(tripPlanId, message));
        template.convertAndSend("/sub/chat/" + tripPlanId, chatMessage);
    }

    @GetMapping(value = "/chatMessage/{id}")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable("id") Long tripPlanId) {
        return ResponseEntity.ok(chatMessageService.getMessages(tripPlanId));
    }

    @GetMapping

    private static ChatMessage createChatMessage(String tripPlanId, String message) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ChatMessage chatMessage = ChatMessage.builder()
                .memberId(Long.valueOf(userDetails.getUsername()))
                .tripPlanId(Long.valueOf(tripPlanId))
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();
        return chatMessage;
    }
}
