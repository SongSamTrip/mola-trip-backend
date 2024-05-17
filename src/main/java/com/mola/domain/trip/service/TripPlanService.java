package com.mola.domain.trip.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.trip.dto.NewTripPlanDto;
import com.mola.domain.trip.entity.TripPlan;
import com.mola.domain.trip.repository.TripPlanRepository;
import com.mola.domain.trip.repository.TripStatus;
import com.mola.domain.tripFriends.TripFriends;
import com.mola.domain.tripFriends.TripFriendsRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import com.mola.global.util.SecurityUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TripPlanService {

    private final TripPlanRepository tripPlanRepository;

    private final TripFriendsRepository tripFriendsRepository;

    private final SecurityUtil securityUtil;

    @Transactional
    public void addTripPlan(NewTripPlanDto newTripPlanDto) {

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

    }

    @Transactional
    public void addParticipant(String tripCode) {
        Member member = securityUtil.findCurrentMember();

        TripPlan tripPlan = tripPlanRepository.findByTripCode(tripCode)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTripPlan));

        TripFriends tripFriends = TripFriends.builder()
                .member(member)
                .tripPlan(tripPlan)
                .isOwner(false)
                .build();

        tripFriendsRepository.save(tripFriends);
    }
}