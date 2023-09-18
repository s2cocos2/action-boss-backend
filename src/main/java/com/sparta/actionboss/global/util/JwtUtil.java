package com.sparta.actionboss.global.util;


import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_ACCESS = "Access";
    public static final String AUTHORIZATION_REFRESH = "Refresh";
    public static final String AUTHORIZATION_KEY = "auth";
    public static final String BEARER_PREFIX = "Bearer ";


    @Value("${jwt.secret.key.access}")
    private String accessTokenSecretKey;
    @Value("${jwt.secret.key.refresh}")
    private String refreshTokenSecretKey;
    private Key accessTokenKey;
    private Key refreshTokenKey;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] accessTokenBytes = Base64.getDecoder().decode(accessTokenSecretKey);
        accessTokenKey = Keys.hmacShaKeyFor(accessTokenBytes);

        byte[] refreshTokenBytes = Base64.getDecoder().decode(refreshTokenSecretKey);
        refreshTokenKey = Keys.hmacShaKeyFor(refreshTokenBytes);
    }

    // 토큰 생성
    public String createAccessToken(String nickname, UserRoleEnum role) {
        Date date = new Date();

        long TOKEN_TIME = 60 * 60 * 1000L; // 60분

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(nickname)
                        .claim(AUTHORIZATION_KEY, role)
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        .setIssuedAt(date)
                        .signWith(accessTokenKey, signatureAlgorithm)
                        .compact();
    }

    public String createRefreshToken(String nickname) {
        Date date = new Date();

        long TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L;   //일주일

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(nickname)
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        .setIssuedAt(date)
                        .signWith(refreshTokenKey, signatureAlgorithm)
                        .compact();
    }

    public String getJwtFromHeader(HttpServletRequest request, String tokenType) {
        if(tokenType.equals("Access")){
            String bearerToken = request.getHeader(AUTHORIZATION_ACCESS);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(7);
            }
        }else {
            String bearerToken = request.getHeader(AUTHORIZATION_REFRESH);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(7);
            }
        }
        return null;
    }

    // 토큰 검증
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(accessTokenKey).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid Access JWT signature, 유효하지 않는 JWT 서명 입니다.");
            throw new IllegalArgumentException("유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT Access token, 만료된 JWT token 입니다.");
            throw new IllegalArgumentException("만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported Access JWT token, 지원되지 않는 JWT 토큰 입니다.");
            throw new IllegalArgumentException("지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("Access JWT claims is empty, 잘못된 JWT 토큰 입니다.");
            throw new IllegalArgumentException("잘못된 JWT 토큰 입니다.");
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(refreshTokenKey).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid Refresh JWT signature, 유효하지 않는 JWT 서명 입니다.");
            throw new IllegalArgumentException("유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired Refresh JWT token, 만료된 JWT token 입니다.");
            throw new IllegalArgumentException("만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported Refresh JWT token, 지원되지 않는 JWT 토큰 입니다.");
            throw new IllegalArgumentException("지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("Refresh JWT claims is empty, 잘못된 JWT 토큰 입니다.");
            throw new IllegalArgumentException("잘못된 JWT 토큰 입니다.");
        }
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromAccessToken(String token) {
        return Jwts.parserBuilder().setSigningKey(accessTokenKey).build().parseClaimsJws(token).getBody();
    }

    public String getUserInfoFromRefreshToken(String token){
        return Jwts.parserBuilder().setSigningKey(refreshTokenKey).build().parseClaimsJws(token).getBody().getSubject();
    }
}
