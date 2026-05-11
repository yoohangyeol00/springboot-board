package com.board.backend.comment.mapper;

import com.board.backend.comment.domain.Comment;
import com.board.backend.comment.dto.CommentCreateRequest;
import com.board.backend.comment.dto.CommentUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    int save(@Param("boardId") Long boardId,
             @Param("memberId") Long memberId,
             @Param("request") CommentCreateRequest request);

    List<Comment> findByBoardId(Long boardId);

    Comment findById(Long id);

    int update(@Param("id") Long id,
               @Param("request") CommentUpdateRequest request);

    int softDelete(Long id);
}
