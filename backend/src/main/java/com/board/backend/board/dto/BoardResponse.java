package com.board.backend.board.dto;

import com.board.backend.attachment.dto.AttachmentResponse;
import com.board.backend.board.domain.Board;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BoardResponse {

    private final Long id;
    private final Long memberId;
    private final String title;
    private final String content;
    private final String writer;
    private final Integer viewCount;
    private final Integer commentCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<AttachmentResponse> attachments;

    public BoardResponse(Board board) {
        this(board, List.of());
    }

    public BoardResponse(Board board, List<AttachmentResponse> attachments) {
        this.id = board.getId();
        this.memberId = board.getMemberId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.writer = board.getWriter();
        this.viewCount = board.getViewCount();
        this.commentCount = board.getCommentCount() == null ? 0 : board.getCommentCount();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        this.attachments = attachments;
    }
}
