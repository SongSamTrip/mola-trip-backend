package com.mola.domain.chat;

import com.mola.domain.chat.entity.ChatMessage;
import com.mola.domain.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatMessageRepositoryTest {

    private static final Long VALID_MEMBER_ID = 1L;
    private static final Long VALID_TRIP_PLAN_ID = 1L;
    private static final int MESSAGE_LIMIT = 10;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @DisplayName("TripPlan에 속한 채팅 내역을 반환")
    @Test
    void getChatMessage_byTripPlanId() {
        // given
        IntStream.range(0, MESSAGE_LIMIT).forEach(i -> {
            chatMessageRepository.save(createChatMessage(VALID_MEMBER_ID, VALID_TRIP_PLAN_ID));
        });

        // when
        List<ChatMessage> byTripPlanId = chatMessageRepository.findByTripPlanId(VALID_TRIP_PLAN_ID);

        // then
        assertEquals(byTripPlanId.size(), MESSAGE_LIMIT);
        byTripPlanId.forEach(chatMessage -> assertEquals(chatMessage.getTripPlanId(), VALID_TRIP_PLAN_ID));
    }


    private ChatMessage createChatMessage(Long memberId, Long tripPlanId){
        return ChatMessage.builder()
                .memberId(memberId)
                .tripPlanId(tripPlanId)
                .content("test")
                .build();
    }

}