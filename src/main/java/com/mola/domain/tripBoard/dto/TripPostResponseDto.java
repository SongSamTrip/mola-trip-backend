package com.mola.domain.tripBoard.dto;

import com.mola.domain.tripBoard.entity.TripPostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Setter
@Getter
public class TripPostResponseDto {

    @NotNull
    private Long id;

    private String writer;

    @NotNull
    private String name;

    @NotNull
    private String content;

    private TripPostStatus tripPostStatus;

    private int commentCount;

    @Builder.Default
    private List<String> imageList = new ArrayList<>();

    private int likeCount;
}
