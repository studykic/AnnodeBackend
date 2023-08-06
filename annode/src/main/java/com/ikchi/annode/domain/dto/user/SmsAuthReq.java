package com.ikchi.annode.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SmsAuthReq {

    @NotBlank
    private String eventName;

    @NotBlank(message = "올바른 이메일을 입력해주세요")
    private String phoneNumber;

}
