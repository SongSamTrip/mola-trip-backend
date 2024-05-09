package com.mola.domain.chat;

import com.mola.domain.tripFriends.TripFriendsService;
import com.mola.global.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatInterceptorTest {

    private static final String VALID_ID = "1";
    private static final String INVALID_ID = "2";

    @Mock
    MessageChannel messageChannel;
    @Mock
    TripFriendsService tripFriendsService;
    @Mock
    SecurityContext securityContext;
    @InjectMocks
    ChatInterceptor chatInterceptor;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("정상적인 메시지 요청은 정상 흐름")
    @Test
    void whenMessageWithAuthentication_success() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/sub/chat/" + VALID_ID);
        Message message = new GenericMessage(new byte[0], accessor.toMap());
        when(securityContext.getAuthentication()).thenReturn(createAuthentication(VALID_ID));
        when(tripFriendsService.existsByMemberAndTripPlan(anyLong(), anyLong())).thenReturn(true);

        // expect
        assertDoesNotThrow(() -> chatInterceptor.preSend(message, messageChannel));
    }

    @DisplayName("사용자 식별자 정보가 잘못된 메시지 요청은 에러를 반환")
    @Test
    void whenMessageWithInvalidAuthentication_fail() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/sub/" + VALID_ID);
        Message message = new GenericMessage(new byte[0], accessor.toMap());
        Authentication auth = createAuthentication(INVALID_ID);
        when(securityContext.getAuthentication()).thenReturn(auth);

        // expect
        assertThrows(CustomException.class, () -> chatInterceptor.preSend(message, messageChannel));
    }

    @DisplayName("사용자 정보가 없는 메시지 요청은 에러를 반환")
    @Test
    void whenMessageWithoutAuthentication_fail() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/sub/" + VALID_ID);
        Message message = new GenericMessage(new byte[0], accessor.toMap());
        when(securityContext.getAuthentication()).thenReturn(null);

        // expect
        assertThrows(CustomException.class, () -> chatInterceptor.preSend(message, messageChannel));
    }

    @DisplayName("목적지 경로가 잘못된 메시지는 에러를 반환")
    @Test
    void whenMessageWithInvalidDestination_fail() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/wrong");
        Message message = new GenericMessage(new byte[0], accessor.toMap());
        when(securityContext.getAuthentication()).thenReturn(createAuthentication(VALID_ID));

        // expect
        assertThrows(CustomException.class, () -> chatInterceptor.preSend(message, messageChannel));
    }

    @DisplayName("목적지 경로가 없는 메시지는 에러를 반환")
    @Test
    void whenMessageWithoutDestination_fail() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        Message message = new GenericMessage(new byte[0], accessor.toMap());
        when(securityContext.getAuthentication()).thenReturn(createAuthentication(VALID_ID));

        // expect
        assertThrows(CustomException.class, () -> chatInterceptor.preSend(message, messageChannel));
    }


    @DisplayName("목적지 경로의 식별자가 잘못된 메시지는 에러를 반환")
    @Test
    void whenMessageWithInValidIdentifier_fail() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/sub/wrong");
        Message message = new GenericMessage(new byte[0], accessor.toMap());
        when(securityContext.getAuthentication()).thenReturn(createAuthentication(VALID_ID));

        // expect
        assertThrows(CustomException.class, () -> chatInterceptor.preSend(message, messageChannel));
    }

    @DisplayName("회원이 속한 여행플랜이 아닌 메시지는 에러를 반환")
    @Test
    void whenMessageWithInValidTripPlan_fail() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/sub/" + VALID_ID);
        Message message = new GenericMessage(new byte[0], accessor.toMap());
        when(securityContext.getAuthentication()).thenReturn(createAuthentication(INVALID_ID));
        when(tripFriendsService.existsByMemberAndTripPlan(anyLong(), anyLong())).thenReturn(false);

        // expect
        assertThrows(CustomException.class, () -> chatInterceptor.preSend(message, messageChannel));
    }

    private Authentication createAuthentication(String username) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username,
                "password",
                true,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}