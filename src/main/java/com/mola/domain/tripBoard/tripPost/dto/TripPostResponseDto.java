package com.mola.domain.tripBoard.tripPost.dto;

import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Setter
@Getter
public class TripPostResponseDto {

    @NotNull
    private Long id;

    private Long memberId;

    private String nickname;

    @NotNull
    private String name;

    @NotNull
    private String content;

    private TripPostStatus tripPostStatus;

    private int commentCount;

    private int likeCount;
}
