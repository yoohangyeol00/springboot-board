package com.board.backend.notification.dto;

import com.board.backend.notification.domain.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {
    private final Long id;
    private final Long boardId;
    private final Long commentId;
    private final String message;
    private final Boolean read;
    private final LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.boardId = notification.getBoardId();
        this.commentId = notification.getCommentId();
        this.message = notification.getMessage();
        this.read = notification.getRead();
        this.createdAt = notification.getCreatedAt();
    }
}
