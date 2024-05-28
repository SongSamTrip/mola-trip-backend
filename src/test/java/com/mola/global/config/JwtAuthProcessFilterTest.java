package com.mola.global.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mola.global.auth.JwtAuthProcessFilter;
import com.mola.global.auth.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtAuthProcessFilterTest {

    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    JwtAuthProcessFilter jwtAuthProcessFilter;

    @Mock
    MockHttpServletRequest request;

    @Mock
    MockHttpServletResponse response;

    @Mock
    MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    @DisplayName("토큰이 유효할 경우 사용자 인증 성공")
    public void whenTokenIsValidThenAuthenticate() throws Exception {
        // given
        String token = "validToken";
        Long memberId = 123L;
        UserDetails userDetails = mock(UserDetails.class);

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtProvider.verifyToken(token)).thenReturn(true);
        when(jwtProvider.extractMemberIdFromToken(token)).thenReturn(memberId);
        when(jwtProvider.createUserDetails(memberId, null)).thenReturn(userDetails);

        // when
        jwtAuthProcessFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtProvider).verifyToken(token);
        verify(jwtProvider).createUserDetails(memberId, null);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("토큰이 유효하지 않을 경우 인증 실패")
    public void whenTokenIsInvalidThenAuthenticationFails() throws Exception {
        // given
        String token = "invalidToken";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtProvider.verifyToken(token)).thenReturn(false);

        // when
        jwtAuthProcessFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtProvider).verifyToken(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}