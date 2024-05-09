package com.mola.domain.tripFriends.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.global.security.service.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ChatControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    MemberRepository memberRepository;
    String url;
    WebSocketStompClient stompClient;
    StompSession stompSession;
    StompSessionHandlerAdapter stompSessionHandlerAdapter;

    @BeforeEach
    void setup() throws Exception {
        this.url = String.format("ws://localhost:%d/ws",port);
        setStompClient();

        Member member = Member.builder()
                .nickname("test")
                .personalId("1")
                .build();
        Member save = memberRepository.save(member);
        String accessToken = jwtProvider.createAccessToken(save.getId());

        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);

        this.stompSession = stompClient.connectAsync(url, httpHeaders, stompSessionHandlerAdapter).get();
    }


    @Test
    void subscribe() {
        this.stompSession.subscribe("sub", stompSessionHandlerAdapter);
    }

    private void setStompClient() {
        this.stompClient = new WebSocketStompClient(new SockJsClient(createTransport()));
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.stompClient.setMessageConverter(messageConverter);
        this.stompSessionHandlerAdapter = new StompSessionHandlerAdapter() {};
    }


    private List<Transport> createTransport() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

}