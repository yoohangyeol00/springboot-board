package com.board.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBoardNotFoundException(
            BoardNotFoundException e) {

        ErrorResponse response = new ErrorResponse(false, e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(BoardCreateFailedException.class)
    public ResponseEntity<ErrorResponse> handleBoardCreateFailedException(
            BoardCreateFailedException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(BoardUpdateFailedException.class)
    public ResponseEntity<ErrorResponse> handleBoardUpdateFailedException(
            BoardUpdateFailedException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(BoardDeleteFailedException.class)
    public ResponseEntity<ErrorResponse> handleBoardDeleteFailedException(
            BoardDeleteFailedException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }
}