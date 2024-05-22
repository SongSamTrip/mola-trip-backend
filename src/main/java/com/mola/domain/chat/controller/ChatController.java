package com.mola.domain.chat.controller;

import com.mola.domain.chat.entity.ChatMessage;
import com.mola.domain.chat.service.ChatMessageService;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate template;

    @MessageMapping(value = "/chat/{id}")
    public void sendMessage(@DestinationVariable("id") String tripPlanId, String message) {
        ChatMessage chatMessage = chatMessageService.saveMessage(createChatMessage(tripPlanId, message));
        template.convertAndSend("/sub/chat/" + tripPlanId, chatMessage);
    }

    @GetMapping(value = "/chatMessage/{id}")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable("id") Long tripPlanId) {
        return ResponseEntity.ok(chatMessageService.getMessages(tripPlanId));
    }

    @GetMapping

    private ChatMessage createChatMessage(String tripPlanId, String message) {
        Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.AccessDenied));

        ChatMessage chatMessage = ChatMessage.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .tripPlanId(Long.valueOf(tripPlanId))
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();
        return chatMessage;
    }
}
