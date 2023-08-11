package com.ikchi.annode.domain.dto.pospace;

import com.ikchi.annode.Enum.Constants.Visibility;
import com.ikchi.annode.domain.dto.user.UserSimpleRes;
import com.ikchi.annode.domain.entity.Pospace;
import com.ikchi.annode.domain.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PospaceInfoRes {


    private UserSimpleRes writerSimpleRes;

    private Long pospaceId;

    private String pospaceContent;

    private int pospaceLikeCount;

    private int maxAnnode;

    private Visibility visibility;

    private Boolean tolkOpen;

    private List<String> pospaceImgFileUrlList;

    private List<PospaceCommentRes> pospaceCommentList;

    private List<UserSimpleRes> userSimpleResTagList;

    private boolean editAuthority;

    private LocalDateTime pospceCreatedTime;


    //  정적 팩토리 메소드로 호출시 간략하게 DTO를 생성
    public static PospaceInfoRes fromPospaceAndUser(Pospace pospace, User user,
        List<User> userTagList) {
        PospaceInfoRes pospaceInfoRes = new PospaceInfoRes();

        pospaceInfoRes.pospaceId = pospace.getId();
        pospaceInfoRes.pospaceContent = pospace.getPospaceContent();
        pospaceInfoRes.pospaceLikeCount = pospace.getPospaceLikeUsers().size();
        pospaceInfoRes.maxAnnode = pospace.getMaxAnnode();

        pospaceInfoRes.visibility = pospace.getVisibility();
        pospaceInfoRes.tolkOpen = pospace.getTolkOpen();
        pospaceInfoRes.pospaceImgFileUrlList = pospace.getPospaceImgFileUrlList();
        pospaceInfoRes.pospceCreatedTime = pospace.getCreatedTime();

        List<UserSimpleRes> userSimpleResList = userTagList.stream().map(item ->
            new UserSimpleRes(item.getUserIdentifier(),
                item.getNickName(), item.getProfileImgFileUrl())).collect(Collectors.toList());

        pospaceInfoRes.userSimpleResTagList = userSimpleResList;

        // 안전한 유저정보로 변환한뒤 할당
        UserSimpleRes userSimpleRes = new UserSimpleRes(user.getUserIdentifier(),
            user.getNickName(), user.getProfileImgFileUrl());

        pospaceInfoRes.writerSimpleRes = userSimpleRes;

        // PospaceComment은 user객체가 포함되어있으니 안전하게 PospaceCommentRes로 변환함
        List<PospaceCommentRes> pospaceCommentList = pospace.getPospaceCommentList().stream()
            .map(PospaceCommentRes::fromPospaceComment)
            .collect(Collectors.toList());

        // 게시글반환DTO를 만들기위해 처리된 PospaceCommentRes를 댓글목록에 넣음
        pospaceInfoRes.pospaceCommentList = pospaceCommentList;

        return pospaceInfoRes;
    }
}
