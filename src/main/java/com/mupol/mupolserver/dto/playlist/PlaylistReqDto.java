package com.mupol.mupolserver.dto.playlist;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class PlaylistReqDto {
    @ApiModelProperty(notes = "재생 목록 제목")
    private String name;

}
