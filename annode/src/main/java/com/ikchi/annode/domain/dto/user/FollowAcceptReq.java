package com.ikchi.annode.domain.dto.user;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
public class FollowAcceptReq {

    @NotNull
    private Boolean accept;

    @NotNull
    private Long annodeFollowId;

}
