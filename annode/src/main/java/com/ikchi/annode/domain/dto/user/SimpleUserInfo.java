package com.ikchi.annode.domain.dto.user;

import lombok.Getter;

@Getter
public class SimpleUserInfo {


    private String writerIdentifier;

    private String writerNickname;

    private String writerProfileImgFileUrl;

    public SimpleUserInfo(String writerIdentifier, String writerNickname,
        String writerProfileImgFileUrl) {
        this.writerIdentifier = writerIdentifier;
        this.writerNickname = writerNickname;
        this.writerProfileImgFileUrl = writerProfileImgFileUrl;
    }
}
