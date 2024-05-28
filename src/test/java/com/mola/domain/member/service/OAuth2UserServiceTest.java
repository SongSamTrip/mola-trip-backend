package com.mola.domain.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mola.domain.member.dto.oauthInfo.OAuth2UserInfo;
import com.mola.domain.member.entity.LoginProvider;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @InjectMocks
    OAuth2UserService oAuth2UserService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    OAuth2UserRequest mockOAuth2UserRequest;

    @DisplayName("지원하지 않는 공급자에 대한 예외 처리 테스트")
    @Test
    void testGetUnsupportedProviderUserInfo() {
        Map<String, Object> attributes = Map.of("id", "4444");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                OAuth2UserService.getUserInfo("unsupported", attributes)
        );
        assertEquals("Unsupported provider unsupported", exception.getMessage());
    }

    @DisplayName("Kakao 사용자 정보 생성 테스트")
    @Test
    void testKakaoUserInfo() {
        Map<String, Object> profile = Map.of(
                "nickname", "BaekSoo",
                "profile_image_url", "http://example.com/BaekSoo.jpg"
        );
        Map<String, Object> kakaoAccount = Map.of("profile", profile);
        Map<String, Object> attributes = Map.of(
                "id", "1234567890",
                "properties", profile,
                "kakao_account", kakaoAccount
        );

        OAuth2UserInfo userInfo = OAuth2UserService.getUserInfo("kakao", attributes);

        assertEquals("1234567890", userInfo.getPersonalId());
        assertEquals("BaekSoo", userInfo.getNickname());
        assertEquals("http://example.com/BaekSoo.jpg", userInfo.getProfileImageUrl());
    }

    @DisplayName("Google 사용자 정보 생성 테스트")
    @Test
    void testGetGoogleUserInfo() {
        Map<String, Object> attributes = Map.of(
                "sub", "1234567890",
                "name", "BaekSoo",
                "picture", "http://example.com/BaekSoo.jpg"
        );
        OAuth2UserInfo userInfo = OAuth2UserService.getUserInfo("google", attributes);

        assertEquals("1234567890", userInfo.getPersonalId());
        assertEquals("BaekSoo", userInfo.getNickname());
        assertEquals("http://example.com/BaekSoo.jpg", userInfo.getProfileImageUrl());
    }

    @DisplayName("Github 사용자 정보 생성 테스트")
    @Test
    void testGetGithubUserInfo() {
        Map<String, Object> attributes = Map.of(
                "id", "123456789",
                "login", "BaekSoo",
                "avatar_url", "http://example.com/BaekSoo.jpg"
        );
        OAuth2UserInfo userInfo = OAuth2UserService.getUserInfo("github", attributes);

        assertEquals("123456789", userInfo.getPersonalId());
        assertEquals("BaekSoo", userInfo.getNickname());
        assertEquals("http://example.com/BaekSoo.jpg", userInfo.getProfileImageUrl());
    }

    @DisplayName("Naver 사용자 정보 생성 테스트")
    @Test
    void testNaverUserInfo() {
        Map<String, Object> responseAttributes = Map.of(
                "id", "1234567890",
                "name", "홍길동",
                "profile_image", "http://example.com/image.jpg"
        );
        Map<String, Object> attributes = Map.of("response", responseAttributes);
        OAuth2UserInfo userInfo = OAuth2UserService.getUserInfo("naver", attributes);

        assertEquals("1234567890", userInfo.getPersonalId());
        assertEquals("홍길동", userInfo.getNickname());
        assertEquals("http://example.com/image.jpg", userInfo.getProfileImageUrl());
    }


    @DisplayName("userInfo 를 받은 후 새로운 회원이면 저장한다")
    @Test
    void saveNewUser() {
        // given
        Map<String, Object> responseAttributes = Map.of(
                "id", "1234567890",
                "name", "백수",
                "profile_image", "http://example.com/백수.jpg"
        );
        Map<String, Object> attributes = Map.of("response", responseAttributes);
        OAuth2UserInfo userInfo = OAuth2UserService.getUserInfo("naver", attributes);

        Member existingMember = Member.builder()
                .id(1L)
                .personalId("1234567890")
                .loginProvider(LoginProvider.NAVER)
                .build();
        //when
        when(memberRepository.findByPersonalId("1234567890"))
                .thenReturn(Optional.of(existingMember));

        Member result = oAuth2UserService.findOrSaveMember(userInfo);

        // then
        verify(memberRepository, times(0)).save(any(Member.class));
        assertEquals(existingMember.getPersonalId(), result.getPersonalId());
    }

    @Test
    @DisplayName("userInfo가 주어질 때 존재하지 않는 회원은 저장해야 한다")
    void saveNewUserWhenNotExists() {
        // given
        Map<String, Object> responseAttributes = Map.of(
                "id", "1234567891",
                "name", "백수",
                "profile_image", "http://example.com/백수.jpg"
        );
        Map<String, Object> attributes = Map.of("response", responseAttributes);
        OAuth2UserInfo userInfo = OAuth2UserService.getUserInfo("naver", attributes);

        when(memberRepository.findByPersonalId("1234567891"))
                .thenReturn(Optional.empty());

        // then
        oAuth2UserService.findOrSaveMember(userInfo);

        // verify
        verify(memberRepository, times(1)).save(any(Member.class));
    }




}