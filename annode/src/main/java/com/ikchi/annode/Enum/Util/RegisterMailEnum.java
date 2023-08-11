package com.ikchi.annode.Enum.Util;

public enum RegisterMailEnum {

    FAILED_SEND_EMAIL("인증 메일 전송에 실패했습니다");

    private final String message;

    RegisterMailEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
