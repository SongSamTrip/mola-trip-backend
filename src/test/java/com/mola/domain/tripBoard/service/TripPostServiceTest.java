package com.mola.domain.tripBoard.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.like.repository.LikesRepository;
import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.domain.tripBoard.tripImage.repository.TripImageRepository;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.global.exception.CustomException;
import com.mola.global.util.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripPostServiceTest {

    @Mock
    TripPostRepository tripPostRepository;

    @Mock
    TripImageRepository tripImageRepository;

    @Mock
    SecurityUtil securityUtil;

    @Mock
    ModelMapper modelMapper;

    @Mock
    LikesRepository likesRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    EntityManager entityManager;

    @Spy
    @InjectMocks
    TripPostService tripPostService;

    TripPost tripPost;

    Member member;

    TripPostDto tripPostDto;

    Long VALID_ID = 1L;
    Long INVALID_ID = 0L;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        // TripPost, TripImage, Member 초기화
        tripPost = TripPost.builder()
                .id(VALID_ID)
                .name("name")
                .content("content")
                .tripPostStatus(TripPostStatus.PUBLIC)
                .build();
        List<TripImage> tripImages = new ArrayList<>();
        LongStream.range(1, 11).forEach(i -> {
            tripImages.add(new TripImage(i, "test" + i, tripPost));
        });
        tripPost.setImageUrl(tripImages);
        member = Member.builder().id(VALID_ID).build();
        tripPost.setMember(member);

        // TripPostDto 초기화
        String content = "<p>Hello World</p><img src='http://example.com/image.jpg'/>";
        tripPostDto = TripPostDto.builder()
                .id(tripPost.getId())
                .memberId(member.getId())
                .name("test")
                .content(content)
                .tripPlanId(VALID_ID)
                .tripPostStatus(TripPostStatus.DRAFT)
                .build();
    }


    @DisplayName("인증된 회원이 존재하는 게시글에 좋아요를 누르면 좋아요 갯수가 증가")
    @Test
    void whenUserAddLikeValidPost_success() throws InterruptedException {
        when(securityUtil.getAuthenticatedMemberId()).thenReturn(member.getId());
        when(tripPostRepository.existsById(anyLong())).thenReturn(true);
        when(likesRepository.existsByMemberIdAndTripPostIdImpl(anyLong(), anyLong())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(tripPostRepository.findByIdWithOptimisticLock(anyLong())).thenReturn(tripPost);

        assertEquals(tripPost.getLikeCount(), 0);

        assertDoesNotThrow(() -> tripPostService.addLikes(1L));
        assertEquals(tripPost.getLikeCount(), 1);
    }

    @DisplayName("좋아요 요청 중 충돌이 일어나면 재시도 로직이 동작")
    @Test
    void testAddLikeRetryLogicOnOptimisticLockException() throws InterruptedException {
        when(securityUtil.getAuthenticatedMemberId()).thenReturn(member.getId());
        when(tripPostRepository.existsById(anyLong())).thenReturn(true);
        when(likesRepository.existsByMemberIdAndTripPostIdImpl(anyLong(), anyLong())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(tripPostRepository.findByIdWithOptimisticLock(anyLong())).thenReturn(tripPost);
        when(tripPostRepository.save(any(TripPost.class))).thenThrow(OptimisticLockException.class);

        assertEquals(tripPost.getLikeCount(), 0);

        assertThrows(CustomException.class, () -> tripPostService.addLikes(1L));
        verify(tripPostRepository, times(3)).save(any(TripPost.class));
    }

    @DisplayName("인증된 회원이 존재하는 게시글에 좋아요를 취소하면 좋아요 갯수가 감소")
    @Test
    void whenUserRemoveLikeValidPost_success() throws InterruptedException {
        tripPost.setLikeCount(1);
        Likes likes = new Likes();
        likes.setMember(member);
        likes.setTripPost(tripPost);

        when(securityUtil.getAuthenticatedMemberId()).thenReturn(member.getId());
        when(tripPostRepository.existsById(anyLong())).thenReturn(true);
        when(likesRepository.existsByMemberIdAndTripPostIdImpl(anyLong(), anyLong())).thenReturn(true);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(tripPostRepository.findByIdWithOptimisticLock(anyLong())).thenReturn(tripPost);
        when(likesRepository.findByMemberIdAndTripPostId(anyLong(), anyLong())).thenReturn(likes);

        assertEquals(tripPost.getLikeCount(), 1);

        assertDoesNotThrow(() -> tripPostService.removeLikes(1L));
        assertEquals(tripPost.getLikeCount(), 0);
    }

    @DisplayName("좋아요 취소 요청 시 충돌이 일어나면 재시도 로직이 동작")
    @Test
    void testRemoveLikeRetryLogicOnOptimisticLockException() throws InterruptedException {
        when(securityUtil.getAuthenticatedMemberId()).thenReturn(member.getId());
        when(tripPostRepository.existsById(anyLong())).thenReturn(true);
        when(likesRepository.existsByMemberIdAndTripPostIdImpl(anyLong(), anyLong())).thenReturn(true);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(tripPostRepository.findByIdWithOptimisticLock(anyLong())).thenReturn(tripPost);
        when(tripPostRepository.save(any(TripPost.class))).thenThrow(OptimisticLockException.class);

        assertThrows(CustomException.class, () -> tripPostService.removeLikes(1L));
        verify(tripPostRepository, times(3)).save(any(TripPost.class));
    }

    @DisplayName("공개상태의 모든 게시글을 조회")
    @Test
    void getAllPublicTripPosts_success() {
        // given
        PageRequest pageRequest = PageRequest.of(1, 10);
        when(tripPostRepository.getAllTripPostResponseDto(null, TripPostStatus.PUBLIC, pageRequest))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        // when
        tripPostService.getAllPublicTripPosts(pageRequest);

        // then
        verify(tripPostRepository, times(1)).getAllTripPostResponseDto(null, TripPostStatus.PUBLIC, pageRequest);
    }

    @DisplayName("본인이 작성한 게시글을 조회")
    @Test
    void getAllMyPosts() {
        // given
        PageRequest pageRequest = PageRequest.of(1, 10);
        when(securityUtil.getAuthenticatedMemberId()).thenReturn(member.getId());
        when(tripPostRepository.getAllTripPostResponseDto(member.getId(), null, pageRequest))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        // when
        tripPostService.getAllMyPosts(pageRequest);

        // then
        verify(tripPostRepository, times(1)).getAllTripPostResponseDto(member.getId(), null, pageRequest);
    }

    @DisplayName("본인이 작성한 게시글을 조회")
    @Test
    void adminGetAllMyPosts() {
        // given
        PageRequest pageRequest = PageRequest.of(1, 10);
        when(tripPostRepository.getAllTripPostResponseDto(null, null, pageRequest))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        // when
        tripPostService.adminGetAllPosts(pageRequest);

        // then
        verify(tripPostRepository, times(1)).getAllTripPostResponseDto(null, null, pageRequest);
    }

    @DisplayName("공개상태의 게시글이라면 true 반환")
    @Test
    void isPublic_true() {
        // given
        when(tripPostRepository.isPublic(tripPost.getId())).thenReturn(true);

        // when
        boolean aPublic = tripPostService.isPublic(tripPost.getId());

        //then
        assertTrue(aPublic);
        verify(tripPostRepository, times(1)).isPublic(tripPost.getId());
    }

    @DisplayName("존재하는 게시글이라면 true 반환")
    @Test
    void existsTripPost() {
        // given
        when(tripPostRepository.existsById(tripPost.getId())).thenReturn(true);

        // when
        boolean aPublic = tripPostService.existsTripPost(tripPost.getId());

        // then
        assertTrue(aPublic);
        verify(tripPostRepository, times(1)).existsById(tripPost.getId());
    }

    @DisplayName("권한이 있는 사용자라면 게시글 상세 dto를 반환")
    @Test
    void getTripPostResponseDto_success() {
        // given
        when(securityUtil.getAuthenticatedMemberId()).thenReturn(member.getId());
        when(tripPostRepository.isPublic(anyLong())).thenReturn(true);

        // when
        tripPostService.getTripPostResponseDto(tripPost.getId());

        // then
        verify(tripPostRepository, times(1)).getTripPostResponseDtoById(anyLong(), anyLong());
    }

    @DisplayName("작성한 사용자가 아니라면 게시글 상세 에러를 발생")
    @Test
    void getTripPostResponseDto_throwException() {
        // given
        when(tripPostRepository.isPublic(anyLong())).thenReturn(false);
        when(securityUtil.isAdmin(anyLong())).thenReturn(false);
        when(securityUtil.getAuthenticatedMemberId()).thenReturn(INVALID_ID);
        when(tripPostRepository.findById(anyLong())).thenReturn(Optional.of(tripPost));

        // expected
        assertThrows(CustomException.class, () -> tripPostService.getTripPostResponseDto(tripPost.getId()));
    }

    @DisplayName("존재하는 tripPost 이라면 엔티티를 반환")
    @Test
    void findById() {
        // given
        when(tripPostRepository.findById(VALID_ID)).thenReturn(Optional.of(tripPost));

        // expected
        assertDoesNotThrow(() -> tripPostService.findById(VALID_ID));
    }

    @DisplayName("존재하지 않는 tripPost 이라면 에러를 발생")
    @Test
    void findById_throwException() {
        // given
        when(tripPostRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        // expected
        assertThrows(CustomException.class, () -> tripPostService.findById(INVALID_ID));
    }

    @DisplayName("임시 상태 tripPost 를 생성")
    @Test
    void createDraftTripPost_success() {
        // given
        when(securityUtil.getAuthenticatedMemberId()).thenReturn(member.getId());
        when(entityManager.getReference(Member.class, member.getId())).thenReturn(member);
        when(tripPostRepository.save(any(TripPost.class))).thenReturn(tripPost);

        // when
        Map<String, Long> draftTripPost = tripPostService.createDraftTripPost();

        // then
        assertEquals(draftTripPost.get("memberId"), member.getId());
        assertEquals(draftTripPost.get("tempPostId"), tripPost.getId());
    }

    @DisplayName("일치하는 회원이 아니라면 에러를 발생")
    @Test
    void testSave_UnauthorizedUser_ThrowsException() {
        // given
        when(securityUtil.getAuthenticatedMemberId()).thenReturn(INVALID_ID);
        when(tripPostRepository.findById(anyLong())).thenReturn(Optional.of(tripPost));

        // Act & Assert
        assertThrows(CustomException.class, () -> tripPostService.save(tripPostDto));
    }

    @DisplayName("일치하는 회원이라면 tripPost 를 저장")
    @Test
    void saveTripPost_success() {
        // given
        doReturn(tripPost).when(tripPostService).findById(anyLong());
        doReturn(tripPost.getMember().getId()).when(securityUtil).getAuthenticatedMemberId();
        doReturn(new TripImage()).when(tripImageRepository).save(any(TripImage.class));

        // expected
        assertDoesNotThrow(() -> tripPostService.save(tripPostDto));
    }

    @DisplayName("tripPost 작성자의 삭제요청은 정상 처리")
    @Test
    void deleteTripPost_success() {
        // given
        doReturn(tripPost).when(tripPostService).findById(anyLong());
        doReturn(tripPost.getMember().getId()).when(securityUtil).getAuthenticatedMemberId();

        // expected
        assertDoesNotThrow(() -> tripPostService.deleteTripPost(VALID_ID));
    }

    @DisplayName("tripPost 작성자가 아닌 사용자의 삭제요청은 에러를 발생")
    @Test
    void deleteTripPost_throwsException() {
        // given
        doReturn(tripPost).when(tripPostService).findById(anyLong());
        doReturn(INVALID_ID).when(securityUtil).getAuthenticatedMemberId();
        doReturn(false).when(securityUtil).isAdmin(anyLong());

        // expected
        assertThrows(CustomException.class, () -> tripPostService.deleteTripPost(VALID_ID));
    }

    @DisplayName("관리자의 tripPost 삭제요청은 정상 처리")
    @Test
    void deleteAdminTripPost_success() {
        // given
        doReturn(tripPost).when(tripPostService).findById(anyLong());

        // expected
        assertDoesNotThrow(() -> tripPostService.deleteAdminTripPost(VALID_ID));
    }

}
