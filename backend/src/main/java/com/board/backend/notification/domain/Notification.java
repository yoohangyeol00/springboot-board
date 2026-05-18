package com.board.backend.notification.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Notification {
    private Long id;
    private Long receiverId;
    private Long boardId;
    private Long commentId;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}
