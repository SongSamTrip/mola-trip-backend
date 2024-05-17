package com.mola.domain.trip.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.trip.dto.NewTripPlanDto;
import com.mola.domain.trip.dto.TripListHtmlDto;
import com.mola.domain.trip.entity.TripPlan;
import com.mola.domain.trip.repository.TripPlanRepository;
import com.mola.domain.trip.repository.TripStatus;
import com.mola.domain.tripFriends.TripFriends;
import com.mola.domain.tripFriends.TripFriendsRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import com.mola.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TripPlanService {

    private final TripPlanRepository tripPlanRepository;

    private final TripFriendsRepository tripFriendsRepository;

    private final SecurityUtil securityUtil;

    public String addTripPlan(NewTripPlanDto newTripPlanDto) {

        Member member = securityUtil.findCurrentMember();

        TripPlan tripPlan = TripPlan.builder()
                .startDate(newTripPlanDto.getStartDate())
                .endDate(newTripPlanDto.getEndDate())
                .tripName(newTripPlanDto.getTripName())
                .tripCode(UUID.randomUUID().toString())
                .tripStatus(TripStatus.ACTIVE)
                .build();

        TripPlan newTripPlan = tripPlanRepository.save(tripPlan);

        TripFriends tripFriends = TripFriends.builder()
                .member(member)
                .tripPlan(newTripPlan)
                .isOwner(true)
                .build();

        tripFriendsRepository.save(tripFriends);

        return tripPlan.toString();
    }

    @Transactional
    public void updateTripPlanList(Long tripId, TripListHtmlDto tripListHtmlDto) {
        TripPlan tripPlan = getMemberTripPlan(tripId);

        tripPlan.setSubTripList(tripListHtmlDto.getSubTripList());
        tripPlan.setMainTripList(tripListHtmlDto.getMainTripList());
        tripPlanRepository.save(tripPlan);
    }

    @Transactional
    public void updateSubPlanList(Long tripId, TripListHtmlDto tripListHtmlDto) {
        TripPlan tripPlan = getMemberTripPlan(tripId);
        tripPlan.setSubTripList(tripListHtmlDto.getSubTripList());
        tripPlanRepository.save(tripPlan);
    }

    private TripPlan getMemberTripPlan(Long tripId) {
        Long memberId = securityUtil.findCurrentMemberId();

        TripPlan tripPlan = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTrip));

        tripFriendsRepository.findByMemberAndTripPlan(memberId, tripPlan.getId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTripFriends));

        return tripPlan;
    }
}
