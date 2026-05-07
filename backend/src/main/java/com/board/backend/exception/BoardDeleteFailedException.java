package com.board.backend.exception;

public class BoardDeleteFailedException extends RuntimeException {

    public BoardDeleteFailedException() {
        super("게시글 삭제에 실패했습니다.");
    }
}