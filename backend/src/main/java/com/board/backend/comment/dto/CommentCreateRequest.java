package com.board.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {

    private Long id;

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;

    private Long parentId;
}
