package com.board.backend.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberLoginResponse {

    private final String accessToken;
    private final String refreshToken;
}
