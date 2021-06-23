package com.mupol.mupolserver.dto.video;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ViewOptionReqDto {
    private Long videoId;
    @ApiModelProperty(notes = "공개:true 비공개:false")
    private Boolean option;
}
