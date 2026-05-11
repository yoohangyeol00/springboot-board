package com.board.backend.comment.controller;

import com.board.backend.comment.dto.CommentCreateRequest;
import com.board.backend.comment.dto.CommentResponse;
import com.board.backend.comment.dto.CommentUpdateRequest;
import com.board.backend.comment.service.CommentService;
import com.board.backend.global.security.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/boards/{boardId}/comments")
    public void create(
            @PathVariable Long boardId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal LoginMember loginMember) {
        commentService.create(boardId, request, loginMember.getId());
    }

    @GetMapping("/boards/{boardId}/comments")
    public List<CommentResponse> getComments(@PathVariable Long boardId) {
        return commentService.getComments(boardId);
    }

    @PutMapping("/boards/{boardId}/comments/{commentId}")
    public void update(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal LoginMember loginMember) {
        commentService.update(boardId, commentId, request, loginMember.getId());
    }

    @DeleteMapping("/boards/{boardId}/comments/{commentId}")
    public void delete(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal LoginMember loginMember) {
        commentService.delete(boardId, commentId, loginMember.getId());
    }
}
