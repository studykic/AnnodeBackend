package com.ikchi.annode.domain.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserSimpleRes {


    private String userIdentifier;

    private String nickName;

    private String profileImgFileUrl;

    public UserSimpleRes(String userIdentifier, String nickName, String profileImgFileUrl) {
        this.userIdentifier = userIdentifier;
        this.nickName = nickName;
        this.profileImgFileUrl = profileImgFileUrl;
    }
}
