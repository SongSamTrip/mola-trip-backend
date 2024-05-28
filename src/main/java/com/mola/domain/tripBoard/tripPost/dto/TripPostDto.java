package com.mola.domain.tripBoard.tripPost.dto;

import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Setter
@Getter
public class TripPostDto {

    @NotNull
    private Long id;

    @NotNull
    private Long memberId;

    @NotBlank
    private String name;

    @NotBlank
    private String content;

    @NotNull
    private Long tripPlanId;

    private TripPostStatus tripPostStatus;
}
