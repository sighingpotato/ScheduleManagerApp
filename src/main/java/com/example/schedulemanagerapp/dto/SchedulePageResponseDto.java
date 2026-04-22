package com.example.schedulemanagerapp.dto;

import com.example.schedulemanagerapp.entity.Schedule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class SchedulePageResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final int commentCount; // 댓글 개수
    private final String username; // 작성자 유저명
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public SchedulePageResponseDto(Schedule schedule) {
        this.id = schedule.getId();
        this.title = schedule.getTitle();
        this.content = schedule.getContent();
        this.commentCount = schedule.getComments().size();
        this.username = schedule.getUser().getUsername();
        this.createdAt = schedule.getCreatedAt();
        this.updatedAt = schedule.getUpdatedAt();
    }
}
