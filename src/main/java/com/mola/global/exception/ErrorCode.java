package com.mola.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    UnAuthorized(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    AccessDenied(HttpStatus.FORBIDDEN, "권한이 없는 사용자입니다."),
    InvalidTripFriends(HttpStatus.BAD_REQUEST, "해당 TripPlan에 속한 사용자가 아닙니다."),
    InvalidDestination(HttpStatus.BAD_REQUEST, "잘못된 목적지 형식입니다."),
    InvalidMemberIdentifierFormat(HttpStatus.BAD_REQUEST, "잘못된 회원 식별자 형식입니다."),
    InvalidTripPlanIdentifierFormat(HttpStatus.BAD_REQUEST, "잘못된 TripPlan 식별자 형식입니다."),
    MissingTripPlanIdentifier(HttpStatus.BAD_REQUEST, "TripPlan 식별자가 누락되었습니다.");




    private final HttpStatus httpStatus;
    private final String errorMessage;
}