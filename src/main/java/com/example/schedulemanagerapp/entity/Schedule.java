package com.example.schedulemanagerapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "schedules")
public class Schedule extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 일정 고유 식별자

    @Column(nullable = false, length = 10)
    private String title; // 일정 제목

    @Column(nullable = false)
    private String content; // 일정 내용

    @ManyToOne(fetch = FetchType.LAZY) // 다대일(N:1) 단방향 연관관계 설정(한 명의 유저가 여러 개의 일정 생성 가능)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작성자 유저 정보

    // 댓글 개수 세기 위한 리스트
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public Schedule(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
