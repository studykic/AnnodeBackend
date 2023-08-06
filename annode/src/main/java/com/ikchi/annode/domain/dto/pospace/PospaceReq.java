package com.ikchi.annode.domain.dto.pospace;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
public class PospaceReq {


    private String pospaceContent;

    private List<MultipartFile> profileImageFiles;

    @NotBlank(message = "최대 어노드수를 지정해야합니다.")
    private Integer maxAnnode;

    @NotBlank(message = "Pospace의 공개범위를 지정해야합니다.")
    private String visibility;

    private Boolean tolkOpen;

    private List<String> userTagList;


    public PospaceReq(String pospaceContent, Integer maxAnnode, String visibility,
        List<String> userTagList, Boolean tolkOpen, List<MultipartFile> profileImageFiles) {
        this.pospaceContent = pospaceContent;
        this.maxAnnode = maxAnnode;
        this.visibility = visibility;
        this.userTagList = userTagList;
        this.profileImageFiles = profileImageFiles;
        this.tolkOpen = tolkOpen;
    }
}
