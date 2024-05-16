package com.mola.domain.tripBoard.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.dto.TripImageDto;
import com.mola.domain.tripBoard.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.dto.TripPostUpdateDto;
import com.mola.domain.tripBoard.entity.TripImage;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.entity.TripPostStatus;
import com.mola.domain.tripBoard.repository.TripPostRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripPostServiceTest {

    @Mock
    TripPostRepository tripPostRepository;

    @Mock
    ModelMapper modelMapper;

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


        doReturn(Optional.of(tripPost)).when(tripPostRepository).findById(any());
        doReturn(true).when(tripPostService).isOwner(any());
    }

    @DisplayName("게시글이 수정될 때 이미지가 제거되면 리스트 사이즈가 변경")
    @Test
    void update() {
        // given
        doReturn(tripPost).when(tripPostRepository).save(any());
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

        // when
        tripPostService.deleteTripPost(tripPost.getId());

        // then
        tripPost.getImageUrl().forEach(tripImage -> {
            verify(tripImage, times(1)).setTripPostNull();
        });
    }
}