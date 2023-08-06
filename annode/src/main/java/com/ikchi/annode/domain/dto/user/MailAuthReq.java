package com.ikchi.annode.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MailAuthReq {

    @NotBlank
    private String eventName;

    @NotBlank(message = "올바른 이메일을 입력해주세요")
    @Email(message = "올바른 이메일을 입력해주세요")
    private String email;
}
