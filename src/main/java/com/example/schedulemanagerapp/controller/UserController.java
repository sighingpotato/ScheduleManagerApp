package com.example.schedulemanagerapp.controller;

import com.example.schedulemanagerapp.dto.LoginRequestDto;
import com.example.schedulemanagerapp.dto.SignupRequestDto;
import com.example.schedulemanagerapp.dto.UserResponseDto;
import com.example.schedulemanagerapp.dto.UserRequestDto;
import com.example.schedulemanagerapp.entity.User;
import com.example.schedulemanagerapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    private Long getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("LOGIN_USER_ID") == null)
            throw new IllegalArgumentException("로그인이 필요합니다.");
        return (Long) session.getAttribute("LOGIN_USER_ID");
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(requestDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletRequest request) {

        // 서비스에 로그인 검증 요청
        User user = userService.login(requestDto);

        // 로그인 시, 세션 생성 및 유저 id 저장
        HttpSession session = request.getSession(true);
        session.setAttribute("LOGIN_USER_ID", user.getId());
        return ResponseEntity.ok().build();
    }

    // 유저 전체 조회
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 유저 단건 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    // 유저 수정
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId, @Valid @RequestBody UserRequestDto requestDto, HttpServletRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, requestDto, getUserIdFromSession(request)));
    }

    // 유저 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        userService.deleteUser(userId, getUserIdFromSession(request));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
