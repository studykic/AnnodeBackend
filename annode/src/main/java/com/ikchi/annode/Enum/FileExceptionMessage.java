package com.ikchi.annode.Enum;

public enum FileExceptionMessage {

    IMAGE_UPLOAD_ERROR("이미지 업로드중 문제가 발생하였습니다"),

    UNSUPPORTED_FILE_FORMAT("지원하지 않는 파일 형식입니다"),

    IMAGE_DELETE_ERROR("이미지 삭제 중 문제가 발생하였습니다");;

    private final String message;

    FileExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
