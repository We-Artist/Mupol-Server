package com.mupol.mupolserver.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class SearchVideoResultDto {
    private Long id;
    private Long userId;
    private String title;
    private String thumbnailUrl;
    private List<String> instrumentList;
    private long likeNum;
    private long commentNum;
    private boolean isLiked;
    private boolean isSaved;
}
