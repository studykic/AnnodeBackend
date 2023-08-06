package com.ikchi.annode.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 요청헤더 Authorization에 있는 jwt를 받아서 토큰을 파싱하고 토큰에 있는 이메일을 반환한다
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JwtToUser {

}

