package com.ikchi.annode.Enum;

public enum SocketHandlerMessage {


    ROOM_TOKEN_EXISTS("이미 존재하는 방토큰 입니다"),
    MESSAGE_MODIFICATION_FAILED("메세지를 수정하는데 실패했습니다"),
    SIGNALING_ERROR("시그널링 서버에서 메시지를 전송하는 도중에 오류가 발생했습니다."),
    USER_CREATION_FAILED("유저 정보를 생성하는데 실패했습니다");


    private final String message;

    SocketHandlerMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
