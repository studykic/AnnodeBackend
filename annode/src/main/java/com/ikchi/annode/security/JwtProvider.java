package com.ikchi.annode.security;

import com.ikchi.annode.domain.vo.Authority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    // 토큰 만료시간 설정

    
    private final int defaultExpTime = 1209600; // 초 단위 - 2주일 기본설정

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
            // HTTP를 이용한 Bearer 토큰 인증 방식인지 확인하고 맞으면 공백제거후 토큰만 추출
            if (!token.substring(0, "BEARER ".length()).equalsIgnoreCase("BEARER ")) {
                return false;
            } else {
                token = token.split(" ")[1].trim();
            }

            // 클레임은 JWT에서 사용되는 정보를 나타내는 객체이며
            // 이떄 setSigningKey로 서명키를 지정하고 parseClaimsJws로 유저의 토큰을 받아 클레임으로 파싱한다
            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);

            // claims에서 토큰 만료기간을 가져와 만료되었을 시 false 반환
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Http 요청으로 받아온 토큰 검증 ( Signature (서명) 부분에서 토큰의 정보들을 확인한다 )
    // 토큰 만료시간 유효성 검사를 한뒤 email을 반환
    public String getEmailFromToken(String token) {

        try {

//            URL 인코딩된 문자열을 디코딩함 - http를 ws방식으로 업그레이드 하는 요청에서 jwt가 url로 들어갈때 사용됨
            token = java.net.URLDecoder.decode(token, StandardCharsets.UTF_8.name());

            // HTTP를 이용한 Bearer 토큰 인증 방식인지 확인하고 맞으면 공백제거후 토큰만 추출
            if (!token.substring(0, "BEARER ".length()).equalsIgnoreCase("BEARER ")) {
                throw new IllegalStateException("BEARER 아님");
            } else {
                token = token.split(" ")[1].trim();
            }

            // 클레임은 JWT에서 사용되는 정보를 나타내는 객체이며
            // 이떄 setSigningKey로 서명키를 지정하고 parseClaimsJws로 유저의 토큰을 받아 클레임으로 파싱한다
            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);

            // mail 정보 가져오기
            String userMail = claims.getBody().getSubject();

            // claims에서 토큰 만료기간을 가져와 만료되었을 시 false 반환
            boolean expTime = !claims.getBody().getExpiration().before(new Date());
            if (expTime) {
                return userMail;
            } else {
                throw new IllegalStateException("jwt 만료됨");
            }

        } catch (Exception e) {
            System.out.println("jwt 에러 : " + e.getMessage());
            throw new RuntimeException("jwt 에러 : " + e.getMessage());
        }
    }


}
