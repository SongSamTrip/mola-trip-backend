package com.mola.domain.tripBoard.controller;

import com.mola.domain.tripBoard.service.TripPostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(TripPostController.class)
class TripPostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TripPostService tripPostService;

    @DisplayName("임시 게시글을 생성")
    @WithMockUser
    @Test
    void createDraftTripPost_success() throws Exception{
        // when
        mockMvc.perform(post("/tripPosts/draft")
                .with(csrf()))
                .andExpect(status().isCreated());

        // then
        verify(tripPostService, times(1)).createDraftTripPost();
    }

}