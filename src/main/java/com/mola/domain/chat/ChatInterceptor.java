package com.mola.domain.chat;

import com.mola.domain.tripFriends.TripFriendsService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
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
            throw new CustomException(ErrorCode.InvalidTripFriends);
        }
    }

    private Long validateAndGetMemberId(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Long)) {
            throw new CustomException(ErrorCode.InvalidMemberIdentifierFormat);
        }
        return (Long) authentication.getPrincipal();
    }

    private static void verifyDestination(String destination) {
        if (destination == null || (!destination.startsWith("/sub/") && !destination.startsWith("/pub/"))) {
            throw new CustomException(ErrorCode.InvalidDestination);
        }
    }

    private static Long parseTripPlanId(String destination) {
        try {
            String[] parts = destination.split("/");
            if (parts.length < 2) {
                throw new CustomException(ErrorCode.MissingTripPlanIdentifier);
            }
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.InvalidTripPlanIdentifierFormat);
        }
    }

    private static Authentication getAndVerifyAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UnAuthorized);
        }
        return authentication;
    }
}