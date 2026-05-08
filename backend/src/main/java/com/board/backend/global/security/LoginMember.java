package com.board.backend.global.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginMember {

    private final Long id;
    private final String loginId;
    private final String role;
}
