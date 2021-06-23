package com.mupol.mupolserver.dto.playlist;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PlaylistMoveVideoDto {
    private Long currentPlaylistId;
    private Long targetPlaylistId;
    private Long videoId;
}
