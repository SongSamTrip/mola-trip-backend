package com.mola.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class ErrorResponseDto {
    private HttpStatus status;
    private String errorMessage;
}