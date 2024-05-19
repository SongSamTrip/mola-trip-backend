package com.mola.domain.tripBoard.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.tripImage.dto.TripImageDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostUpdateDto;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.domain.tripBoard.like.repository.LikesRepository;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.fixture.Fixture;
import com.mola.global.exception.CustomException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripPostServiceTest {

    @Mock
    TripPostRepository tripPostRepository;

    @Mock
    ModelMapper modelMapper;

    @Mock
    LikesRepository likesRepository;

    @Mock
    SecurityContext securityContext;

    @Mock
    MemberRepository memberRepository;

    @Spy
    @InjectMocks
    TripPostService tripPostService;

    TripPost tripPost;

    Member member;


    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        // tripPost 초기화
        tripPost = TripPost.builder()
                .id(1L)
                .name("name")
                .content("content")
                .tripPostStatus(TripPostStatus.PUBLIC)
                .build();
        // TripImages 초기화
        List<TripImage> tripImages = new ArrayList<>();
        LongStream.range(1, 11).forEach(i -> {
            tripImages.add(new TripImage(i, "test" + i, tripPost));
        });
        tripPost.setImageUrl(tripImages);

        // member 초기화
        member = Member.builder()
                        .id(1L)
                        .build();
        tripPost.setMember(member);
    }

    @DisplayName("게시글이 수정될 때 이미지가 제거되면 리스트 사이즈가 변경")
    @Test
    void update() {
        // given
        List<TripImageDto> tripImageDtos = new ArrayList<>();
        LongStream.range(1, 5).forEach(i -> {
            tripImageDtos.add(new TripImageDto(i, "test" + i, tripPost.getId()));
        });

        TripPostUpdateDto updateDto = TripPostUpdateDto.builder()
                .id(tripPost.getId())
                .name(tripPost.getName())
                .content(tripPost.getContent())
                .tripImageList(tripImageDtos)
                .build();


        doReturn(Optional.of(tripPost)).when(tripPostRepository).findById(any());
        doReturn(true).when(tripPostService).isOwner(any());
        doReturn(tripPost).when(tripPostRepository).save(any());
        // when
        TripPostResponseDto update = tripPostService.update(updateDto);

        // then
        assertEquals(tripPost.getId(), update.getId());
        assertEquals(tripPost.getName(), update.getName());
        assertEquals(tripPost.getContent(), update.getContent());
        assertEquals(tripPost.getImageUrl().size(), 4);
    }

    @DisplayName("게시글이 삭제될 때 이미지의 연관관계를 제거")
    @Test
    void delete() {
        // given
        List<TripImage> tripImages = new ArrayList<>();
        LongStream.range(1, 11).forEach(i -> {
            TripImage mockImage = Mockito.mock(TripImage.class);
            tripImages.add(mockImage);
        });
        tripPost.setImageUrl(tripImages);

        doReturn(Optional.of(tripPost)).when(tripPostRepository).findById(any());
        doReturn(true).when(tripPostService).isOwner(any());

        // when
        tripPostService.deleteTripPost(tripPost.getId());

        // then
        tripPost.getImageUrl().forEach(tripImage -> {
            verify(tripImage, times(1)).setTripPostNull();
        });
    }

    @DisplayName("인증된 회원이 존재하는 게시글에 좋아요를 누르면 좋아요 갯수가 증가")
    @Test
    void whenUserAddLikeValidPost_success() throws InterruptedException {
        // given
        when(tripPostRepository.existsById(anyLong())).thenReturn(true);
        when(likesRepository.existsByMemberIdAndTripPostId(anyLong(), anyLong())).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(Fixture.createAuthentication("1"));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(tripPostRepository.findByIdWithOptimisticLock(anyLong())).thenReturn(tripPost);
        SecurityContextHolder.setContext(securityContext);
        assertEquals(tripPost.getLikeCount(), 0);

        // expect
        assertDoesNotThrow(() -> tripPostService.addLikes(1L));
        assertEquals(tripPost.getLikeCount(), 1);
    }

    @DisplayName("좋아요 요청 중 충돌이 일어나면 재시도 로직이 동작")
    @Test
    void testAddLikeRetryLogicOnOptimisticLockException() throws InterruptedException {
        // given
        when(tripPostRepository.existsById(anyLong())).thenReturn(true);
        when(likesRepository.existsByMemberIdAndTripPostId(anyLong(), anyLong())).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(Fixture.createAuthentication("1"));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(tripPostRepository.findByIdWithOptimisticLock(anyLong())).thenReturn(tripPost);
        when(tripPostRepository.save(any(TripPost.class))).thenThrow(OptimisticLockException.class);

        SecurityContextHolder.setContext(securityContext);
        assertEquals(tripPost.getLikeCount(), 0);


        // expect
        assertThrows(CustomException.class, () -> tripPostService.addLikes(1L));
        verify(tripPostRepository, times(3)).save(any(TripPost.class));
    }


    @DisplayName("인증된 회원이 존재하는 게시글에 좋아요를 취소하면 좋아요 갯수가 감소")
    @Test
    void whenUserRemoveLikeValidPost_success() throws InterruptedException {
        // given
        tripPost.setLikeCount(1);
        Likes likes = new Likes();
        likes.setMember(member);
        likes.setTripPost(tripPost);

        when(tripPostRepository.existsById(anyLong())).thenReturn(true);
        when(likesRepository.existsByMemberIdAndTripPostId(anyLong(), anyLong())).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(Fixture.createAuthentication("1"));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(tripPostRepository.findByIdWithOptimisticLock(anyLong())).thenReturn(tripPost);
        when(likesRepository.findByMemberIdAndTripPostId(anyLong(), anyLong())).thenReturn(likes);
        SecurityContextHolder.setContext(securityContext);
        assertEquals(tripPost.getLikeCount(), 1);

        // expect
        assertDoesNotThrow(() -> tripPostService.removeLikes(1L));
        assertEquals(tripPost.getLikeCount(), 0);
    }

    @DisplayName("좋아요 취소 요청 시 충돌이 일어나면 재시도 로직이 동작")
    @Test
    void testRemoveLikeRetryLogicOnOptimisticLockException() throws InterruptedException {
        // given
        when(tripPostRepository.existsById(anyLong())).thenReturn(true);
        when(likesRepository.existsByMemberIdAndTripPostId(anyLong(), anyLong())).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(Fixture.createAuthentication("1"));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(tripPostRepository.findByIdWithOptimisticLock(anyLong())).thenReturn(tripPost);
        when(tripPostRepository.save(any(TripPost.class))).thenThrow(OptimisticLockException.class);

        SecurityContextHolder.setContext(securityContext);

        // expect
        assertThrows(CustomException.class, () -> tripPostService.removeLikes(1L));
        verify(tripPostRepository, times(3)).save(any(TripPost.class));
    }

}