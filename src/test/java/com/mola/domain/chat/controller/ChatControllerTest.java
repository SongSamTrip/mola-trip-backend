package com.mola.domain.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mola.domain.chat.dto.ChatMessageDto;
import com.mola.domain.chat.entity.ChatMessage;
import com.mola.domain.chat.service.ChatMessageService;
import com.mola.domain.tripFriends.TripFriendsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TripFriendsService tripFriendsService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private ChatController chatController;

    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 0L;

    private String message = "test message";


    @DisplayName("유효한 여행플랜의 채팅방이라면 메시지를 전송")
    @Test
    void sendMessage() throws JsonProcessingException {
        // given
        ChatMessageDto chatMessageDto = new ChatMessageDto(message, 1L, "nickname");
        when(objectMapper.readValue(message, ChatMessageDto.class)).thenReturn(chatMessageDto);
        when(tripFriendsService.existsByMemberAndTripPlan(VALID_ID, VALID_ID)).thenReturn(true);
        when(chatMessageService.saveMessage(any(ChatMessage.class))).thenReturn(new ChatMessage());
        when(objectMapper.writeValueAsString(any(ChatMessage.class))).thenReturn(message);

        // when
        chatController.sendMessage(String.valueOf(VALID_ID), message);

        // then
        verify(simpMessagingTemplate, times(1)).convertAndSend(anyString(), anyString());
    }

    @DisplayName("유효하지 않은 여행플랜의 채팅플랜이라면 에러 발생")
    @Test
    void sendMessage_fail() throws JsonProcessingException {
        // given
        Long memberId = 0L;
        String tripPlanId = String.valueOf(0L);

        ChatMessageDto chatMessageDto = new ChatMessageDto(message, memberId, "nickname");
        when(objectMapper.readValue(message, ChatMessageDto.class)).thenReturn(chatMessageDto);
        when(tripFriendsService.existsByMemberAndTripPlan(memberId, Long.valueOf(tripPlanId))).thenReturn(false);

        // when & then
        assertThrows(MessageDeliveryException.class, () -> {
            chatController.sendMessage(tripPlanId, message);
        });
        verify(chatMessageService, never()).saveMessage(any(ChatMessage.class));
    }

    @DisplayName("여행플랜의 이전 채팅내역을 조회")
    @Test
    void getChatMessage() throws JsonProcessingException {
        // given
        when(chatMessageService.getMessages(VALID_ID)).thenReturn(new ArrayList<ChatMessage>());

        // when
        ResponseEntity<java.util.List<ChatMessage>> chatMessages =
                chatController.getChatMessages(VALID_ID);

        // then
        assertTrue(chatMessages.getStatusCode().is2xxSuccessful());
    }


}