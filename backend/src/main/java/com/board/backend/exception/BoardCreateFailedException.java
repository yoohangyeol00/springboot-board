package com.board.backend.exception;

public class BoardCreateFailedException extends RuntimeException {
    public BoardCreateFailedException() {
        super("게시글 등록에 실패했습니다.");
    }
}