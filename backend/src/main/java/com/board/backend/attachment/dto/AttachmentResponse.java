package com.board.backend.attachment.dto;

import com.board.backend.attachment.domain.BoardAttachment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AttachmentResponse {

    private final Long id;
    private final Long boardId;
    private final String originalName;
    private final String fileUrl;
    private final Long fileSize;
    private final String contentType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AttachmentResponse(BoardAttachment attachment) {
        this.id = attachment.getId();
        this.boardId = attachment.getBoardId();
        this.originalName = attachment.getOriginalName();
        this.fileUrl = attachment.getFileUrl();
        this.fileSize = attachment.getFileSize();
        this.contentType = attachment.getContentType();
        this.createdAt = attachment.getCreatedAt();
        this.updatedAt = attachment.getUpdatedAt();
    }
}
