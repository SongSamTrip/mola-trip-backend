package com.mola.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum GlobalErrorCode implements ErrorCode {

    UnAuthorized(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    AccessDenied(HttpStatus.FORBIDDEN, "권한이 없는 사용자입니다."),
    InvalidTripFriends(HttpStatus.BAD_REQUEST, "해당 TripPlan에 속한 사용자가 아닙니다."),
    InvalidDestination(HttpStatus.BAD_REQUEST, "잘못된 목적지 형식입니다."),
    InvalidMemberIdentifierFormat(HttpStatus.BAD_REQUEST, "잘못된 회원 식별자 형식입니다."),
    InvalidTripPlanIdentifierFormat(HttpStatus.BAD_REQUEST, "잘못된 TripPlan 식별자 형식입니다."),
    InvalidTripPlan(HttpStatus.BAD_REQUEST, "유효하지 않은 TripPlan 입니다."),
    MissingTripPlanIdentifier(HttpStatus.BAD_REQUEST, "TripPlan 식별자가 누락되었습니다."),
    InvalidTripPostIdentifier(HttpStatus.BAD_REQUEST, "유효하지 않은 여행 식별자입니다."),
    InvalidTrip(HttpStatus.BAD_REQUEST, "유효하지 않은 여행입니다."),
    InvalidImageType(HttpStatus.BAD_REQUEST, "잘못된 형식의 이미지 파일입니다."),
    MissingRequireData(HttpStatus.BAD_REQUEST, "필수 입력값을 입력하세요."),
    InvalidCommentIdentifier(HttpStatus.BAD_REQUEST, "유효하지 않은 댓글입니다."),
    DuplicateLike(HttpStatus.BAD_REQUEST, "이미 좋아요를 누른 게시글입니다."),
    ExcessiveRetries(HttpStatus.INTERNAL_SERVER_ERROR, "잠시 후 다시 시도해주세요."),
    BadRequest(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");


    private final HttpStatus httpStatus;
    private final String errorMessage;
}