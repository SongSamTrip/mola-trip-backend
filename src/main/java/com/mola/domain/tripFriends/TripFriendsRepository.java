package com.mola.domain.tripFriends;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TripFriendsRepository extends JpaRepository<TripFriends, TripFriendsId> {

    @Query("SELECT tf FROM TripFriends tf WHERE tf.member.id = :memberId AND tf.tripPlan.id = :tripPlanId")
    Optional<TripFriends> findByMemberAndTripPlan(@Param("memberId") Long memberId,
                                                  @Param("tripPlanId") Long tripPlanId);


    List<TripFriends> findAllByMemberId(Long memberId);
}
