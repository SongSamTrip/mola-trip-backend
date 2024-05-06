package com.mola.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
}
