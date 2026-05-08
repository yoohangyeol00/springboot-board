package com.board.backend.member.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Member {

    private Long id;
    private String loginId;
    private String passwordHash;
    private String nickname;
    private String role;
    private String status;
    private Integer failedLoginCount;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
