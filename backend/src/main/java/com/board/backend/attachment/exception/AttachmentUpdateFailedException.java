package com.board.backend.attachment.exception;

public class AttachmentUpdateFailedException extends RuntimeException {

    public AttachmentUpdateFailedException() {
        super("Failed to update attachment.");
    }
}
