package com.mola.domain.chat;

import com.mola.domain.tripFriends.TripFriendsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatInterceptor implements ChannelInterceptor {

    private final TripFriendsService tripFriendsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (requiresAuthentication(command)){
            verifyIsValidMessage(accessor);
        }

        return message;
    }

    private boolean requiresAuthentication(StompCommand command) {
        return StompCommand.SUBSCRIBE.equals(command) ||
                StompCommand.SEND.equals(command) ||
                StompCommand.MESSAGE.equals(command) ||
                StompCommand.UNSUBSCRIBE.equals(command);
    }

    private void verifyIsValidMessage(StompHeaderAccessor accessor) {
        Authentication authentication = getAndVerifyAuthentication();
        Long memberId = validateAndGetMemberId(authentication);

        String destination = accessor.getDestination();
        verifyDestination(destination);

        Long tripPlanId = parseTripPlanId(destination);
        verifyTripFriends(memberId, tripPlanId);
    }

    private void verifyTripFriends(Long memberId, Long tripPlanId) {
        if (!tripFriendsService.existsByMemberAndTripPlan(memberId, tripPlanId)) {
            throw new AccessDeniedException("유효하지 않은 채팅 요청입니다.");
        }
    }

    private Long validateAndGetMemberId(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Long)) {
            throw new IllegalArgumentException("잘못된 회원 식별자 형식입니다.");
        }
        return (Long) authentication.getPrincipal();
    }

    private static void verifyDestination(String destination) {
        if (destination == null || (!destination.startsWith("/sub/") && !destination.startsWith("/pub/"))) {
            throw new IllegalArgumentException("잘못된 목적지 형식입니다.");
        }
    }

    private static Long parseTripPlanId(String destination) {
        try {
            String[] parts = destination.split("/");
            if (parts.length < 2) {
                throw new IllegalArgumentException("식별자 값이 존재하지 않습니다.");
            }
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 식별자 형식입니다.", e);
        }
    }

    private static Authentication getAndVerifyAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        return authentication;
    }
}