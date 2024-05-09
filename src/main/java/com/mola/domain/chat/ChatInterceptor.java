package com.mola.domain.chat;

import com.mola.domain.tripFriends.TripFriendsService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.ErrorCode;
import com.mola.global.security.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
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
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        if(requiresAuthentication(command)){
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
        System.out.println(authentication.toString());
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            throw new CustomException(ErrorCode.InvalidMemberIdentifierFormat);
        }
        return Long.valueOf(((UserDetails) authentication.getPrincipal()).getUsername());
    }

    private static void verifyDestination(String destination) {
        if (destination == null || (!destination.startsWith("/sub/") && !destination.startsWith("/pub/"))) {
            throw new CustomException(ErrorCode.InvalidDestination);
        }
    }

    private static Long parseTripPlanId(String destination) {
        try {
            String[] parts = destination.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
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