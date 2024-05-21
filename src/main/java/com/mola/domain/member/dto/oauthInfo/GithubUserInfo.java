package com.mola.domain.member.dto.oauthInfo;


import com.mola.domain.member.entity.LoginProvider;
import java.util.Map;

public class GithubUserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    public GithubUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getPersonalId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("login");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("avatar_url");
    }

    @Override
    public LoginProvider getLoginProvider() {
        return LoginProvider.GITHUB;
    }
}
