package com.mupol.mupolserver.dto.video;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ViewHistoryDto {

    @ApiModelProperty(notes ="View History Id")
    private Long id;

    @ApiModelProperty(notes ="created at")
    private Long createdAt;

    @ApiModelProperty(notes = "Video Id")
    private Long videoId;
}
