package com.ikchi.annode.Enum.Util;

public enum JwtErrorEnum {


    NOT_BEARER("JWT 접두사가 BEARER이 아닙니다"),
    JWT_EXPIRED("로그인 정보가 만료되었습니다"),
    UNSUPPORTED_ENCODING("JWT에 대한 지원되지 않는 인코딩"),
    UNKNOWN_ERROR("JWT와 관련된 알 수 없는 오류");

    private final String message;

    JwtErrorEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}