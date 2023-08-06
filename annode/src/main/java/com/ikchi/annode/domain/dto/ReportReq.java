package com.ikchi.annode.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class ReportReq {

    @NotNull
    private Long pospaceId;

    @NotBlank(message = "신고 내용을 입력해주세요")
    private String reportReason;

}
