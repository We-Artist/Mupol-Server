package com.mupol.mupolserver.dto.video;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ViewOptionReqDto {
    private Long videoId;

    @ApiModelProperty(notes = "대표영상:0, 공개:1, 비공개:2")
    private Long option;
}
