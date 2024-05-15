package com.mola.domain.tripBoard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TripPostDto {

    @NotNull
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String content;
}
