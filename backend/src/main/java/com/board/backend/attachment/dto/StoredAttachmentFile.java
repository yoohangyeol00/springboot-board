package com.board.backend.attachment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredAttachmentFile {

    private String originalName;
    private String storedName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
}
