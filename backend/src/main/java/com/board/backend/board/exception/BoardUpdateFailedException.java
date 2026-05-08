package com.board.backend.board.exception;

public class BoardUpdateFailedException extends RuntimeException {
    public BoardUpdateFailedException() {
        super("게시글 수정에 실패했습니다.");
    }
}
