package com.board.backend.attachment.exception;

public class AttachmentNotFoundException extends RuntimeException {

    public AttachmentNotFoundException() {
        super("Attachment not found.");
    }
}
