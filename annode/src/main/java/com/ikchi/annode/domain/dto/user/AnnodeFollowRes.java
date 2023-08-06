package com.ikchi.annode.domain.dto.user;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnnodeFollowRes {

//    누구로부터 요청이왔는지 이름 + 이미지
//
//        우측엔 클릭버튼이있기에 클릭시 annodfollow Id로 수락여부 선택

    String requesterNickName;

    String profileImgFileUrl;

    String followRequsterIdentifier;

    // follow 요청 수락시 사용되는 요청의 고유 id
    Long AnnodeFollowId;

    public AnnodeFollowRes(String requesterNickName, String profileImgFileUrl,
        String followRequsterIdentifier, Long AnnodeFollowId) {
        this.requesterNickName = requesterNickName;
        this.profileImgFileUrl = profileImgFileUrl;
        this.followRequsterIdentifier = followRequsterIdentifier;
        this.AnnodeFollowId = AnnodeFollowId;
    }
}
