package com.board.backend.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberWithdrawRequest {

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
