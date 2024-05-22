package com.mola.domain.tripBoard.tripPost.dto;

import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import lombok.*;

import java.time.LocalDateTime;

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

    private TripPostStatus tripPostStatus;

    private int commentCount;

    private int likeCount;

    private LocalDateTime createdDate;
}
