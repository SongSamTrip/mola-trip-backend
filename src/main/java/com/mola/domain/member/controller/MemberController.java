package com.mola.domain.member.controller;

import com.mola.domain.member.dto.MemberActivityProfile;
import com.mola.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/info")
    public ResponseEntity<MemberActivityProfile> memberInfo(){
        MemberActivityProfile memberActivityProfile = memberService.getMemberActivity();
        return ResponseEntity.ok(memberActivityProfile);
    }
}
