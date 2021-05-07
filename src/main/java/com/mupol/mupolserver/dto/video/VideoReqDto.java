package com.mupol.mupolserver.dto.video;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class VideoReqDto {

    @ApiModelProperty(notes = "비디오 제목")
    private String title;

    @ApiModelProperty(notes = "원곡 제목")
    private String origin_title;

    @ApiModelProperty(notes = "비디오 상세 설명")
    private String detail;

    @ApiModelProperty(notes = "공개 여부")
    private Boolean is_private;

    @ApiModelProperty(notes = "악기 목록")
    private List<String> instrument_list;

    @ApiModelProperty(notes = "해시태그 목록")
    private List<String> hashtag_list;
}
