package com.ikchi.annode.domain.dto.pospace;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PospaceUpdate {

    private Long pospaceId;

    private String visibility;

    private Boolean tolkOpen;

    @Size(max = 300, message = "300자 이내로 작성해주세요")
    private String pospaceContent;

    private Integer maxAnnode;


    // 기존 Pospace 이미지중 제거할 이미지Url을 할당함
    private List<String> deleteFileImgList;

}
