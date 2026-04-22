package com.example.schedulemanagerapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "user")
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 유저 고유 식별자

    @Column(nullable = false, length = 4)
    private String username; // 유저명

    @Column(nullable = false, length = 100, unique = true)
    private String email; // 이메일

    @Getter
    @Column(nullable = false)
    private String password; // 비밀번호

    // 생성자
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // 유저 정보 수정
    public void updateUsername(String username) {
        this.username = username;
    }

}
