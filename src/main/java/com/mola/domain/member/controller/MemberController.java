package com.mola.domain.member.controller;

import com.mola.domain.member.dto.MemberResponseDto;
import com.mola.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members/admin")
    public ResponseEntity<Page<MemberResponseDto>> adminMemberInfos(Pageable pageable){
        return ResponseEntity.ok(memberService.getMembersForAdmin(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/members/admin/{id}")
    public ResponseEntity<?> adminDeleteMember(@PathVariable("id") Long id){
        memberService.adminDeleteMember(id);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/members/admin")
    public ResponseEntity<Long> requestAdmin(@RequestParam("secretKey") String secretKey) {
        return ResponseEntity.ok(memberService.requestAdmin(secretKey));
    }
}
