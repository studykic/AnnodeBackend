package com.ikchi.annode.domain.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPhoneInfoRes {


    private String userIdentifier;

    private String nickName;

    private String profileImgFileUrl;

    private String phoneNumber;


    public UserPhoneInfoRes(String userIdentifier, String nickName, String profileImgFileUrl,
        String phoneNumber) {
        this.userIdentifier = userIdentifier;
        this.nickName = nickName;
        this.profileImgFileUrl = profileImgFileUrl;
        this.phoneNumber = phoneNumber;
    }
}
