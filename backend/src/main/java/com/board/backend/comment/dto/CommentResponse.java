package com.board.backend.comment.dto;

import com.board.backend.comment.domain.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    private final Long id;
    private final Long boardId;
    private final Long memberId;
    private final Long parentId;
    private final String content;
    private final String writer;
    private final boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.boardId = comment.getBoardId();
        this.memberId = comment.getMemberId();
        this.parentId = comment.getParentId();
        this.deleted = comment.getDeletedAt() != null;
        this.content = deleted ? "삭제된 댓글입니다." : comment.getContent();
        this.writer = comment.getWriter();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
