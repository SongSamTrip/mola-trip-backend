package com.mola.domain.member.service;

import com.mola.domain.member.dto.oauthInfo.*;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.entity.MemberRole;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.global.auth.CustomOAuth2User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        String oAuth2ClientName = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = getUserInfo(oAuth2ClientName, oAuth2User.getAttributes());

        Member member = findOrSaveMember(userInfo);
        return new CustomOAuth2User(member);
    }

    public Member findOrSaveMember(OAuth2UserInfo userInfo) {
        return memberRepository.findByPersonalId(userInfo.getPersonalId())
                .orElseGet(() -> saveMember(userInfo));
    }

    private Member saveMember(OAuth2UserInfo userInfo) {
        Member newMember = Member.builder()
                .loginProvider(userInfo.getLoginProvider())
                .personalId(userInfo.getPersonalId())
                .nickname(userInfo.getNickname())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .memberRole(MemberRole.USER)
                .build();

        memberRepository.save(newMember);
        return newMember;
    }

    public static OAuth2UserInfo getUserInfo(String registrationId, Map<String, Object> attributes) {

        attributes.forEach((key, value) -> System.out.println(key + ": " + value));

        switch (registrationId) {
            case "google":
                return new GoogleUserInfo(attributes);
            case "kakao":
                return new KakaoUserInfo(attributes);
            case "naver":
                return new NaverUserInfo(attributes);
            case "github":
                return new GithubUserInfo(attributes);
            default:
                throw new IllegalArgumentException("Unsupported provider " + registrationId);
        }
    }

}
