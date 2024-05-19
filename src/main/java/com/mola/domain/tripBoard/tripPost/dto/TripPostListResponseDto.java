package com.mola.domain.tripBoard.tripPost.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TripPostListResponseDto {

    private Long id;

    private String name;

    private String member;

    private String preview;

    private int commentCount;

    private int likeCount;
}
