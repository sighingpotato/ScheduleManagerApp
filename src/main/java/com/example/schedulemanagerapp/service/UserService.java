package com.example.schedulemanagerapp.service;

import com.example.schedulemanagerapp.dto.LoginRequestDto;
import com.example.schedulemanagerapp.dto.SignupRequestDto;
import com.example.schedulemanagerapp.dto.UserResponseDto;
import com.example.schedulemanagerapp.dto.UserRequestDto;
import com.example.schedulemanagerapp.entity.User;
import com.example.schedulemanagerapp.repository.UserRepository;
import com.example.schedulemanagerapp.config.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입
    @Transactional
    public UserResponseDto signup(SignupRequestDto requestDto) {
        // 비밀번호를 암호화하여 저장
        String encodedPassword = passwordEncoder.encode((requestDto.getPassword()));
        User user = new User(requestDto.getUsername(), requestDto.getEmail(), encodedPassword);
        return new UserResponseDto(userRepository.save(user));
    }

    // 로그인
    @Transactional(readOnly = true)
    public User login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");

        return user;
    }

    // 유저 전체 조회
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        List<User> userList = userRepository.findAll();
        List<UserResponseDto> responseDtoList = new ArrayList<>();

        // for문을 돌면서 유저를 하나씩 꺼낸다.
        for (User user : userList) {
            UserResponseDto dto = new UserResponseDto(user);

            responseDtoList.add(dto);
        }
        return responseDtoList;
    }

    // 유저 단건 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long id) {
        return new UserResponseDto(userRepository.findById(id).orElseThrow());
    }

    // 유저 수정
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto requestDto, Long sessionUserId) {
        if (!id.equals(sessionUserId)) throw new IllegalArgumentException("권한이 없습니다.");
        User user = userRepository.findById(id).orElseThrow();
        user.updateUsername(requestDto.getUsername());
        return new UserResponseDto(user);
    }

    // 유저 삭제
    @Transactional
    public void deleteUser(Long id, Long sessionUserId) {
        if (!id.equals(sessionUserId)) throw new IllegalArgumentException("권한이 없습니다.");
        userRepository.deleteById(id);
    }
}
