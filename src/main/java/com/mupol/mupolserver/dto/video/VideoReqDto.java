package com.mupol.mupolserver.dto.video;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class VideoReqDto {

    @ApiModelProperty(notes = "비디오 제목")
    private String title;
}
