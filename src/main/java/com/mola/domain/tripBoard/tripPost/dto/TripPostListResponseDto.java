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

    private String writer;

    private String imageUrl;

    private int commentCount;

    private int likeCount;
}
