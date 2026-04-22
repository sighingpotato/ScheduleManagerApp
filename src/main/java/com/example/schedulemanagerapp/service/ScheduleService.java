package com.example.schedulemanagerapp.service;

import com.example.schedulemanagerapp.dto.SchedulePageResponseDto;
import com.example.schedulemanagerapp.dto.ScheduleRequestDto;
import com.example.schedulemanagerapp.dto.ScheduleResponseDto;
import com.example.schedulemanagerapp.entity.Schedule;
import com.example.schedulemanagerapp.entity.User;
import com.example.schedulemanagerapp.repository.ScheduleRepository;
import com.example.schedulemanagerapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    // 일정 생성
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Schedule schedule = new Schedule(requestDto.getTitle(), requestDto.getContent(), user);
        return new ScheduleResponseDto(scheduleRepository.save(schedule));
    }

    // 일정 전체 조회
//    @Transactional(readOnly = true)
//    public List<ScheduleResponseDto> getAllSchedules() {
//        List<Schedule> scheduleList = scheduleRepository.findAll();
//        List<ScheduleResponseDto> responseDtoList = new ArrayList<>();
//
//        for (Schedule schedule : scheduleList) {
//            ScheduleResponseDto dto = new ScheduleResponseDto(schedule);
//
//            responseDtoList.add(dto);
//        }
//        return responseDtoList;
//    }


    // 일정 단건 조회
    @Transactional(readOnly = true)
    public ScheduleResponseDto getSchedule(Long id) {
        return new ScheduleResponseDto(scheduleRepository.findById(id).orElseThrow());
    }

    // 일정 수정
    @Transactional
    public ScheduleResponseDto updateSchedule(Long id, ScheduleRequestDto requestDto, Long userId) {
        Schedule schedule = scheduleRepository.findById(id).orElseThrow();
        if (!schedule.getUser().getId().equals(userId))
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        schedule.update(requestDto.getTitle(), requestDto.getContent());
        return new ScheduleResponseDto(schedule);
    }

    // 일정 삭제
    @Transactional
    public void deleteSchedule(Long id, Long userId) {
        Schedule schedule = scheduleRepository.findById(id).orElseThrow();
        if (!schedule.getUser().getId().equals(userId))
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        scheduleRepository.delete(schedule);
    }

    // 일정 페이징 조회
    @Transactional(readOnly = true)
    public Page<SchedulePageResponseDto> getSchedules(Pageable pageable) {
        Page<Schedule> schedulePage = scheduleRepository.findAll(pageable);
        List<SchedulePageResponseDto> responseDtoList = new ArrayList<>();

        for (Schedule schedule : schedulePage.getContent()) {
            SchedulePageResponseDto dto = new SchedulePageResponseDto(schedule);

            responseDtoList.add(dto);
        }
        return new PageImpl<>(responseDtoList, pageable, schedulePage.getTotalElements());
    }
}
