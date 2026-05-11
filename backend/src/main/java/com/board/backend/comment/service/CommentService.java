package com.board.backend.comment.service;

import com.board.backend.comment.dto.CommentCreateRequest;
import com.board.backend.comment.dto.CommentResponse;
import com.board.backend.comment.dto.CommentUpdateRequest;

import java.util.List;

public interface CommentService {

    void create(Long boardId, CommentCreateRequest request, Long memberId);

    List<CommentResponse> getComments(Long boardId);

    void update(Long boardId, Long commentId, CommentUpdateRequest request, Long memberId);

    void delete(Long boardId, Long commentId, Long memberId);
}
