package com.mola.global.util;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SecurityUtil {

    private final MemberRepository memberRepository;

    public Member findCurrentMember() {
        UserDetails userDetails = getAuthenticatedUser();

        return validateMember(userDetails.getUsername());
    }

    public UserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null) {
            throw new CustomException(GlobalErrorCode.UnAuthorized);
        }
        return (UserDetails) authentication.getPrincipal();
    }

    public Member validateMember(String personalId) {
        return memberRepository.findById(Long.valueOf(personalId))
                .orElseThrow(() -> new CustomException(GlobalErrorCode.UnAuthorized));
    }

}
