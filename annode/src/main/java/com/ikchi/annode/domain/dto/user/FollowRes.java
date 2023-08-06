package com.ikchi.annode.domain.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class FollowRes {

    private String followNickName;

    private String followIdentifier;

    private String followprofileImgFileUrl;

    public FollowRes(String followNickName, String followIdentifier,
        String followprofileImgFileUrl) {
        this.followNickName = followNickName;
        this.followIdentifier = followIdentifier;
        this.followprofileImgFileUrl = followprofileImgFileUrl;
    }
}
