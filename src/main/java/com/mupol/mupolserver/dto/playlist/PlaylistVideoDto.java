package com.mupol.mupolserver.dto.playlist;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PlaylistVideoDto {
    @ApiModelProperty(notes ="Id")
    private Long id;

    @ApiModelProperty(notes ="재생 목록 Id")
    private Long playlistId;

    @ApiModelProperty(notes ="비디오 Id")
    private Long videoId;

    @ApiModelProperty(notes ="재생 목록에 비디오 추가 시간")
    private LocalDateTime createdAt;
}
