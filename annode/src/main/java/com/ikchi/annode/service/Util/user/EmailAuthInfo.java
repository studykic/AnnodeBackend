package com.ikchi.annode.service.Util.user;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

// 메일의 인증번호와 만료시간이 담긴 클래스
@Getter
@ToString
public class EmailAuthInfo {


    private String authCode;
    private LocalDateTime expirationTime;

    public EmailAuthInfo(String authCode, int CODE_EXPIRATION_MINUTES) {
        this.authCode = authCode;
        this.expirationTime = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);
    }

    // 인증번호 유효시간이 지났는지 확인하여 만료를 알림
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(expirationTime);
    }


}
