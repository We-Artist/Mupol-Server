package com.mupol.mupolserver.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SearchVideoResultDto {
    private Long videoId;
    private String title;
    private String thumbnailUrl;
    private long likeNum;
    private long saveNum;
    private boolean isLiked;
    private boolean isSaved;
}
