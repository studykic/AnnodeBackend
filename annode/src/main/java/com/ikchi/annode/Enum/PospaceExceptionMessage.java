package com.ikchi.annode.Enum;

public enum ExceptionMessage {

    NO_SUCH_POST("존재하지 않는 Pospace입니다"),
    POST_OR_IMAGE_REQUIRED("글과 사진중 하나는 존재해야합니다"),
    INVALID_POSPACE_TYPE("Pospace Type이 올바르지 않습니다"),
    LOGIN_REQUIRED_FOR_POST("로그인이 필요한 게시물입니다. 어플에서 접근해주세요"),
    NO_FOLLOW_RELATIONSHIP("follow 관계가 아니기에 접근할수없습니다"),
    NO_CROSSFOLLOW_RELATIONSHIP("Cross Follow 관계가 아니기에 접근할수없습니다"),
    ALREADY_IN_ROOM("이미 접속중인 방이있습니다"),
    NO_CROSSFOLLOW_USERS("cross Follow중인 유저가 없습니다"),
    ALREADY_LIKED_POST("이미 좋아요한 게시물입니다"),
    LIKE_FUNCTION_ISSUE("좋아요기능이 원활하지않습니다 나중에 다시 시도해주세요"),
    NO_SUCH_USER("존재하지 않는 유저입니다"),
    NOT_COMMENT_WRITER("댓글의 작성자가 아닙니다"),
    NO_EDIT_PERMISSION("수정할수있는 권한이 없습니다");

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
