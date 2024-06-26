package com.mola.domain.trip.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripPlanInfoDto {
    String tripCode;
    List<TripPlanDto> tripPlanDtos;
    TripListHtmlDto tripListHtmlDto;
}
