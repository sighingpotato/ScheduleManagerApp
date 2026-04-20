package com.example.schedulemanagerapp.controller;

import com.example.schedulemanagerapp.dto.LoginRequestDto;
import com.example.schedulemanagerapp.dto.SignupRequestDto;
import com.example.schedulemanagerapp.entity.User;
import com.example.schedulemanagerapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto.getUsername(), requestDto.getEmail(), requestDto.getPassword());

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletRequest request) {

        // 서비스에 로그인 검증 요청
        User loginUser = userService.login(requestDto.getEmail(), requestDto.getPassword());

        // 로그인 시, 세션 생성 및 유저 id 저장
        HttpSession session = request.getSession(true);
        session.setAttribute("LOGIN_USER_ID", loginUser.getId());

        return ResponseEntity.ok("로그인 성공");
    }
}
