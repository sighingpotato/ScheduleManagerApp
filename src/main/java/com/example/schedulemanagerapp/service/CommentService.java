package com.example.schedulemanagerapp.service;

import com.example.schedulemanagerapp.dto.CommentRequestDto;
import com.example.schedulemanagerapp.dto.CommentResponseDto;
import com.example.schedulemanagerapp.entity.Comment;
import com.example.schedulemanagerapp.entity.Schedule;
import com.example.schedulemanagerapp.entity.User;
import com.example.schedulemanagerapp.repository.CommentRepository;
import com.example.schedulemanagerapp.repository.ScheduleRepository;
import com.example.schedulemanagerapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseDto createComment(Long scheduleId, CommentRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        Comment comment = new Comment(requestDto.getContent(), user, schedule);
        return new CommentResponseDto(commentRepository.save(comment));
    }

    // 댓글 전체 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllComments(Long scheduleId) {
        List<Comment> commentList = commentRepository.findAllByScheduleId(scheduleId);
        List<CommentResponseDto> responseList = new ArrayList<>();
        for (Comment comment : commentList) {
            responseList.add(new CommentResponseDto(comment));
        }
        return responseList;
    }

    // 댓글 단건 조회
    @Transactional(readOnly = true)
    public CommentResponseDto getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        return new CommentResponseDto(comment);
        }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        if (!comment.getUser().getId().equals(userId))
            throw new IllegalArgumentException("권한 없음");
        comment.update(requestDto.getContent());
        return new CommentResponseDto(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        if (!comment.getUser().getId().equals(userId))
            throw new IllegalArgumentException("권한 없음");
        commentRepository.delete(comment);
    }
}
