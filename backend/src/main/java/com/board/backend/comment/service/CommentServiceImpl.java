package com.board.backend.comment.service;

import com.board.backend.board.exception.BoardNotFoundException;
import com.board.backend.board.mapper.BoardMapper;
import com.board.backend.comment.domain.Comment;
import com.board.backend.comment.dto.CommentCreateRequest;
import com.board.backend.comment.dto.CommentResponse;
import com.board.backend.comment.dto.CommentUpdateRequest;
import com.board.backend.comment.exception.CommentCreateFailedException;
import com.board.backend.comment.exception.CommentDeleteFailedException;
import com.board.backend.comment.exception.CommentNotFoundException;
import com.board.backend.comment.exception.CommentUpdateFailedException;
import com.board.backend.comment.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final BoardMapper boardMapper;

    @Override
    public void create(Long boardId, CommentCreateRequest request, Long memberId) {
        if (boardMapper.findById(boardId) == null) {
            throw new BoardNotFoundException();
        }

        validateParentComment(boardId, request.getParentId());

        int result = commentMapper.save(boardId, memberId, request);

        if (result != 1) {
            throw new CommentCreateFailedException();
        }
    }

    @Override
    public List<CommentResponse> getComments(Long boardId) {
        if (boardMapper.findById(boardId) == null) {
            throw new BoardNotFoundException();
        }

        return commentMapper.findByBoardId(boardId)
                .stream()
                .map(CommentResponse::new)
                .toList();
    }

    @Override
    public void update(Long boardId, Long commentId, CommentUpdateRequest request, Long memberId) {
        Comment comment = getExistingComment(commentId);
        validateCommentBelongsToBoard(comment, boardId);
        validateOwner(comment, memberId);
        validateNotDeleted(comment);

        int result = commentMapper.update(commentId, request);

        if (result != 1) {
            throw new CommentUpdateFailedException();
        }
    }

    @Override
    public void delete(Long boardId, Long commentId, Long memberId) {
        Comment comment = getExistingComment(commentId);
        validateCommentBelongsToBoard(comment, boardId);
        validateOwner(comment, memberId);
        validateNotDeleted(comment);

        int result = commentMapper.softDelete(commentId);

        if (result != 1) {
            throw new CommentDeleteFailedException();
        }
    }

    private void validateParentComment(Long boardId, Long parentId) {
        if (parentId == null) {
            return;
        }

        Comment parent = commentMapper.findById(parentId);

        if (parent == null || parent.getDeletedAt() != null || !parent.getBoardId().equals(boardId)) {
            throw new CommentNotFoundException();
        }

        if (parent.getParentId() != null) {
            throw new IllegalArgumentException("대댓글에는 답글을 작성할 수 없습니다.");
        }
    }

    private Comment getExistingComment(Long id) {
        Comment comment = commentMapper.findById(id);

        if (comment == null) {
            throw new CommentNotFoundException();
        }

        return comment;
    }

    private void validateCommentBelongsToBoard(Comment comment, Long boardId) {
        if (!comment.getBoardId().equals(boardId)) {
            throw new CommentNotFoundException();
        }
    }

    private void validateOwner(Comment comment, Long memberId) {
        if (!comment.getMemberId().equals(memberId)) {
            throw new AccessDeniedException("본인이 작성한 댓글만 수정/삭제할 수 있습니다.");
        }
    }

    private void validateNotDeleted(Comment comment) {
        if (comment.getDeletedAt() != null) {
            throw new CommentNotFoundException();
        }
    }
}
