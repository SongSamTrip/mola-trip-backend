package com.mola.global.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.mola.domain.member.dto.LoginResponseDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class JwtProvider {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    @Value("${JWT_ACCESS_TOKEN_TIME}")
    private Long accessTokenExpireTime;

    @Value("${JWT_REFRESH_TOKEN_TIME}")
    private Long refreshTokenExpireTime;

    private static final String ISSUER = "MolaTrip";

    private final MemberRepository memberRepository;

    public String createAccessToken(Long memberId, String profileImageUrl, String nickName) {
        Date now = new Date();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject("AccessToken")
                .withExpiresAt(new Date(now.getTime() + accessTokenExpireTime))
                .withClaim("memberId", memberId)
                .withClaim("profileImageUrl", profileImageUrl)
                .withClaim("nickName", nickName)
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withIssuer(ISSUER)
                .withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpireTime))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC512(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public LoginResponseDto createTokens(Long memberId, String profileImageUrl, String nickName) {
        String accessToken = createAccessToken(memberId, profileImageUrl, nickName);
        String refreshToken = createRefreshToken();
        updateRefreshToken(memberId, refreshToken);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void updateRefreshToken(Long memberId, String refreshToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("No member found"));
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);
    }

    public Long extractMemberIdFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("memberId").asLong();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public Authentication extractAuthenticationFromStompHeaderAccessor(StompHeaderAccessor accessor){
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            if (verifyToken(token)) {
                Long memberId = extractMemberIdFromToken(token);
                UserDetails user = createUserDetails(memberId, "ROLE_USER");

                return new UsernamePasswordAuthenticationToken(
                        user, "", user.getAuthorities());
            }
        }

        return null;
    }

    public UserDetails createUserDetails(Long memberId, String role) {
        return User.builder()
                .username(memberId.toString())
                .password("")
                .authorities(role)
                .build();
    }
}