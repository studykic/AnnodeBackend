package com.ikchi.annode.domain.dto.user;

import com.ikchi.annode.domain.vo.Authority;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoRes {

    private String nickName;
    // 아이디와 비밀번호 찾기때 이용
    private String mail;
    private String password;

    private String token;

    private List<Authority> roles = new ArrayList<>();

    public UserInfoRes(String nickName, String mail,
        String password, String token, List<Authority> roles) {
        this.nickName = nickName;
        this.mail = mail;
        this.password = password;
        this.token = token;
        this.roles = roles;
    }
}
