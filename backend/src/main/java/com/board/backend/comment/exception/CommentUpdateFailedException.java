package com.board.backend.comment.exception;

public class CommentUpdateFailedException extends RuntimeException {

    public CommentUpdateFailedException() {
        super("댓글 수정에 실패했습니다.");
    }
}
