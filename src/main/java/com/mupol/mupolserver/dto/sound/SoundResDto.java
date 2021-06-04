package com.mupol.mupolserver.dto.sound;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SoundResDto {
    @ApiModelProperty(notes ="Sound Id")
    private Long id;

    @ApiModelProperty(notes = "녹음본 bpm")
    private int bpm;

    @ApiModelProperty(notes = "녹음본 제목")
    private String title;

    @ApiModelProperty(notes ="created at")
    private Long createdAt;

    @ApiModelProperty(notes ="file url")
    private String fileUrl;

    @ApiModelProperty(notes = "User Id")
    private Long userId;
}