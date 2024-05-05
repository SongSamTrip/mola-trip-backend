package com.mola.domain.tripFriends;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TripFriendsService {

    private final TripFriendsRepository tripFriendsRepository;

    @Transactional(readOnly = true)
    public boolean existsByMemberAndTripPlan(Long memberId, Long tripPlanId){
        return tripFriendsRepository.findByMemberAndTripPlan(memberId, tripPlanId).isPresent();
    }
}
