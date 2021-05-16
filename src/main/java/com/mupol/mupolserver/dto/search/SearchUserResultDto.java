package com.mupol.mupolserver.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SearchUserResultDto {
    private Long userId;
    private String username;
    private String profileImageUrl;
}
