package com.ikchi.annode.security;

import com.ikchi.annode.domain.entity.User;
import com.ikchi.annode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// 로그인 요청이 요면 자동으로 UserDetailsService 타입으로 IOC되어있는 빈( JpaUserDetailsService )의 loadUserByUsername함수 실행
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    // 로그인 요청이 요면 자동으로 UserDetailsService 타입으로 IOC되어있는 빈( JpaUserDetailsService )의 loadUserByUsername함수 실행
    // Jwt를 가지고 DB에 가입되어있는 회원을 검증하는게 아니라 로그인 정보를 가지고 DB에 있는 회원정보를 조회한다
    // 아래 username은 실제값이 들어갈때엔 이메일이 들어가게된다
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findUserByMail(username).orElseThrow(
            () -> new UsernameNotFoundException("이메일 정보로 가입된 유저가없습니다!")
        );

        return new CustomUserDetails(user);
    }
}