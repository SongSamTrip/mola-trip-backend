package com.mola.domain.tripFriends;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripFriendsServiceTest {

    TripFriendsRepository tripFriendsRepository;
    TripFriendsService tripFriendsService;

    @BeforeEach
    void setup() {
        tripFriendsRepository = mock(TripFriendsRepository.class);
        tripFriendsService = new TripFriendsService(tripFriendsRepository);
    }

    @DisplayName("여행 플랜과 사용자 식별자가 유효한 값이라면 true 을 반환")
    @Test
    void existsByMemberAndTripPlan_success() {
        // given
        Long VALID_MEMBER_ID = 2L;
        Long VALID_TRIP_ID = 1L;
        TripFriends tripFriends = new TripFriends();
        Optional<TripFriends> optionalTripFriends = Optional.of(tripFriends);
        when(tripFriendsRepository.findByMemberAndTripPlan(VALID_MEMBER_ID, VALID_TRIP_ID))
                .thenReturn(optionalTripFriends);

        // when
        boolean validChatRequest = tripFriendsService.existsByMemberAndTripPlan(VALID_MEMBER_ID, VALID_TRIP_ID);

        // then
        assertTrue(validChatRequest);
    }

    @DisplayName("여행 플랜과 사용자 식별자 둘 중 하나라도 유효한 값이 아니라면 false 을 반환")
    @Test
    void existsByMemberAndTripPlan_fail() {
        // given
        Long INVALID_MEMBER_ID = 2L;
        Long VALID_TRIP_ID = 1L;
        when(tripFriendsRepository.findByMemberAndTripPlan(INVALID_MEMBER_ID, VALID_TRIP_ID))
                .thenReturn(Optional.empty());

        // when
        boolean validChatRequest = tripFriendsService.existsByMemberAndTripPlan(INVALID_MEMBER_ID, VALID_TRIP_ID);

        // then
        assertFalse(validChatRequest);
    }

}