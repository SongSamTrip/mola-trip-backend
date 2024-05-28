package com.mola.domain.member.controller;

import com.mola.domain.member.service.MemberService;
import com.mola.global.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(MemberControllerTest.TestConfig.class)
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


    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestConfig {
    }

}
