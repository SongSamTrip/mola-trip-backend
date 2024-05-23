package com.mola.domain.chat;

import com.mola.domain.chat.common.ChatInterceptor;
import com.mola.global.auth.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ChatInterceptorTest {

    private static final String VALID_TOKEN = "1";
    private static final String INVALID_TOKEN = "2";

    @Mock
    MessageChannel messageChannel;
    @Mock
    JwtProvider jwtProvider;
    @InjectMocks
    ChatInterceptor chatInterceptor;

    @DisplayName("정상적인 메시지 요청은 정상 흐름")
    @Test
    void whenMessageWithAuthentication_success() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setDestination("/sub/chat/" + VALID_TOKEN);
        accessor.setNativeHeader("Authorization", "Bearer " + VALID_TOKEN);
        Message message = new GenericMessage(new byte[0], accessor.toMap());

        doReturn(true).when(jwtProvider).verifyToken(VALID_TOKEN);

        // expect
        assertDoesNotThrow(() -> chatInterceptor.preSend(message, messageChannel));
    }

    @DisplayName("사용자 식별자 정보가 잘못된 메시지 요청은 에러를 반환")
    @Test
    void whenMessageWithInvalidAuthentication_fail() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + INVALID_TOKEN);
        Message message = new GenericMessage(new byte[0], accessor.toMap());

        doReturn(false).when(jwtProvider).verifyToken(INVALID_TOKEN);


        // expect
        assertThrows(MessageDeliveryException.class, () -> chatInterceptor.preSend(message, messageChannel));
    }
}