package com.ikchi.annode.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// 회원가입 요청에 이용할 DTO
@Getter
@Setter
@ToString
public class LonginReq {

    @NotBlank(message = "올바른 이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "올바른 비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자리 이상으로 입력해주세요.")
    private String password;

    private String fcmToken;

}
