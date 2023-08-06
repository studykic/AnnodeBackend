package com.ikchi.annode.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;


// JWT 로그인시 검증을 거치는 필터 클래스
// OncePerRequestFilter를 상속받았기 때문에, 요청당 한 번씩만 실행
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    // spring Security 필터 체인에서 인증 정보 검증 단계에서 호출되며, 요청에서 추출한 JWT를 검증하여 사용자가 인증되었는지 확인
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 JWT 토큰을 추출
        String token = jwtProvider.resolveToken(request);

        // JWT의 서명 유효성 검증을 통과하면 Authentication 객체를 생성하고
        // setAuthentication 메소드를 통해 SecurityContext에 저장하여 전역적으로 인증된 사용자 설정함
        if (token != null && jwtProvider.validateToken(token)) {
            // check access token
            token = token.split(" ")[1].trim();

            // Authentication는 Spring Security에서 인증 정보를 나타내는 객체
            Authentication auth = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}