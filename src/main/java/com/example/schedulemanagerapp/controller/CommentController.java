package com.example.schedulemanagerapp.controller;

import com.example.schedulemanagerapp.dto.CommentRequestDto;
import com.example.schedulemanagerapp.dto.CommentResponseDto;
import com.example.schedulemanagerapp.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    private Long getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("LOGIN_USER_ID") == null)
            throw new IllegalArgumentException("로그인 필요");
        return (Long) session.getAttribute("LOGIN_USER_ID");
    }

    // 댓글 생성
    @PostMapping("/api/schedules/{scheduleId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long scheduleId,
            @Valid @RequestBody CommentRequestDto requestDto,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(scheduleId, requestDto, getUserIdFromSession(request)));
    }

    // 댓글 전체 조회
    @GetMapping("/api/schedules/{scheduleId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getAllComments(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(commentService.getAllComments(scheduleId));
    }

    // 댓글 단건 조회
    @GetMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> getComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getComment(commentId));
    }

    // 댓글 수정
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto,
            HttpServletRequest request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, requestDto, getUserIdFromSession(request)));
    }

    // 댓글 삭제
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        commentService.deleteComment(commentId, getUserIdFromSession(request));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
