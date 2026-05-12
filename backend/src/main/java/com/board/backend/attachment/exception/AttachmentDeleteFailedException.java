package com.board.backend.attachment.exception;

public class AttachmentDeleteFailedException extends RuntimeException {

    public AttachmentDeleteFailedException() {
        super("Failed to delete attachment.");
    }
}
