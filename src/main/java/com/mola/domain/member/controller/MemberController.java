package com.mola.domain.member.controller;

import com.mola.domain.member.dto.MemberActivityProfile;
import com.mola.domain.member.dto.MemberResponseDto;
import com.mola.domain.member.service.MemberService;
import com.mola.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    private final SecurityUtil securityUtil;

    @GetMapping("/member/info")
    public ResponseEntity<MemberActivityProfile> memberInfo(){
        MemberActivityProfile memberActivityProfile = memberService.getMemberActivity();
        return ResponseEntity.ok(memberActivityProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members/admin")
    public ResponseEntity<Page<MemberResponseDto>> adminMemberInfos(Pageable pageable){
        return ResponseEntity.ok(memberService.getMembersForAdmin(pageable));
    }

    @PostMapping("/members/admin")
    public ResponseEntity<Long> requestAdmin(@RequestBody String secretKey) {
        System.out.println(securityUtil.getAuthenticatedUser());
        UserDetails authenticatedUser = securityUtil.getAuthenticatedUser();
        System.out.println(authenticatedUser.getAuthorities());
        return ResponseEntity.ok(memberService.requestAdmin(secretKey));
    }
}
