package com.mola.domain.tripBoard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.tripBoard.comment.controller.CommentController;
import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.comment.service.CommentService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CommentService commentService;

    @DisplayName("댓글 전체 조회 정상 호출")
    @WithMockUser
    @Test
    void getComment() throws Exception {
        // given
        List<CommentDto> commentDtoList = new ArrayList<>();
        LongStream.range(1, 10).forEach(i -> {
            commentDtoList.add(new CommentDto());
        });
        Page<CommentDto> comments = new PageImpl<>(commentDtoList);
        doReturn(comments).when(commentService).getAllComments(anyLong(), any());

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/tripPosts/{tripPostId}/comments", 1)
                .param("pageNo", "1")
                .param("pageSize", "10")
                .with(csrf()));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk());
        verify(commentService, times(1)).getAllComments(anyLong(), any());
    }

    @DisplayName("댓글 저장 정상 호출")
    @WithMockUser
    @Test
    void saveComment() throws Exception {
        // given
        MemberTripPostDto memberTripPostDto = new MemberTripPostDto(1L, "name");
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("hello");
        commentDto.setMemberTripPostDto(memberTripPostDto);

        doReturn(commentDto).when(commentService).save(anyLong(), any());

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/tripPosts/{tripPostId}/comments", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .with(csrf()));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk());
        verify(commentService, times(1)).save(anyLong(), any());
    }

    @DisplayName("댓글 수정 정상 호출")
    @WithMockUser
    @Test
    void updateComment() throws Exception {
        // given
        Long tripPostId = 1L;
        Long commentId = 2L;
        CommentDto commentDto = new CommentDto();

        // when
        ResultActions resultActions = mockMvc.perform(put("/tripPosts/{tripPostId}/comments/{commentId}",
                tripPostId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto))
                .with(csrf()));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk());
        verify(commentService, times(1)).update(anyLong(),anyLong(), any());
    }

    @DisplayName("댓글 삭제 정상 호출")
    @WithMockUser
    @Test
    void deleteComment() throws Exception {
        // given
        Long tripPostId = 1L;
        Long commentId = 2L;

        // when
        ResultActions resultActions = mockMvc.perform(delete("/tripPosts/{tripPostId}/comments/{commentId}",
                tripPostId, commentId)
                .with(csrf()));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk());
        verify(commentService, times(1)).delete(anyLong(),anyLong());
    }
}