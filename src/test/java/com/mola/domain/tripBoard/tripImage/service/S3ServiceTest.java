package com.mola.domain.tripBoard.tripImage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.mola.global.exception.CustomException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    AmazonS3 amazonS3;

    S3Service s3Service;

    @BeforeEach
    void setup() {
        amazonS3 = mock(AmazonS3.class);
        s3Service = new S3Service(amazonS3, "test");
    }

    @DisplayName("이미지를 S3 에 업로드 한다")
    @Test
    void upload() throws Exception {
        // given
        String fileName = "test.jpg";
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("image/jpeg");

        URL mockURL = new URL("http://example.com/" + fileName);
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(mockURL);

        // when
        String url = s3Service.upload(file);

        // then
        assertThat(url).isNotNull();
        assertThat(url).contains(fileName);
    }

    @DisplayName("이미지를 S3 에 업로드 하는 과정에 에러가 발생하면 CustomException 이 발생")
    @Test
    void upload_throwsException() throws Exception {
        // given
        String fileName = "test.jpg";
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("image/jpeg");

        doThrow(new IOException("ex")).when(file).getInputStream();

        // expected
        Assertions.assertThrows(CustomException.class,
                () -> s3Service.upload(file));
        verify(amazonS3, never()).getUrl(anyString(), anyString());
    }

    @DisplayName("이미지가 삭제된다")
    @Test
    void delete() {
        // given
        String VALID_FILE_NAME = "test.jpg";

        // when
        s3Service.delete(VALID_FILE_NAME);

        // then
        verify(amazonS3, times(1)).deleteObject(anyString(), anyString());
    }
}