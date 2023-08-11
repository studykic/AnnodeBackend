package com.ikchi.annode.security;

import com.ikchi.annode.Enum.Util.JwtErrorEnum;
import com.ikchi.annode.domain.vo.Authority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// 토큰을 생성하는 클래스이며 DI를 위해 @Component 어노테이션을 붙여준다.
@RequiredArgsConstructor
@Component
public class JwtProvider {

    // application.properties 파일의 jwt.secret.key 속성 값을 가져온다.
    @Value("${jwt.secret.key}")
    private String salt;

    private Key secretKey;

    private static final Logger logger = LogManager.getLogger(JwtProvider.class);

    // JWT 접두사
    public static final String BEARER_PREFIX = "BEARER";

    // 토큰 만료시간 설정
    private static final int defaultExpTime = 1209600; // 초 단위 - 2주일 기본설정

    private long exp = getExpirationTimeInSeconds(defaultExpTime);

    // ms 단위로 변환
    private long getExpirationTimeInSeconds(long seconds) {
        return seconds * 1000L;
    }


    private final JpaUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성
    public String createToken(String account, List<Authority> roles) {
        Claims claims = Jwts.claims().setSubject(account);
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + exp))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    // 토큰을 파라미터로 받아서 토큰에 포함된 계정 정보를 이용해 UserDetails 객체를 생성
    // userDetailsService은 로그인 요청이 오면 자동으로 loadUserByUsername함수를 실행시켜 유저의 가입유무를 확인한다
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getAccount(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }

    // 토큰으로부터 사용자 계정 정보를 추출
    public String getAccount(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody()
            .getSubject();
    }

    //  Authorization 헤더에서 JWT 토큰을 추출
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    // Http 요청으로 받아온 토큰 검증 ( Signature (서명) 부분에서 토큰의 정보들을 확인한다 )
    public boolean validateToken(String token) {
        try {
            if (!token.substring(0, (BEARER_PREFIX + " ").length())
                .equalsIgnoreCase(BEARER_PREFIX + " ")) {
                logger.error(JwtErrorEnum.NOT_BEARER.getMessage());
                return false;
            } else {
                token = token.split(" ")[1].trim();
            }

            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            logger.error(JwtErrorEnum.UNKNOWN_ERROR.getMessage(), e);
            return false;
        }
    }

    // Http 요청으로 받아온 토큰 검증
    // 토큰 만료시간 유효성 검사를 한뒤 email을 반환
    public String getEmailFromToken(String token) {
        try {
            token = java.net.URLDecoder.decode(token, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.error(JwtErrorEnum.UNSUPPORTED_ENCODING.getMessage(), e);
            throw new RuntimeException(e);
        }

        if (!token.substring(0, (BEARER_PREFIX + " ").length())
            .equalsIgnoreCase(BEARER_PREFIX + " ")) {
            logger.error(JwtErrorEnum.NOT_BEARER.getMessage());
            throw new IllegalStateException(JwtErrorEnum.NOT_BEARER.getMessage());
        } else {
            token = token.split(" ")[1].trim();
        }

        Jws<Claims> claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token);
        String userMail = claims.getBody().getSubject();
        boolean expTime = !claims.getBody().getExpiration().before(new Date());

        if (expTime) {
            return userMail;
        } else {
            throw new IllegalStateException(JwtErrorEnum.JWT_EXPIRED.getMessage());
        }
    }


}
