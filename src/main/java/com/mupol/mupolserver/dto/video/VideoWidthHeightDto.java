package com.mupol.mupolserver.dto.video;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class VideoWidthHeightDto {
    private Long width;
    private Long height;
}
