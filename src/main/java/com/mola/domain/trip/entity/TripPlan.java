package com.mola.domain.trip.entity;

import com.mola.domain.trip.repository.TripStatus;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripFriends.TripFriends;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class TripPlan {

    @Id @GeneratedValue
    @Column(name = "trip_plan_id")
    private Long id;

    @Column(name = "trip_name", length = 100, nullable = false)
    private String tripName;

    @Column(name = "total_trip_member")
    private Long totalTripMember;

    @Column(name = "trip_start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "trip_end_date", nullable = false)
    private LocalDateTime endDate;

    @OneToOne(mappedBy = "tripPlan")
    private TripPost tripPost;

    @Column(name = "trip_code", length = 100)
    private String tripCode;

    private TripStatus tripStatus;

    @OneToMany(
            mappedBy = "tripPlan",
            cascade = CascadeType.ALL
    )
    private List<TripFriends> tripFriendsList;

    @Column(name = "main_trip_list", columnDefinition = "TEXT")
    private String mainTripList;

    @Column(name = "sub_trip_list", columnDefinition = "TEXT")
    private String subTripList;}
