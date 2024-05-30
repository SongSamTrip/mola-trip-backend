package com.mola.domain.tripBoard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mola.common.TestConfig;
import com.mola.domain.tripBoard.tripPost.controller.TripPostController;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestConfig.class)
@WebMvcTest(TripPostController.class)
class TripPostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TripPostService tripPostService;

    Page<TripPostListResponseDto> dtoPage;

    TripPostResponseDto tripPostResponseDto;

    @BeforeEach
    void setUp() {
        List<TripPostListResponseDto> dtoList = new ArrayList<>();
        LongStream.range(1, 11).forEach(i -> {
            TripPostListResponseDto dto = TripPostListResponseDto.builder()
                    .id(i)
                    .name("test")
                    .writer("test")
                    .imageUrl("test image")
                    .tripPostStatus(TripPostStatus.PUBLIC)
                    .createdDate(LocalDateTime.of(2024, 05, 28, 12, 0, 0))
                    .build();
            dtoList.add(dto);
        });
        dtoPage = new PageImpl<>(dtoList);
    }


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
                .memberId(1L)
                .name("name")
                .content("content")
                .tripPlanId(1L)
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

    @DisplayName("게시글을 식별자로 조회")
    @WithMockUser
    @Test
    void getTripPost() throws Exception {
        // given
        tripPostResponseDto = TripPostResponseDto.builder()
                .id(1L)
                .memberId(1L)
                .nickname("test nickname")
                .name("test name")
                .content("test content")
                .tripPostStatus(TripPostStatus.PUBLIC)
                .tripName("test tripName")
                .tripId(1L)
                .mainList("test mainList")
                .createdDate(LocalDateTime.of(2024, 05, 28, 12, 0, 0,0))
                .build();
        when(tripPostService.getTripPostResponseDto(1L)).thenReturn(tripPostResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(get("/tripPosts/{id}", "1")
                .with(csrf()));

        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("게시글 리스트를 조회")
    @WithMockUser
    @Test
    void getTripPosts_success() throws Exception {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(tripPostService.getAllPublicTripPosts(pageRequest)).thenReturn(dtoPage);

        // when
        ResultActions resultActions = mockMvc.perform(get("/tripPosts")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(10));
    }

    @DisplayName("작성자가 작성한 게시글 리스트를 조회")
    @WithMockUser
    @Test
    void getMyTripPosts_success() throws Exception {
        // given
        dtoPage.forEach(dto -> {
            dto.setTripPostStatus(TripPostStatus.PRIVATE);
        });
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(tripPostService.getAllMyPosts(pageRequest)).thenReturn(dtoPage);

        // when
        ResultActions resultActions = mockMvc.perform(get("/tripPosts/myPosts")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(10));
    }

    @DisplayName("게시글을 삭제하면 200 상태코드를 반환")
    @WithMockUser
    @Test
    void deleteTripPost() throws Exception {
        // given
        doNothing().when(tripPostService).deleteTripPost(anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/tripPosts/{id}", "1")
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("게시글 좋아요 요청은 200 상태코드를 반환")
    @WithMockUser
    @Test
    void addLike_success() throws Exception {
        // given
        String VALID_ID = "1";
        doNothing().when(tripPostService).addLikes(anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(post("/tripPosts/{id}/likes", VALID_ID)
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("게시글 좋아요 요청시 동시성 충돌이 발생하면 500 상태코드를 반환")
    @WithMockUser
    @Test
    void addLike_fail() throws Exception {
        // given
        String VALID_ID = "1";
        doThrow(new CustomException(GlobalErrorCode.ExcessiveRetries)).when(tripPostService).addLikes(anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(post("/tripPosts/{id}/likes", VALID_ID)
                .with(csrf()));

        // then
        resultActions.andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.errorMessage").value("잠시 후 다시 시도해주세요."));
    }


    @DisplayName("게시글 좋아요 취소 요청은 200 상태코드를 반환")
    @WithMockUser
    @Test
    void removeLike_success() throws Exception {
        // given
        String VALID_ID = "1";
        doNothing().when(tripPostService).removeLikes(anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/tripPosts/{id}/likes", VALID_ID)
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("게시글 좋아요 취소 요청시 동시성 충돌이 발생하면 500 상태코드를 반환")
    @WithMockUser
    @Test
    void removeLike_fail() throws Exception {
        // given
        String VALID_ID = "1";
        doThrow(new CustomException(GlobalErrorCode.ExcessiveRetries)).when(tripPostService).removeLikes(anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/tripPosts/{id}/likes", VALID_ID)
                .with(csrf()));

        // then
        resultActions.andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.errorMessage").value("잠시 후 다시 시도해주세요."));
    }

    @DisplayName("관리자는 모든 게시글을 조회")
    @WithMockUser(roles = "ADMIN")
    @Test
    void getAdminTripPosts() throws Exception {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(tripPostService.adminGetAllPosts(pageRequest)).thenReturn(dtoPage);

        // when
        ResultActions resultActions = mockMvc.perform(get("/tripPosts/admin")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(10));
    }

    @DisplayName("관리자는 어떤 게시글이든 삭제를 할 수 있다")
    @WithMockUser(roles = "ADMIN")
    @Test
    void deleteAdminTripPosts() throws Exception {
        // given
        String VALID_ID = "1";
        doNothing().when(tripPostService).deleteAdminTripPost(anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/tripPosts/{id}/admin", VALID_ID)
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk());
    }



}