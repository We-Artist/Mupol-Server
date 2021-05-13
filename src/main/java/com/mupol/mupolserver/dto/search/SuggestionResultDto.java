package com.mupol.mupolserver.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;

@Getter
@Builder
@ToString
public class SuggestionResultDto {
    List<HashMap<String, String>> userList;
    List<HashMap<String, String>> videoListByTitle;
//    List<HashMap<String, String>> videoListByInstrument;
}
