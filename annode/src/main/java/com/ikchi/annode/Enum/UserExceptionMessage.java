package com.ikchi.annode.Enum;


public enum UserExceptionMessage {

    ALREADY_REGISTERED_EMAIL("이미 가입되어있는 email입니다"),
    ALREADY_REGISTERED_PHONE("이미 가입되어있는 PhoneNumber입니다"),
    UNREGISTERED_EMAIL("가입되어있지 않은 email입니다"),
    FAILED_EMAIL_VERIFICATION("인증 메일 전송에 실패했습니다"),
    UNAUTHORIZED_REGISTRATION("회원가입 불가한 유저입니다"),
    EMAIL_CODE_MISMATCH("Email 코드가 일치하지 않습니다"),
    PHONE_CODE_MISMATCH("PhoneNumber 코드가 일치하지 않습니다"),
    NICKNAME_LENGTH_CONSTRAINT("닉네임은 2자리 이상 10자리 이하로 입력해주세요"),
    PASSWORD_MISMATCH("비밀번호를 다시 확인해주세요"),
    INVALID_VERIFICATION_CODE("인증번호가 유효하지않습니다"),
    EXIT_CHAT_BEFORE_DELETION("참여중인 대화방에서 나간뒤 계정탈퇴를 시도하세요"),
    NOT_ADMIN_ACCOUNT("운영자 계정이 아니기에 요청할수없습니다"),
    NON_EXISTENT_USER("존재하지 않는 유저입니다"),
    NON_EXISTENT_FOLLOW_REQUEST("존재하지 않는 Follow 요청입니다"),
    SELF_FOLLOW_ATTEMPT("자신을 팔로워할수 없습니다"),
    DUPLICATE_FOLLOW_REQUEST("이미 같은 어노드에게 Follow 요청을 보냈습니다"),
    ALREADY_FOLLOWING("이미 Follow 관계입니다"),
    NOT_FOLLOWING_USER("Follow 관계가 아닙니다"),
    PASSWORD_RECHECK("비밀번호를 다시 확인해주세요"),

    INVALID_NICKNAME_LENGTH("올바른 닉네임 2자리 이상 20자리 이하로 입력해주세요");


    private final String message;

    UserExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}