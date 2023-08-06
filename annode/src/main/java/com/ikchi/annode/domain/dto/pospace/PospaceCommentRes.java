package com.ikchi.annode.domain.dto.pospace;

import com.ikchi.annode.domain.dto.user.UserSimpleRes;
import com.ikchi.annode.domain.entity.PospaceComment;
import com.ikchi.annode.domain.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PospaceCommentRes {


    private Long id;

    private UserSimpleRes commentWriter;

    private String commentContent;


    public static PospaceCommentRes fromPospaceComment(PospaceComment pospaceComment) {
        PospaceCommentRes pospaceCommentRes = new PospaceCommentRes();
        pospaceCommentRes.id = pospaceComment.getId();
        pospaceCommentRes.commentContent = pospaceComment.getCommentContent();

        User commentWriter = pospaceComment.getCommentWriter();
        UserSimpleRes userSimpleRes = new UserSimpleRes(commentWriter.getUserIdentifier(),
            commentWriter.getNickName(), commentWriter.getProfileImgFileUrl());

        pospaceCommentRes.commentWriter = userSimpleRes;

        return pospaceCommentRes;
    }

}
