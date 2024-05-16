package com.mola.domain.tripBoard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TripPostUpdateDto {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String content;

    private List<TripImageDto> tripImageList = new ArrayList<>();

}
