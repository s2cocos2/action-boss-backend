package com.sparta.actionboss.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.actionboss.domain.user.dto.response.KakaoUserInfoDto;
import com.sparta.actionboss.domain.user.entity.RefreshToken;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import com.sparta.actionboss.domain.user.repository.RefreshTokenRepository;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static com.sparta.actionboss.global.response.SuccessMessage.LOGIN_SUCCESS;

@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    @Value("${kakao.client.id}")
    private String kakaoClientId;


    @Transactional
    public CommonResponse kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // 3. 필요시에 회원가입
        User kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);

        // 4. JWT 토큰 반환
        String createAccessToken = jwtUtil.createAccessToken(kakaoUser.getUserId(), kakaoUser.getRole());
        String createRefreshToken = jwtUtil.createRefreshToken(kakaoUser.getUserId());

        refreshTokenRepository.deleteByUserId(kakaoUser.getUserId());
        RefreshToken refreshTokenEntity = new RefreshToken(createRefreshToken.substring(7), kakaoUser.getUserId());
        refreshTokenRepository.save(refreshTokenEntity);

        response.addHeader(JwtUtil.AUTHORIZATION_ACCESS, createAccessToken);
        response.addHeader(JwtUtil.AUTHORIZATION_REFRESH, createRefreshToken);

        return new CommonResponse(LOGIN_SUCCESS);
    }


    private String getToken(String code) throws JsonProcessingException {
        log.info("인가코드 : " + code);
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
//        body.add("redirect_uri", "http://localhost:8080/api/auth/kakao");
        body.add("redirect_uri", "https://hdaejang.com/oauth/callback");
        body.add("code", code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }


    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        log.info("accessToken : " + accessToken);
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();

        JsonNode kakaoAccountNode = jsonNode.get("kakao_account");
        String email = null;
        if (kakaoAccountNode != null && kakaoAccountNode.has("email")) {
            email = kakaoAccountNode.get("email").asText();
            log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
            return new KakaoUserInfoDto(id, nickname, email);
        } else {
            log.info("카카오 사용자 정보: " + id + ", " + nickname);
            return new KakaoUserInfoDto(id, nickname);
        }
    }


    private User registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
        Long kakaoId = kakaoUserInfo.getId();
        User kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null);

        if (kakaoUser == null) {
            String nickname = kakaoUserInfo.getNickname() + "_KAKAO" + kakaoId;
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);
            String kakaoEmail = kakaoUserInfo.getEmail();

            if(kakaoEmail != null){
                User sameEmailUser = userRepository.findByEmail(kakaoEmail).orElse(null);
                if (sameEmailUser != null) {
                    kakaoUser = sameEmailUser;
                    kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
                } else {
                    String email = kakaoUserInfo.getEmail();
                    kakaoUser = new User(nickname, encodedPassword, email, UserRoleEnum.USER, kakaoId);
                }
                userRepository.save(kakaoUser);
            } else {
                kakaoUser = new User(nickname, encodedPassword, UserRoleEnum.USER, kakaoId);
                userRepository.save(kakaoUser);
            }
        }
        return kakaoUser;
    }
}