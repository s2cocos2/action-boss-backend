package com.sparta.actionboss.global.security;

import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.repository.UserRepository;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.sparta.actionboss.global.exception.errorcode.ClientErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserByUserId(Long userId) throws CommonException {
        User user = userRepository.findByUserId(userId).orElseThrow(
                () -> new CommonException(NO_ACCOUNT));

        return new UserDetailsImpl(user);
    }
}
