package com.mupol.mupolserver.dto.video;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class VideoReqDto {

    @ApiModelProperty(notes = "비디오 제목")
    private String title;

    @ApiModelProperty(notes = "원곡 제목")
    private String originTitle;

    @ApiModelProperty(notes = "비디오 상세 설명")
    private String detail;

    @ApiModelProperty(notes = "공개 여부")
    private Boolean isPrivate;

    @ApiModelProperty(notes = "악기 목록")
    private List<String> instrumentList;

    @ApiModelProperty(notes = "해시태그 목록")
    private List<String> hashtagList;
}
