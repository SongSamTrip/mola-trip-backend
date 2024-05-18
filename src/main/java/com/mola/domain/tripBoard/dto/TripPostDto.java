package com.mola.domain.tripBoard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Setter
@Getter
public class TripPostDto {

    @NotNull
    private Long id;

    private String writer;

    @NotNull
    private String name;

    @NotNull
    private String content;

    private List<TripImageDto> tripImageDtos;
}
