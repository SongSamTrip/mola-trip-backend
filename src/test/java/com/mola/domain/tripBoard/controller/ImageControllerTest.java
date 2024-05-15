package com.mola.domain.tripBoard.controller;

import com.mola.domain.tripBoard.service.S3Service;
import com.mola.domain.tripBoard.service.TripImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @MockBean
    TripImageService tripImageService;

    @Autowired
    MockMvc mockMvc;

    @DisplayName("게시글 식별자가 있다면 사진이 저장된다.")
    @WithMockUser
    @Test
    void saveImageWithTripPostIdentifier() throws Exception {
        String VALID_ID = "1";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.img"
                , "image/jpeg", "Test Image Content".getBytes());

        // when
        mockMvc.perform(multipart("/images")
                .file(multipartFile)
                .param("tripPlanId", VALID_ID)
                .with(csrf()))
                .andDo(print());

        // then
        verify(tripImageService, times(1)).save(any(), any());
    }

}