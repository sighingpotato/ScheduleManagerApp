package com.example.schedulemanagerapp.entity;

import jakarta.persistence.Column;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

public class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime creadtedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
