package com.ikchi.annode.Resolver;

import com.ikchi.annode.annotation.JwtToUser;
import com.ikchi.annode.security.JwtProvider;
import com.ikchi.annode.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

//  HTTP 요청을 디스패처 서블릿에서 받으면 서블릿은 요청에 해당하는 컨트롤러 메소드를찾는다
//  이후 핸들러 메소드의 인자를 변환할수있는지 시도하기 위해서 HandlerMethodArgumentResolver 를 확인한다
//  만약  supportsParameter 에서 인자를 변환이 가능하다고 true를 반환한다면
//  resolveArgument() 가 실행되고 의도한 값으로 변환처리후 반환값을 핸들러 메소드의 인자로 자동 전달된다
//  중요!! HandlerMethodArgumentResolver 는 비즈니스 로직을 담당하는것이 아니라 인자변환의 역할을 담당하니 최소한의 변환 로직만 작성하자
// 참고!! HandlerMethodArgumentResolver를 사용했기에 컨트롤러에서만 사용되며 일반메소드에서 커스텀어노테이션 사용을 하려면 Aop를 사용해야한다
@Component
public class JwtResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserService userService;


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(JwtToUser.class) != null
            && parameter.getParameterType().equals(String.class);

    }


    // 요청헤더 Authorization에 있는 jwt를 받아서 토큰을 파싱하고 토큰에 있는 이메일을 반환한다
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        String jwt = webRequest.getHeader("Authorization");

        if (jwt != null) {
            String userMail = jwtProvider.getEmailFromToken(jwt);
            return userMail;
        }

        return null;

    }


}
