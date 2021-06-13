package com.mupol.mupolserver.dto.video;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Setter
@Getter
public class VideoWithCommentDto extends VideoResDto {

    @ApiModelProperty(notes = "댓글 수")
    private Integer commentNum;

}
