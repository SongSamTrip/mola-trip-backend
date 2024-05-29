package com.mola.domain.tripBoard.repository;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.trip.entity.TripPlan;
import com.mola.domain.trip.repository.TripPlanRepository;
import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.comment.repository.CommentRepository;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.global.config.QueryDslConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@Import(QueryDslConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DataJpaTest
class TripPostRepositoryTest {

    @Autowired
    TripPostRepository tripPostRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    TripPlanRepository tripPlanRepository;

    Member savedMember;

    TripPost savedTripPost;

    static Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        // 사용자 생성
        Member firstMember = new Member();
        firstMember.setNickname("First User");
        firstMember.setPersonalId("1");
        savedMember = memberRepository.save(firstMember);

        // 공개 게시글
        IntStream.rangeClosed(1, 7).forEach(i -> {
            TripPost tripPost = new TripPost();
            tripPost.setMember(savedMember);
            tripPost.setTripPostStatus(TripPostStatus.PUBLIC); // 상태 설정
            savedTripPost = tripPostRepository.save(tripPost);

            // 각 트립 포스트에 대한 댓글 10개 추가
            IntStream.rangeClosed(1, 10).forEach(j -> {
                Comment comment = new Comment();
                comment.setMember(savedMember);
                comment.setContent("Comment " + j + " from first user");
                comment.setTripPost(savedTripPost);
                commentRepository.save(comment);
            });
        });

        // 비공개 게시글
        IntStream.rangeClosed(1, 3).forEach(i -> {
            TripPost tripPost = new TripPost();
            tripPost.setMember(savedMember);
            tripPost.setTripPostStatus(TripPostStatus.PRIVATE); // 상태 설정
            TripPost savedTripPost = tripPostRepository.save(tripPost);

            // 각 트립 포스트에 대한 댓글 10개 추가
            IntStream.rangeClosed(1, 10).forEach(j -> {
                Comment comment = new Comment();
                comment.setMember(savedMember);
                comment.setContent("Comment " + j + " from second user");
                comment.setTripPost(savedTripPost);
                commentRepository.save(comment);
            });
        });
    }

    @Test
    void findByIdWithOptimisticLock() {
        TripPost tripPost = new TripPost();
        TripPost save = tripPostRepository.save(tripPost);

        TripPost byIdWithOptimisticLock = tripPostRepository.findByIdWithOptimisticLock(save.getId());

        assertNotNull(byIdWithOptimisticLock);
    }

    @Order(1)
    @ParameterizedTest
    @MethodSource("getAllTripPostResponseDto_parameter")
    @DisplayName("TripResponseDto 를 Page 로 조회")
    void getAllTripPostResponseDto(Long memberId, TripPostStatus status, Pageable pageable, int expect) {
        // given
        memberId = memberId != null ? savedMember.getId() : memberId;

        // when
        Page<TripPostListResponseDto> all = tripPostRepository.getAllTripPostResponseDto(memberId, status, pageable);

        System.out.println(savedMember.getId());
        // then
        assertThat(all).isNotNull();
        assertThat(all.getTotalElements()).isEqualTo(expect);
        all.forEach(dto -> {
            assertThat(dto.getCommentCount()).isEqualTo(10);
        });
    }


    @DisplayName("tripPost에 속한 댓글을 Page로 10개 조회")
    @Test
    void getCommentsForTripPost() {
        // given
        Long tripPostId = savedTripPost.getId();

        // when
        Page<CommentDto> commentsForTripPost = tripPostRepository.getCommentsForTripPost(tripPostId, pageable);

        // then
        assertThat(commentsForTripPost).isNotNull();
        assertThat(commentsForTripPost.getTotalElements()).isEqualTo(10);
    }

    @DisplayName("tripPost가 PUBLIC 상태라면 true 반환")
    @Test
    void isPublic() {
        // given
        Long tripPostId = savedTripPost.getId();

        // when
        boolean isPublic = tripPostRepository.isPublic(tripPostId);

        // then
        assertThat(isPublic).isTrue();
    }

    @DisplayName("tripPost가 DRAFT 상태라면 초기값이 null이므로 다른 쿼리를 실행하지 않는다")
    @Test
    void getTripPostResponseDtoById_draft() {
        // given
        Long memberId = savedMember.getId();
        TripPost tripPost = tripPostRepository.save(TripPost.createDraft(savedMember));

        // when
        TripPostResponseDto dto = tripPostRepository.getTripPostResponseDtoById(tripPost.getId(), memberId);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getMemberId()).isEqualTo(memberId);
        assertThat(dto.getTripPostStatus()).isEqualTo(TripPostStatus.DRAFT);
        assertThat(dto.getCommentDtos().getTotalElements()).isEqualTo(0);
    }

    @DisplayName("tripPost를 관련있는 엔티티들과 함께 게시글 상세 dto 로 반환")
    @Test
    void getTripPostResponseDtoById_notDraft() {
        // given
        TripPlan tripPlan = TripPlan.builder()
                .tripName("test")
                .startDate(LocalDateTime.of(2024, 5, 29, 12, 0, 0))
                .endDate(LocalDateTime.of(2024, 5, 30, 12, 0, 0))
                .build();
        TripPlan save = tripPlanRepository.save(tripPlan);

        savedTripPost.setTripPlan(save);
        Long memberId = savedMember.getId();
        Long tripPostId = tripPostRepository.save(savedTripPost).getId();

        // when
        TripPostResponseDto dto = tripPostRepository.getTripPostResponseDtoById(tripPostId, memberId);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getMemberId()).isEqualTo(memberId);
        assertThat(dto.isLike()).isFalse();
        assertThat(dto.getTripName()).isEqualTo("test");
        assertThat(dto.getCommentDtos().getTotalElements()).isEqualTo(10);
    }




    private static Stream<Arguments> getAllTripPostResponseDto_parameter() {
        return Stream.of(
                // 사용자가 작성한 모든 게시글
                Arguments.of(1L, null, pageable, 10),
                // 관리자 권한으로 모든 게시글
                Arguments.of(null, null, pageable, 10),
                // 모든 공개 게시글
                Arguments.of(null, TripPostStatus.PUBLIC, pageable, 7)
        );
    }
}