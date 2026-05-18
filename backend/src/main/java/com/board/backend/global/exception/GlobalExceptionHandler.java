package com.board.backend.global.exception;

import com.board.backend.attachment.exception.AttachmentDeleteFailedException;
import com.board.backend.attachment.exception.AttachmentNotFoundException;
import com.board.backend.attachment.exception.AttachmentSaveFailedException;
import com.board.backend.attachment.exception.AttachmentUpdateFailedException;
import com.board.backend.board.exception.BoardCreateFailedException;
import com.board.backend.board.exception.BoardDeleteFailedException;
import com.board.backend.board.exception.BoardNotFoundException;
import com.board.backend.board.exception.BoardUpdateFailedException;
import com.board.backend.comment.exception.CommentCreateFailedException;
import com.board.backend.comment.exception.CommentDeleteFailedException;
import com.board.backend.comment.exception.CommentNotFoundException;
import com.board.backend.comment.exception.CommentUpdateFailedException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("Request body validation failed message={}", message, e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("Request parameter type mismatch parameter={}, value={}", e.getName(), e.getValue(), e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "요청 값의 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException e) {
        log.warn("JWT validation failed", e);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(false, "인증 토큰이 유효하지 않습니다."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {
        log.error("Data integrity violation", e);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(false, "이미 사용 중인 값이거나 데이터 제약 조건을 위반했습니다."));
    }

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBoardNotFoundException(
            BoardNotFoundException e) {
        log.warn("Board not found message={}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(BoardCreateFailedException.class)
    public ResponseEntity<ErrorResponse> handleBoardCreateFailedException(
            BoardCreateFailedException e) {
        log.error("Board create failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(BoardUpdateFailedException.class)
    public ResponseEntity<ErrorResponse> handleBoardUpdateFailedException(
            BoardUpdateFailedException e) {
        log.error("Board update failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(BoardDeleteFailedException.class)
    public ResponseEntity<ErrorResponse> handleBoardDeleteFailedException(
            BoardDeleteFailedException e) {
        log.error("Board delete failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(AttachmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAttachmentNotFoundException(
            AttachmentNotFoundException e) {
        log.warn("Attachment not found message={}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(AttachmentSaveFailedException.class)
    public ResponseEntity<ErrorResponse> handleAttachmentSaveFailedException(
            AttachmentSaveFailedException e) {
        log.error("Attachment save failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(AttachmentUpdateFailedException.class)
    public ResponseEntity<ErrorResponse> handleAttachmentUpdateFailedException(
            AttachmentUpdateFailedException e) {
        log.error("Attachment update failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(AttachmentDeleteFailedException.class)
    public ResponseEntity<ErrorResponse> handleAttachmentDeleteFailedException(
            AttachmentDeleteFailedException e) {
        log.error("Attachment delete failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFoundException(
            CommentNotFoundException e) {
        log.warn("Comment not found message={}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(CommentCreateFailedException.class)
    public ResponseEntity<ErrorResponse> handleCommentCreateFailedException(
            CommentCreateFailedException e) {
        log.error("Comment create failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(CommentUpdateFailedException.class)
    public ResponseEntity<ErrorResponse> handleCommentUpdateFailedException(
            CommentUpdateFailedException e) {
        log.error("Comment update failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(CommentDeleteFailedException.class)
    public ResponseEntity<ErrorResponse> handleCommentDeleteFailedException(
            CommentDeleteFailedException e) {
        log.error("Comment delete failed", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e) {
        log.warn("Access denied message={}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e) {
        log.warn("Invalid request message={}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException e) {
        log.error("Invalid server state", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Internal server error", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "서버 내부 오류가 발생했습니다."));
    }
}
