package com.board.backend.attachment.exception;

public class AttachmentSaveFailedException extends RuntimeException {

    public AttachmentSaveFailedException() {
        super("Failed to save attachment.");
    }
}
