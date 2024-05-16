package com.mola.domain.tripFriends;

import com.mola.domain.member.entity.Member;
import com.mola.domain.trip.entity.TripPlan;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@IdClass(TripFriendsId.class)
@Builder
@Setter
@Getter
@Entity
public class TripFriends {

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;

    private boolean isOwner;
}
