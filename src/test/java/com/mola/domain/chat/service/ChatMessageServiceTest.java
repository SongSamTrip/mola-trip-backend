package com.mola.domain.chat.service;

import com.mola.domain.chat.entity.ChatMessage;
import com.mola.domain.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    ChatMessageRepository chatMessageRepository;

    @InjectMocks
    ChatMessageService chatMessageService;

    @DisplayName("채팅 메시지가 저장된다")
    @Test
    void saveMessage() {
        // given
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(1L);
        when(chatMessageRepository.save(chatMessage)).thenReturn(chatMessage);

        // when
        ChatMessage save = chatMessageService.saveMessage(chatMessage);

        // then
        assertThat(save.getId()).isNotNull();
    }

    @DisplayName("채팅 메시지를 조회한다")
    @Test
    void getMessage() {
        // given
        Long VALID_ID = 1L;
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTripPlanId(VALID_ID);
        when(chatMessageRepository.findByTripPlanId(anyLong())).thenReturn(List.of(chatMessage));

        // when
        List<ChatMessage> byTripPlanId = chatMessageService.getMessages(VALID_ID);

        // then
        assertThat(byTripPlanId).isNotNull();
        assertThat(byTripPlanId).hasSize(1);
    }
}