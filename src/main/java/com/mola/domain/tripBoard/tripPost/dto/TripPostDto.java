package com.mola.domain.tripBoard.tripPost.dto;

import com.mola.domain.tripBoard.tripImage.dto.TripImageDto;
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

    private Long memberId;

    @NotNull
    private String name;

    @NotNull
    private String content;

    private List<TripImageDto> tripImageDtos;
}
