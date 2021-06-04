package com.mupol.mupolserver.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class FollowingResDto {
    private Long userId;
    private String username;
    private String profileImageUrl;
}