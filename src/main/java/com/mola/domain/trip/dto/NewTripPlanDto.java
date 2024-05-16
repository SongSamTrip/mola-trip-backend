package com.mola.domain.trip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
public class NewTripPlanDto {

    @NotBlank
    @JsonProperty("trip_name")
    private String tripName;

    @NotNull
    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @NotNull
    @JsonProperty("end_date")
    private LocalDateTime endDate;
}
