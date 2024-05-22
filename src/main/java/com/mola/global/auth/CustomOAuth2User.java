package com.mola.global.auth;

import com.mola.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    public Member member;

    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", member.getId());
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return member.getMemberRole().getGrantedAuthorities();
    }

    public Member getMember() {
        return member;
    }

    @Override
    public String getName() {
        return member.getNickname();
    }

    public String getImageUrl() {
        return member.getProfileImageUrl();
    }
}

