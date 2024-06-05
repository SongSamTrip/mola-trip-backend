package com.mola.domain.tripBoard.like.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.like.repository.LikesRepository;
import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.util.SecurityUtil;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikesServiceTest {

    @Mock
    LikesRepository likesRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    TripPostRepository tripPostRepository;

    @Mock
    SecurityUtil securityUtil;

    @InjectMocks
    LikesService likesService;


    TripPost tripPost;

    Member member;

    TripPostDto tripPostDto;

    Long VALID_ID = 1L;

    @BeforeEach
    void setUp() {
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

        assertDoesNotThrow(() -> likesService.addLikes(1L));
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

        assertThrows(CustomException.class, () -> likesService.addLikes(1L));
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

        assertDoesNotThrow(() -> likesService.removeLikes(1L));
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

        assertThrows(CustomException.class, () -> likesService.removeLikes(1L));
        verify(tripPostRepository, times(3)).save(any(TripPost.class));
    }

}