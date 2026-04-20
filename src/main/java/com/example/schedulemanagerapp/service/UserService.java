package com.example.schedulemanagerapp.service;

import com.example.schedulemanagerapp.entity.User;
import com.example.schedulemanagerapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 회원가입
    @Transactional
    public User signup(String username, String email, String password) {
        // 이미 가입된 이메일인지 확인하기
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 유저 객체 생성, 저장
        User user = new User(username, email, password);
        return userRepository.save(user);
    }

    // 로그인
    public User login(String email, String password) {
        // 이메일로 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호 일치 확인
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        return user;
    }
}
