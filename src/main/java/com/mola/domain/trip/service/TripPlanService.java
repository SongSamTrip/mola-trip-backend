package com.mola.domain.trip.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.trip.dto.NewTripPlanDto;
import com.mola.domain.trip.dto.TripListHtmlDto;
import com.mola.domain.trip.dto.TripPlanDto;
import com.mola.domain.trip.dto.TripPlanInfoDto;
import com.mola.domain.trip.entity.TripPlan;
import com.mola.domain.trip.repository.TripPlanRepository;
import com.mola.domain.trip.repository.TripStatus;
import com.mola.domain.tripFriends.TripFriends;
import com.mola.domain.tripFriends.TripFriendsRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import com.mola.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TripPlanService {

    private final TripPlanRepository tripPlanRepository;

    private final TripFriendsRepository tripFriendsRepository;

    private final SecurityUtil securityUtil;

    private final ModelMapper modelMapper;

    @Transactional
    public Long addTripPlan(NewTripPlanDto newTripPlanDto) {

        Member member = securityUtil.findCurrentMember();

        TripPlan tripPlan = TripPlan.builder()
                .startDate(newTripPlanDto.getStartDate())
                .endDate(newTripPlanDto.getEndDate())
                .tripName(newTripPlanDto.getTripName())
                .tripCode(UUID.randomUUID().toString())
                .tripStatus(TripStatus.ACTIVE)
                .totalTripMember(1L)
                .build();

        TripPlan newTripPlan = tripPlanRepository.save(tripPlan);

        TripFriends tripFriends = TripFriends.builder()
                .member(member)
                .tripPlan(newTripPlan)
                .isOwner(true)
                .build();

        tripFriendsRepository.save(tripFriends);
        return newTripPlan.getId();
    }

    @Transactional
    public TripListHtmlDto updateTripPlanList(Long tripId, TripListHtmlDto tripListHtmlDto) {
        TripPlan tripPlan = getMemberTripPlan(tripId);

        modelMapper.map(tripListHtmlDto, tripPlan);

        tripPlan = tripPlanRepository.save(tripPlan);

        return TripListHtmlDto.builder()
                .mainTripList(tripPlan.getMainTripList())
                .subTripList(tripPlan.getSubTripList())
                .build();
    }

    @Transactional
    public TripListHtmlDto updateSubPlanList(Long tripId, TripListHtmlDto tripListHtmlDto) {
        TripPlan tripPlan = getMemberTripPlan(tripId);
        tripPlan.setSubTripList(tripListHtmlDto.getSubTripList());
        tripPlan = tripPlanRepository.save(tripPlan);
        return TripListHtmlDto.builder()
                .mainTripList(tripPlan.getMainTripList())
                .subTripList(tripPlan.getSubTripList())
                .build();
    }

    public TripPlanInfoDto getTripList(Long tripPlanId) {
        TripPlan tripPlan = getMemberTripPlan(tripPlanId);
        return TripPlanInfoDto.builder()
                .tripListHtmlDto(TripListHtmlDto.builder()
                        .mainTripList(tripPlan.getMainTripList())
                        .subTripList(tripPlan.getSubTripList())
                        .build()
                )
                .tripCode(tripPlan.getTripCode())
                .build();

    }

    private TripPlan getMemberTripPlan(Long tripId) {
        Long memberId = securityUtil.findCurrentMemberId();

        TripPlan tripPlan = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTrip));

        tripFriendsRepository.findByMemberAndTripPlan(memberId, tripPlan.getId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTripFriends));

        return tripPlan;
    }

    @Transactional
    public Long addParticipant(String tripCode) {
        Member member = securityUtil.findCurrentMember();

        TripPlan tripPlan = tripPlanRepository.findByTripCode(tripCode)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidTripPlan));

        Optional<TripFriends> existingTripFriends = tripFriendsRepository.findByMemberAndTripPlan(member.getId(),
                tripPlan.getId());
        if (existingTripFriends.isPresent()) {
            return tripPlan.getId();
        }

        tripPlan.setTotalTripMember(tripPlan.getTotalTripMember() + 1);
        tripPlanRepository.save(tripPlan);

        TripFriends tripFriends = TripFriends.builder()
                .member(member)
                .tripPlan(tripPlan)
                .isOwner(false)
                .build();

        tripFriendsRepository.save(tripFriends);
        return tripPlan.getId();
    }

    public List<TripPlanDto> getTripPlans() {
        Long memberId = securityUtil.findCurrentMemberId();
        List<TripFriends> tripFriendsList = tripFriendsRepository.findAllByMemberId(memberId);

        if (tripFriendsList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tripPlanIds = tripFriendsList.stream()
                .map(tripFriends -> tripFriends.getTripPlan().getId())
                .distinct()
                .collect(Collectors.toList());

        List<TripPlan> tripPlans = tripPlanRepository.findAllById(tripPlanIds);

        return tripPlans.stream()
                .map(this::convertTripPlansToDto)
                .collect(Collectors.toList());
    }

    private TripPlanDto convertTripPlansToDto(TripPlan tripPlan) {

        return TripPlanDto.builder()
                .tripName(tripPlan.getTripName())
                .tripId(tripPlan.getId())
                .tripImageUrl("https://picsum.photos/200")
                .totalTripMember(tripPlan.getTotalTripMember())
                .build();
    }


    public void checkMemberIsInTrip(Long tripId) {
        getMemberTripPlan(tripId);
    }
}
