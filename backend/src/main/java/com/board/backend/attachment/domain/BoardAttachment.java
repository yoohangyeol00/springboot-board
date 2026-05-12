package com.board.backend.attachment.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardAttachment {

    private Long id;
    private Long boardId;
    private String originalName;
    private String storedName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
