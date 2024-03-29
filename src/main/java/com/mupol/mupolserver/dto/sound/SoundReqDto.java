package com.mupol.mupolserver.dto.sound;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SoundReqDto {

    @ApiModelProperty(notes = "녹음본 bpm")
    private int bpm;

    @ApiModelProperty(notes = "녹음본 제목")
    private String title;
}