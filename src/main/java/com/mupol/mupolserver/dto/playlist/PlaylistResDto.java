package com.mupol.mupolserver.dto.playlist;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PlaylistResDto {
    @ApiModelProperty(notes ="재생 목록 Id")
    private Long id;

    @ApiModelProperty(notes ="댓글 작성자 Id")
    private Long userId;

    @ApiModelProperty(notes ="재생 목록 이름")
    private String name;

    @ApiModelProperty(notes ="재생 목록 작성 시간")
    private LocalDateTime createdAt;

    @ApiModelProperty(notes ="재생 목록 수정 시간")
    private LocalDateTime updatedAt;

    @ApiModelProperty(notes ="비디오 개수")
    private int videoNum;
}
