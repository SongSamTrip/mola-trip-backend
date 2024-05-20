package com.mola.domain.tripBoard.tripPost.dto;

import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
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

    private Long memberId;

    private String nickname;

    @NotNull
    private String name;

    @NotNull
    private String content;

    private TripPostStatus tripPostStatus;

    private List<CommentDto> commentDtos;

    private int likeCount;

    private boolean isLike;


    public TripPostResponseDto(Long id, Long memberId, String nickname, String name, String content, TripPostStatus tripPostStatus, int likeCount, boolean isLike) {
        this.id = id;
        this.memberId = memberId;
        this.nickname = nickname;
        this.name = name;
        this.content = content;
        this.tripPostStatus = tripPostStatus;
        this.likeCount = likeCount;
        this.isLike = isLike;
        this.commentDtos = new ArrayList<>(); // 댓글 목록 초기화
    }
}
