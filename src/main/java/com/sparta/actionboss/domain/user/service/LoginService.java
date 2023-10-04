package com.sparta.actionboss.domain.user.service;

import com.sparta.actionboss.domain.user.dto.request.LoginRequestDto;
import com.sparta.actionboss.domain.user.entity.RefreshToken;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.repository.RefreshTokenRepository;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.sparta.actionboss.global.exception.errorcode.ClientErrorCode.*;
import static com.sparta.actionboss.global.response.SuccessMessage.CREATE_REFRESHTOKEN;
import static com.sparta.actionboss.global.response.SuccessMessage.LOGIN_SUCCESS;
import static com.sparta.actionboss.global.util.JwtUtil.AUTHORIZATION_REFRESH;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Transactional
    public CommonResponse login(LoginRequestDto requestDto, HttpServletResponse response){

        User user = userRepository.findByEmail(requestDto.email()).orElseThrow(() ->
                new CommonException(NO_ACCOUNT));
        if(!passwordEncoder.matches(requestDto.password(), user.getPassword())){
            throw new CommonException(INVALID_PASSWORDS);
        }
        String accessToken = jwtUtil.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getUserId());

        refreshTokenRepository.deleteByUserId(user.getUserId());
        RefreshToken refreshTokenEntity = new RefreshToken(refreshToken.substring(7), user.getUserId());
        refreshTokenRepository.save(refreshTokenEntity);

        response.addHeader(JwtUtil.AUTHORIZATION_ACCESS, accessToken);
        response.addHeader(JwtUtil.AUTHORIZATION_REFRESH, refreshToken);

        return new CommonResponse(LOGIN_SUCCESS);
    }


    @Transactional(readOnly = true)
    public CommonResponse reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = jwtUtil.getJwtFromHeader(request, AUTHORIZATION_REFRESH);

        if (StringUtils.hasText(refreshToken)) {
            if (jwtUtil.validateRefreshToken(refreshToken)) {
                refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                        ()-> new CommonException(NO_REFRESHTOKEN));

                Long userId = jwtUtil.getUserInfoFromRefreshToken(refreshToken);


                String newAccessToken = jwtUtil.createAccessToken(userId, UserRoleEnum.USER);

                response.addHeader(JwtUtil.AUTHORIZATION_ACCESS, newAccessToken);
                return new CommonResponse(CREATE_REFRESHTOKEN);
            }
        }
        throw new CommonException(INVALID_REFRESHTOKEN);
    }
}
