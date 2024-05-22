package com.mola.domain.member.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;


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



}