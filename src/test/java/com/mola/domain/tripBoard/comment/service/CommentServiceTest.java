package com.mola.domain.tripBoard.comment.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.entity.MemberRole;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.comment.repository.CommentRepository;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.fixture.Fixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TripPostService tripPostService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Spy
    @InjectMocks
    private CommentService commentService;

    private final Long VALID_ID = 1L;
    private final Long INVALID_ID = 0L;

    private TripPost tripPost;
    private Member member;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setup() {
        tripPost = Fixture.createTripPost(VALID_ID, TripPostStatus.PUBLIC);
        member = Fixture.createMember(VALID_ID, "test");
        tripPost.setMember(member);
        comment = Fixture.createComment(VALID_ID, "test", member, tripPost);
        commentDto = Fixture.createCommentDto(VALID_ID, VALID_ID, "test", "test", LocalDateTime.now());
    }


    @DisplayName("존재하는 공개상태 게시글의 댓글을 페이지로 반환")
    @Test
    void getAllComments_success() {
        // given
        PageRequest pageRequest = PageRequest.of(1, 10);
        Page<CommentDto> commentDtoPage = Fixture.createCommentDtoPage(1, 10);

        doNothing().when(commentService).validateTripPost(VALID_ID);
        doReturn(commentDtoPage).when(tripPostService).getCommentsForTripPost(anyLong(), any(Pageable.class));

        // when
        Page<CommentDto> allComments = commentService.getAllComments(VALID_ID, pageRequest);

        // then
        assertThat(allComments.getContent().size()).isEqualTo(10);
    }

    @DisplayName("비공개 상태의 게시글에 대한 댓글에 접근하면 에러 발생")
    @Test
    void validateTripPost_throwsException() {
        // given
        when(tripPostService.isPublic(anyLong())).thenReturn(false);

        // expected
        assertThatThrownBy(() -> commentService.validateTripPost(INVALID_ID));
    }

    @DisplayName("공개상태의 게시글에 댓글을 저장하면 CommentDto 를 반환")
    @Test
    void saveComment_success() {
        // given
        String content = "test";

        doNothing().when(commentService).validateTripPost(VALID_ID);
        doReturn(VALID_ID).when(commentService).getAuthenticatedMemberId();
        when(entityManager.getReference(TripPost.class, VALID_ID)).thenReturn(tripPost);
        when(memberRepository.existsById(VALID_ID)).thenReturn(true);
        when(entityManager.getReference(Member.class, VALID_ID)).thenReturn(member);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // when
        CommentDto dto = commentService.save(VALID_ID, content);

        // then
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getContent()).isEqualTo(content);
        assertThat(dto.getMemberTripPostDto().getId()).isEqualTo(VALID_ID);
    }


    @DisplayName("댓글 작성자가 댓글을 수정할 수 있다")
    @Test
    void saveComment() {
        // given
        doNothing().when(commentService).validateTripPost(VALID_ID);
        doReturn(VALID_ID).when(commentService).getAuthenticatedMemberId();
        doReturn(comment).when(commentService).findById(VALID_ID);
        when(commentRepository.isUserAuthorizedForComment(VALID_ID, VALID_ID)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // when
        CommentDto update = commentService.update(VALID_ID, VALID_ID, commentDto);

        // then
        verify(commentRepository).save(any(Comment.class));
        assertThat(update.getId()).isEqualTo(VALID_ID);
        assertThat(update.getContent()).isEqualTo(commentDto.getContent());
    }

    @DisplayName("댓글 작성자가 아니라면 댓글을 수정 요청 시 에러 발생")
    @Test
    void saveComment_throwsException() {
        // given
        doNothing().when(commentService).validateTripPost(VALID_ID);
        doReturn(VALID_ID).when(commentService).getAuthenticatedMemberId();
        doReturn(comment).when(commentService).findById(VALID_ID);
        when(commentRepository.isUserAuthorizedForComment(INVALID_ID, INVALID_ID)).thenReturn(false);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // expected
        assertThatThrownBy(() -> commentService.update(INVALID_ID,INVALID_ID,commentDto));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @DisplayName("댓글 작성자 혹은 관리자는 댓글을 삭제할 수 있다")
    @Test
    void deleteComment_success() {
        // given
        doNothing().when(commentService).validateTripPost(VALID_ID);
        doReturn(VALID_ID).when(commentService).getAuthenticatedMemberId();
        when(commentRepository.isUserAuthorizedForComment(VALID_ID,VALID_ID)).thenReturn(true);

        // expected
        assertDoesNotThrow(() -> commentService.delete(VALID_ID, VALID_ID));
        verify(commentRepository, times(1)).deleteById(anyLong());
    }

    @DisplayName("댓글 작성자 혹은 관리자가 아니라면 댓글 삭제 요청 시 에러 발생")
    @Test
    void deleteComment_throwsException() {
        // given
        doNothing().when(commentService).validateTripPost(VALID_ID);
        doReturn(VALID_ID).when(commentService).getAuthenticatedMemberId();
        when(commentRepository.isUserAuthorizedForComment(INVALID_ID, INVALID_ID)).thenReturn(false);
        when(memberRepository.findRoleByMemberId(INVALID_ID)).thenReturn(MemberRole.USER);

        // expected
        assertThatThrownBy(() -> commentService.delete(INVALID_ID, INVALID_ID));
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @DisplayName("인증된 회원 정보가 있다면 식별자를 반환")
    @Test
    void getAuthenticationMemberId_success() {
        // given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(String.valueOf(VALID_ID));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        Long memberId = commentService.getAuthenticatedMemberId();

        // then
        assertThat(memberId).isEqualTo(VALID_ID);
    }

    @DisplayName("인증된 회원 정보가 없다면 에러를 발생")
    @Test
    void getAuthenticationMemberId_throwsException() {
        // given
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // expected
        assertThatThrownBy(() -> commentService.getAuthenticatedMemberId());
    }

    @DisplayName("존재하지 않는 댓글을 조회 요청 시 에러를 발생")
    @Test
    void findById_throwsException() {
        // given
        when(commentRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> commentService.findById(INVALID_ID));
    }


}