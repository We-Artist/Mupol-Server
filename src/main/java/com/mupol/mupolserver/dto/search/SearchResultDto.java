package com.mupol.mupolserver.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class SearchResultDto {
    List<SearchUserResultDto> userList;
    List<SearchVideoResultDto> videoListByTitle;
}
