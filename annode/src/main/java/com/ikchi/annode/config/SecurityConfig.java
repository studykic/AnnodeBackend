package com.ikchi.annode.config;

import com.ikchi.annode.security.JwtAuthenticationFilter;
import com.ikchi.annode.security.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
// 시큐리티 활성화 -> 기본 스프링 필터체인에 등록
public class SecurityConfig {

    // jwt 토큰을 관리하는 클래스를 DI
    private final JwtProvider jwtProvider;

    // 스프링 시큐리티에서 cors 설정시 corsConfigurationSource() 메소드를 선언하는것 외에도 securityFilterChain에서 cors() 메소드를 호출해야함.

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(
            List.of("https://loadbalancer.annode-kic.com",
                "http://loadbalancer.annode-kic.com",
                "https://annode-kic.com",
                "http://annode-kic.com"
            ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    // HTTP 요청이 들어올 때마다 등록된 필터들을 순서대로 실행하여 요청을 처리
    // Spring Security에서 필터 체인을 설정하고 관리할수 있도록 Bean 등록
    // .and()는 Spring Security에서 메소드 체이닝을 사용할 때, 이전에 호출된 메소드와 새로운 메소드를 연결하여 하나의 메소드 체인으로 만들어주는 역할임
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .cors()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/signup", "/login", "/mailConfirm", "/phoneConfirm",
                    "/passwordReset", "/file/**",
                    "/view/**",
                    "/ws/**").permitAll()
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")
                .requestMatchers("/user/**", "/room/**", "/category/**", "/pospace/**")
                .hasRole("USER")
                .anyRequest().denyAll())
            .addFilterBefore(new JwtAuthenticationFilter(jwtProvider),
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .accessDeniedHandler(new AccessDeniedHandler() {
                @Override
                public void handle(HttpServletRequest request, HttpServletResponse response,
                    AccessDeniedException accessDeniedException)
                    throws IOException, ServletException {
                    response.setStatus(403);
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("text/html; charset=UTF-8");
                    response.getWriter().write("권한이 없는 사용자입니다.");
                }
            })
            .authenticationEntryPoint(new AuthenticationEntryPoint() {
                @Override
                public void commence(HttpServletRequest request, HttpServletResponse response,
                    AuthenticationException authException) throws IOException, ServletException {
                    response.setStatus(401);
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("text/html; charset=UTF-8");
                    response.getWriter().write("로그인하지 않은 사용자입니다. 로그인후 다시 시도해주세요. ");
                }
            })
            .and()
            .sessionManagement()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true)
            .expiredUrl("/");

        return http.build();
    }

    // 암호화를 할때 사용되며 일반적으로 BCryptPasswordEncoder 암호 알고리즘을 사용한다
    // 주요 메소드는 문자열을 해시 함수를 사용하여 암호화하는 encode()
    // 저장된 암호화 입력된 암호를 비교하는 matches() 메소드가 있다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}






