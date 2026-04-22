package com.example.schedulemanagerapp.controller;

import com.example.schedulemanagerapp.dto.SchedulePageResponseDto;
import com.example.schedulemanagerapp.dto.ScheduleRequestDto;
import com.example.schedulemanagerapp.dto.ScheduleResponseDto;
import com.example.schedulemanagerapp.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    public Long getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        // 인증, 인가 세션 검증
        if (session == null || session.getAttribute("LOGIN_USER_ID") == null) throw new IllegalArgumentException("로그인이 필요합니다.");
        return (Long) session.getAttribute("LOGIN_USER_ID");
    }

    // 일정 생성
    @PostMapping
    public ResponseEntity<ScheduleResponseDto> createSchedule(@Valid @RequestBody ScheduleRequestDto requestDto, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(requestDto, getUserIdFromSession(request)));
    }

    // 일정 전체 조회
//    @GetMapping
//    public ResponseEntity<List<ScheduleResponseDto>> getAllSchedules() {
//        return ResponseEntity.ok(scheduleService.getAllSchedules());
//    }

    // 일정 단건 조회
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> getSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.getSchedule(scheduleId));
    }

    // 일정 수정
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(@PathVariable Long scheduleId, @Valid @RequestBody ScheduleRequestDto requestDto, HttpServletRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, requestDto, getUserIdFromSession(request)));
    }

    // 일정 삭제
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId, HttpServletRequest request) {
        scheduleService.deleteSchedule(scheduleId, getUserIdFromSession(request));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 일정 페이징 조회
    @GetMapping
    public ResponseEntity<Page<SchedulePageResponseDto>> getSchedules(
            // 수정일 내림차순 정렬
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getSchedules(pageable));
    }
}
