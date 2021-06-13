package com.mupol.mupolserver.dto.video;

import com.mupol.mupolserver.domain.video.Video;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VideoPageDto {
    @ApiModelProperty(notes = "비디오 목록")
    private List<Video> videoList;

    @ApiModelProperty(notes = "이전 페이지 존재 여부")
    private boolean hasNextPage;

    @ApiModelProperty(notes = "다음 페이지 존재 여부")
    private boolean hasPrevPage;
}
