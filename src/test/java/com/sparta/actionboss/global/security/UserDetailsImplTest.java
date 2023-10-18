package com.sparta.actionboss.global.security;

import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDetailsImplTest {

    User user;
    UserDetailsImpl userDetailsIml;

    @BeforeEach
    void setUp(){
        user = new User("코코","abcd1234","coco@naver.com", UserRoleEnum.USER);
        userDetailsIml = new UserDetailsImpl(user);
    }

    @Test
    @DisplayName("비밀번호")
    void getPassword(){
        assertEquals("abcd1234",userDetailsIml.getPassword());
    }

    @Test
    @DisplayName("계정")
    void getEmail(){
        assertEquals("coco@naver.com",userDetailsIml.getUsername());
    }

    @Test
    @DisplayName("권한")
    void getAuthorities(){
        Collection<? extends GrantedAuthority> authorities = userDetailsIml.getAuthorities();

        GrantedAuthority grantedAuthority = authorities.iterator().next();
        assertEquals(UserRoleEnum.USER.getAuthority(),grantedAuthority.getAuthority());
    }

    @Test
    @DisplayName("계정 만료 여부")
    void isAccountNonExpired(){
        assertTrue(userDetailsIml.isAccountNonExpired());
    }

    @Test
    @DisplayName("계정 잠금 여부")
    void isAccountNonLocked(){
        assertTrue(userDetailsIml.isAccountNonLocked());
    }

    @Test
    @DisplayName("자격 만료 여부")
    void isCredentialsNonExpired(){
        assertTrue(userDetailsIml.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("활성화 여부")
    void isEnabled(){
        assertTrue(userDetailsIml.isEnabled());
    }
}