package com.example.schedulemanagerapp.dto;

import lombok.Getter;

@Getter
public class ErrorResponseDto {
    private final String error;
    private final String message;

    // 생성자
    public ErrorResponseDto(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
