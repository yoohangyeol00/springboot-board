package com.board.backend.member.controller;

import com.board.backend.global.security.LoginMember;
import com.board.backend.member.dto.MemberLoginRequest;
import com.board.backend.member.dto.MemberLoginResponse;
import com.board.backend.member.dto.MemberMeResponse;
import com.board.backend.member.dto.MemberSignupRequest;
import com.board.backend.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public void signup(@Valid @RequestBody MemberSignupRequest request) {
        memberService.signup(request);
    }

    @PostMapping("/login")
    public MemberLoginResponse login(@Valid @RequestBody MemberLoginRequest request) {
        return memberService.login(request);
    }

    @GetMapping("/me")
    public MemberMeResponse me(@AuthenticationPrincipal LoginMember loginMember) {
        return memberService.getMe(loginMember.getId());
    }
}
