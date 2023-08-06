package com.ikchi.annode.domain.dto.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class PwResetInfoReq {

    // 비밀번호 재설정을 위한 Map의 key 정보로 메일이 사용됨
    @Email(message = "올바른 이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "올바른 비밀번호 8자리를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자리 이상으로 입력해주세요.")
    private String resetPassword;


    @NotBlank(message = "올바른 메일인증번호를 입력해주세요.")
    private String mailAuthCode;

}
