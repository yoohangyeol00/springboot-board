package com.board.backend.comment.exception;

public class CommentDeleteFailedException extends RuntimeException {

    public CommentDeleteFailedException() {
        super("댓글 삭제에 실패했습니다.");
    }
}
