package com.mupol.mupolserver.dto.comment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class CommentReqDto {

    @ApiModelProperty(notes = "내용")
    private String content;
}
