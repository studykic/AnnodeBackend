package com.ikchi.annode.domain.dto.user;

import com.ikchi.annode.domain.vo.Authority;
import java.util.List;
import lombok.Getter;


@Getter
public class LoginRes {

    private String jwt;
    private List<Authority> roles;

    public LoginRes(String jwt, List<Authority> roles) {
        this.jwt = jwt;
        this.roles = roles;
    }
}
