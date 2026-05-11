package com.board.backend.comment.exception;

public class CommentCreateFailedException extends RuntimeException {

    public CommentCreateFailedException() {
        super("댓글 등록에 실패했습니다.");
    }
}
