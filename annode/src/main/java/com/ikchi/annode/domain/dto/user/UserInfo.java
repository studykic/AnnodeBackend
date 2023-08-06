package com.ikchi.annode.domain.dto.user;


import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {

    private String nickName;
    private String email;
    private String userIdentifier;

    private String profileImgFileUrl;

    private LocalDateTime userCreatedDate;
    private int followerCount;
    private boolean followRelationShip;


    public UserInfo(String email, String nickName, String userIdentifier,
        String profileImgFileUrl, LocalDateTime userCreatedDate, int followerCount) {
        this.email = email;
        this.nickName = nickName;
        this.userIdentifier = userIdentifier;
        this.profileImgFileUrl = profileImgFileUrl;
        this.userCreatedDate = userCreatedDate;
        this.followerCount = followerCount;

    }
}
