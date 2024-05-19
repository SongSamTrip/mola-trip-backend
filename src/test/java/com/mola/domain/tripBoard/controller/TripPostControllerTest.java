package com.mola.domain.tripBoard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mola.domain.tripBoard.tripImage.dto.TripImageDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostUpdateDto;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.domain.tripBoard.tripPost.controller.TripPostController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(TripPostController.class)
class TripPostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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

    @DisplayName("필드값들이 정상적이라면 save 를 호출")
    @WithMockUser
    @Test
    void whenValidAllOfFiledChangeStatusToPublic() throws Exception {
        // given
        TripPostDto tripPostDto = TripPostDto.builder()
                .id(1L)
                .name("name")
                .content("content")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/tripPosts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripPostDto))
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk());
        verify(tripPostService, times(1)).save(any());
    }

    @DisplayName("필드값들이 조건에 맞지 않다면 save 를 호출하지 않음")
    @WithMockUser
    @Test
    void whenValidAllOfFiledChangeStatusToPublic_fail() throws Exception {
        // given
        TripPostDto tripPostDto = TripPostDto.builder()
                .id(1L)
                .name(null)
                .content(null)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/tripPosts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripPostDto))
                .with(csrf()));

        // then
        resultActions.andExpect(status().isBadRequest());
        verify(tripPostService, never()).save(any());
    }

    @DisplayName("필드값들이 조건에 맞지 않다면 update 를 호출하지 않음")
    @WithMockUser
    @Test
    void callUpdate_success() throws Exception {
        // given
        List<TripImageDto> tripImageList = new ArrayList<>();
        LongStream.range(1, 11).forEach(i -> {
            tripImageList.add(new TripImageDto(i, "test", 1L));
        });

        var updateDto = TripPostUpdateDto.builder()
                .id(1L)
                .name("test")
                .content("test")
                .tripImageList(tripImageList)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(put("/tripPosts/{id}", updateDto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk());


    }

}