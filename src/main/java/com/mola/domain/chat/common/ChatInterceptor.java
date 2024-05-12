package com.mola.domain.chat.common;

import com.mola.domain.chat.exception.StompError;
import com.mola.domain.tripFriends.TripFriendsService;
import com.mola.global.security.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatInterceptor implements ChannelInterceptor {

    private final TripFriendsService tripFriendsService;
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        String authorization = accessor.getFirstNativeHeader("Authorization");

        if(requiresAuthentication(command)){
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);

                if (jwtProvider.verifyToken(token)) {
                    Long memberId = jwtProvider.extractMemberIdFromToken(token);
                    UserDetails user = jwtProvider.createUserDetails(memberId, "ROLE_USER");

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            user, "", user.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }else {
                throw new MessageDeliveryException(StompError.UNAUTHORIZED.name());
            }
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
            throw new MessageDeliveryException(StompError.INVALID.name());
        }
    }

    private Long validateAndGetMemberId(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            throw new MessageDeliveryException(StompError.UNAUTHORIZED.name());
        }
        return Long.valueOf(((UserDetails) authentication.getPrincipal()).getUsername());
    }

    private static void verifyDestination(String destination) {
        if (destination == null || (!destination.startsWith("/sub/") && !destination.startsWith("/pub/"))) {
            throw new MessageDeliveryException(StompError.INVALID.name());
        }
    }

    private static Long parseTripPlanId(String destination) {
        try {
            String[] parts = destination.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageDeliveryException(StompError.INVALID.name());
        }
    }

    private static Authentication getAndVerifyAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new MessageDeliveryException(StompError.UNAUTHORIZED.name());
        }
        return authentication;
    }
}