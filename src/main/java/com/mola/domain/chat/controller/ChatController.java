package com.mola.domain.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mola.domain.chat.dto.ChatMessageDto;
import com.mola.domain.chat.entity.ChatMessage;
import com.mola.domain.chat.exception.StompError;
import com.mola.domain.chat.service.ChatMessageService;
import com.mola.domain.tripFriends.TripFriendsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final ObjectMapper objectMapper;
    private final TripFriendsService tripFriendsService;
    private final SimpMessagingTemplate template;

    @MessageMapping(value = "/chat/{id}")
    public void sendMessage(@DestinationVariable("id") String tripPlanId, String message) throws JsonProcessingException {
        ChatMessageDto chatMessageDto = objectMapper.readValue(message, ChatMessageDto.class);
        verifyTripFriends(chatMessageDto.getMemberId(), Long.valueOf(tripPlanId));

        ChatMessage chatMessage = chatMessageService.saveMessage(createChatMessage(tripPlanId, chatMessageDto));
        template.convertAndSend("/sub/chat/" + tripPlanId, objectMapper.writeValueAsString(chatMessage));
    }

    @GetMapping(value = "/chatMessage/{id}")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable("id") Long tripPlanId) {
        return ResponseEntity.ok(chatMessageService.getMessages(tripPlanId));
    }

    private void verifyTripFriends(Long memberId, Long tripPlanId) {
        if (!tripFriendsService.existsByMemberAndTripPlan(memberId, tripPlanId)) {
            throw new MessageDeliveryException(StompError.INVALID.name());
        }
    }

    private ChatMessage createChatMessage(String tripPlanId, ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = ChatMessage.builder()
                .memberId(chatMessageDto.getMemberId())
                .nickname(chatMessageDto.getNickname())
                .tripPlanId(Long.valueOf(tripPlanId))
                .content(chatMessageDto.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        chatMessageService.saveMessage(chatMessage);
        return chatMessage;
    }
}
