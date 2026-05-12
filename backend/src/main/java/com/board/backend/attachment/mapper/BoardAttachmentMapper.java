package com.board.backend.attachment.mapper;

import com.board.backend.attachment.domain.BoardAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardAttachmentMapper {

    int save(BoardAttachment attachment);

    List<BoardAttachment> findByBoardId(Long boardId);

    BoardAttachment findByIdAndBoardId(@Param("id") Long id,
                                       @Param("boardId") Long boardId);

    int countByBoardId(Long boardId);

    int updateFile(BoardAttachment attachment);

    int delete(@Param("id") Long id,
               @Param("boardId") Long boardId);
}
