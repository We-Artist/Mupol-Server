package com.mupol.mupolserver.dto.playlist;

import io.swagger.annotations.ApiParam;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class PlaylistVideoDto {

    private LocalDateTime createdAt;

    @ApiParam("연결되는 영상 id")
    private Long videoId;

    @ApiParam("연결되는 재생목록 id")
    private Long playlistId;
}
