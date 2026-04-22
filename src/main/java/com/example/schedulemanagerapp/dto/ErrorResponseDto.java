package com.example.schedulemanagerapp.dto;

import lombok.Getter;

@Getter
public class ErrorResponseDto {
    private String error;
    private String message;

    // 생성자
    public ErrorResponseDto(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
