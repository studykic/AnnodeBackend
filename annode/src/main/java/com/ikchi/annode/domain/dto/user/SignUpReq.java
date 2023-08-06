package com.ikchi.annode.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;


// 회원가입 요청에 이용할 DTO
@Getter
@Setter
@ToString
public class SignUpReq {


    @Email(message = "올바른 이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "올바른 PhoneNumber를 입력해주세요.")
    private String phoneNumber;

    // 비밀번호는 8자리 이상으로 영문, 숫자, 특수문자를 포함해야한다.
    @NotBlank(message = "올바른 비밀번호 8자리를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자리 이상으로 입력해주세요.")
    private String password;


    @NotBlank(message = "올바른 닉네임 2자리 이상 10자리 이하로 입력해주세요.")
    private String nickName;

    @NotBlank(message = "메일 인증 코드를 입력해주세요.")
    private String mailAuthCode;

    @NotBlank(message = "Phone 인증 코드를 입력해주세요.")
    private String phoneAuthCode;

    private boolean accountPublic;

    private boolean termsAndConditionsCheck;
    
    private MultipartFile profileImageFile; // MultipartFile 추가
}