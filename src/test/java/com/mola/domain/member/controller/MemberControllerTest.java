package com.mola.domain.member.controller;


import com.mola.common.TestConfig;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mola.domain.member.service.MemberService;
import com.mola.global.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Import(TestConfig.class)
@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemberService memberService;

    @MockBean
    SecurityUtil securityUtil;

    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자 권한이 있다면 200 코드를 반환")
    @Test
    void callAdminApi_success() throws Exception {
        // expected
        mockMvc.perform(get("/api/members/admin"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "USER")
    @DisplayName("관리자 권한이 없다면 403 코드를 반환")
    @Test
    void callAdminApi_fail() throws Exception {
        // expected
        mockMvc.perform(get("/api/members/admin"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "USER")
    @DisplayName("관리자 권한이 없다면 403 코드를 반환")
    @Test
    void adminDeleteUser_fail() throws Exception {
        Long memberId = 1L;
        // expected
        mockMvc.perform(delete("/api/members/admin/{id}", memberId))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자 권한이 있다면 200 코드를 반환")
    @Test
    void adminDeleteUser_success() throws Exception {
        Long memberId = 1L;
        doNothing().when(memberService).adminDeleteMember(memberId);

        // expected
        mockMvc.perform(delete("/api/members/admin/{id}", memberId).with(csrf()))
                .andExpect(status().isOk());
    }

    @DisplayName("관리자 변환 api")
    @Test
    void requestAdmin_success() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("user123")
                .password("password123")
                .roles("USER")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, new NullAuthoritiesMapper().mapAuthorities(userDetails.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        String secretKey = "1234";
        when(memberService.requestAdmin(secretKey)).thenReturn(1L);
        when(securityUtil.getAuthenticatedUser()).thenReturn(userDetails);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/members/admin")
                        .param("secretKey", secretKey)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
