package com.mola.domain.member.dto;

import com.mola.domain.trip.dto.TripPlanDto;
import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Setter
@Getter
public class MemberActivityProfile {

    //내가쓴글
    //내가쓴댓글
    //내가간 여행
    //내가누른좋아요
    private List<CommentDto> commentList;
    private List<TripPlanDto> tripPlanList;
    private List<TripPostDto> myTripPostList;
    private List<TripPostDto> likeTripPostList;

}
