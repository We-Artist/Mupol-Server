package com.mupol.mupolserver.dto.playlist;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistResDto {
    @ApiModelProperty(notes ="보관함 Id")
    private Long id;

    @ApiModelProperty(notes ="보관함 작성자 Id")
    private Long userId;

    @ApiModelProperty(notes ="보관함 이름")
    private String name;

    @ApiModelProperty(notes ="보관함 작성 시간")
    private Long createdAt;

    @ApiModelProperty(notes ="보관함 수정 시간")
    private Long updatedAt;

    @ApiModelProperty(notes ="비디오 개수")
    private Long videoNum;

    @ApiModelProperty(notes ="보관함 썸네일")
    private String thumbnail;
}
