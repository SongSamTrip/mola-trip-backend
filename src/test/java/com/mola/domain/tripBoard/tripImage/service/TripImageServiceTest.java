package com.mola.domain.tripBoard.tripImage.service;

import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.domain.tripBoard.tripImage.repository.TripImageRepository;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripImageServiceTest {

    @Mock
    TripImageRepository tripImageRepository;

    @Mock
    TripPostService tripPostService;

    @Mock
    ImageService imageService;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    TripImageService tripImageService;


    @DisplayName("존재하는 tripPost 라면 이미지를 저장")
    @Test
    void saveImage_success(){
        // given
        Long VALID_ID = 1L;
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(tripPostService.existsTripPost(VALID_ID)).thenReturn(true);
        when(imageService.upload(file)).thenReturn("url");
        when(tripImageRepository.save(any(TripImage.class))).thenReturn(new TripImage("url", new TripPost()));

        // when
        TripImage save = tripImageService.save(VALID_ID, file);

        // then
        verify(imageService, times(1)).upload(file);
        verify(tripImageRepository, times(1)).save(any(TripImage.class));
        Assertions.assertNotNull(save);
    }

    @DisplayName("존재하지 않는 tripPost 라면 에러를 발생")
    @Test
    void saveImage_throwsException(){
        // given
        Long INVALID_ID = 1L;
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(tripPostService.existsTripPost(INVALID_ID)).thenReturn(false);

        // expected
        Assertions.assertThrows(CustomException.class, () -> tripImageService.save(INVALID_ID, file));
        verify(imageService, never()).upload(file);
        verify(tripImageRepository, never()).save(any(TripImage.class));
    }

    @DisplayName("고아객체인 이미지를 찾아서 삭제작업 수행")
    @Test
    void deleteOrphanImages_thenDeleteImages() {
        // given
        List<TripImage> orphanImages = List.of(new TripImage("url1", null), new TripImage("url2", null));
        when(tripImageRepository.findAllByFlag(false)).thenReturn(orphanImages);

        // when
        tripImageService.deleteOrphanImages();

        // then
        verify(imageService, times(orphanImages.size())).delete(anyString());
        orphanImages.forEach(img -> verify(imageService).delete(img.getUrl()));
    }

}