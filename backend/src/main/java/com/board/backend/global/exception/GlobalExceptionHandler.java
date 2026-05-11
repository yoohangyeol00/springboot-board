package com.board.backend.global.exception;

import com.board.backend.board.exception.BoardCreateFailedException;
import com.board.backend.board.exception.BoardDeleteFailedException;
import com.board.backend.board.exception.BoardNotFoundException;
import com.board.backend.board.exception.BoardUpdateFailedException;
import com.board.backend.comment.exception.CommentCreateFailedException;
import com.board.backend.comment.exception.CommentDeleteFailedException;
import com.board.backend.comment.exception.CommentNotFoundException;
import com.board.backend.comment.exception.CommentUpdateFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, message));
    }

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBoardNotFoundException(
            BoardNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, e.getMessage()));
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

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFoundException(
            CommentNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(CommentCreateFailedException.class)
    public ResponseEntity<ErrorResponse> handleCommentCreateFailedException(
            CommentCreateFailedException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(CommentUpdateFailedException.class)
    public ResponseEntity<ErrorResponse> handleCommentUpdateFailedException(
            CommentUpdateFailedException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(CommentDeleteFailedException.class)
    public ResponseEntity<ErrorResponse> handleCommentDeleteFailedException(
            CommentDeleteFailedException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }
}
