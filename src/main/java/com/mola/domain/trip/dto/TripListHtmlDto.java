package com.mola.domain.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class TripListHtmlDto {
    private String mainTripList;
    private String subTripList;
}
