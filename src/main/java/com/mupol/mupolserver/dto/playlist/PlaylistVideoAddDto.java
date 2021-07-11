package com.mupol.mupolserver.dto.playlist;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PlaylistVideoAddDto {
    private Long playlistId;
    private Long videoId;
}
