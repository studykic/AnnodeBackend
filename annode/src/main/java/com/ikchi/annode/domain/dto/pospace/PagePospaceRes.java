package com.ikchi.annode.domain.dto.pospace;

import com.ikchi.annode.Enum.Constants.Visibility;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class PagePospaceRes {


    private Long pospaceId; // 방 id이자 index

    private int pospaceLikeCount;

    private String pospaceContent;

    // 사진은 기본 무한 스크롤로 게시글 조회시 1개만 넣기
    private List<String> pospaceImgFileUrlList;

    private LocalDateTime createdTime;

    private String writerIdentifier;

    private String writerNickname;

    private String writerProfileImgFileUrl;

    private Visibility pospaceKind;


    public PagePospaceRes(Long pospaceId, int pospaceLikeCount, String pospaceContent,
        LocalDateTime createdTime,
        String writerIdentifier,
        String writerNickname,
        String writerProfileImgFileUrl,
        Visibility pospaceKind
    ) {
        this.pospaceId = pospaceId;
        this.pospaceLikeCount = pospaceLikeCount;
        this.pospaceContent = pospaceContent;
        this.writerIdentifier = writerIdentifier;
        this.writerNickname = writerNickname;
        this.writerProfileImgFileUrl = writerProfileImgFileUrl;
        this.createdTime = createdTime;
        this.pospaceKind = pospaceKind;

    }
}



