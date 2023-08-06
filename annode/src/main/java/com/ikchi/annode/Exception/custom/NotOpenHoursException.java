package com.ikchi.annode.Exception.custom;

public class NotOpenHoursException extends RuntimeException {

    public NotOpenHoursException() {
        super("대화방 오픈시간이 아닙니다! 대기해주신뒤 18:00~21:00 사이 다시 방문해주세요.");
    }
}
