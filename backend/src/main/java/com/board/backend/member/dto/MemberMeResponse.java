package com.board.backend.member.dto;

import com.board.backend.member.domain.Member;
import lombok.Getter;

@Getter
public class MemberMeResponse {

    private final Long id;
    private final String loginId;
    private final String nickname;
    private final String role;

    public MemberMeResponse(Member member) {
        this.id = member.getId();
        this.loginId = member.getLoginId();
        this.nickname = member.getNickname();
        this.role = member.getRole();
    }
}
