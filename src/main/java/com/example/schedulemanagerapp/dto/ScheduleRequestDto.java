package com.example.schedulemanagerapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ScheduleRequestDto {

    @NotBlank
    @Size(max = 10)
    private String title;

    @NotBlank
    private String content;
}
