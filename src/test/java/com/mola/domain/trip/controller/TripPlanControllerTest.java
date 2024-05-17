package com.mola.domain.trip.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mola.domain.trip.dto.NewTripPlanDto;
import com.mola.domain.trip.service.TripPlanService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TripPlanController.class)
class TripPlanControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TripPlanService tripPlanService;

    @DisplayName("새 여행 계획 생성 Mock 사용자 사용")
    @WithMockUser
    @Test
    void createTripPlanSuccess() throws Exception {
        // given
        NewTripPlanDto mockTripPlanDto = NewTripPlanDto.builder()
                .tripName("SSAFY 방학 제발")
                .startDate(LocalDateTime.of(2024, 5, 25, 0, 0))
                .endDate(LocalDateTime.of(2024, 5, 30, 0, 0))
                .build();

        // when
        doNothing().when(tripPlanService).addTripPlan(mockTripPlanDto);

        // then
        mockMvc.perform(post("/api/trip-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockTripPlanDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Add trip plan success"));

        verify(tripPlanService, times(1)).addTripPlan(any(NewTripPlanDto.class));

    }

    @DisplayName("계획에 사용자 추가 성공")
    @WithMockUser
    @Test
    void addMemberToTripSuccess() throws Exception {
        // given
        String tripCode = "12345";

        // when
        doNothing().when(tripPlanService).addParticipant(tripCode);

        //then
        mockMvc.perform(post("/api/trip-plan/{tripCode}", tripCode))
                .andExpect(status().isOk())
                .andExpect(content().string("Add participant to trip plan success"));

        verify(tripPlanService, times(1)).addParticipant(tripCode);

    }

}