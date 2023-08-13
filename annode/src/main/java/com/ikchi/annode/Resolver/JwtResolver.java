package com.ikchi.annode.Resolver;

import com.ikchi.annode.annotation.JwtToUserMail;
import com.ikchi.annode.security.JwtProvider;
import com.ikchi.annode.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

//  HTTP 요청을 디스패처 서블릿에서 받으면 핸들러 매핑을 통해 핸들러를 찾게되고
//  핸들러 어댑터를 이어서 찾은뒤 핸들러 어댑터는 요청에 해당하는 컨트롤러 메소드를찾는다
//  이때 핸들러 메소드의 인자를 변환할수있는지 시도하기 위해서 HandlerMethodArgumentResolver 를 확인한다


//  중요!! HandlerMethodArgumentResolver가 맡은 역할은 요청으로 받은 인자에서 변환을 하는 목적이니 최소한의 변환 로직만 작성하자
@Component
@RequiredArgsConstructor
public class JwtResolver implements HandlerMethodArgumentResolver {


    private final JwtProvider jwtProvider;

    private final UserService userService;

    // supportsParameter에서 인자를 변환이 가능하다고 true를 반환한다면
    // resolveArgument() 가 실행되고 의도한 값으로 변환처리후 반환값을 핸들러 메소드의 인자로 자동 전달된다

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(JwtToUserMail.class) != null
            && parameter.getParameterType().equals(String.class);

    }


    // 요청헤더 Authorization에 있는 jwt를 받아서 토큰을 파싱하고 토큰에 있는 이메일을 반환한다
    @Override
    public String resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        String jwt = webRequest.getHeader("Authorization");

        if (jwt == null || jwt.trim().isEmpty()) {
            throw new IllegalArgumentException("요청 헤더에 JWT 토큰이 없습니다.");
        }

        return jwtProvider.getEmailFromToken(jwt);
    }


}
