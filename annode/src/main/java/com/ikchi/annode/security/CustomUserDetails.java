package com.ikchi.annode.security;

import com.ikchi.annode.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

// 로그인시 유저 정보를 담는 클래스이며 UserDetails 인터페이스를 구현
// 검증을 수행하기 위한 클래스이지 검증 클래스는 아니다
public class CustomUserDetails implements UserDetails {

    // user를 생성자를 통해 주입함
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public final User getUser() {
        return user;
    }


    // Spring Security에서 권한 정보를 설정하기 위한 메소드
    // 사용자가 가지고 있는 권한 정보(GrantedAuthority)를 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream().map(o -> new SimpleGrantedAuthority(
            o.getName()
        )).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
