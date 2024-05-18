package com.mola.domain.tripBoard.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Setter
@Getter
public class TripImageDto {

    private Long id;

    private String url;

    private Long tripPostId;
}
