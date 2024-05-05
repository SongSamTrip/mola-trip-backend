package com.mola.domain.tripFriends;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter @Setter
public class TripFriendsId implements Serializable {
    private Long member;
    private Long tripPlan;
}
