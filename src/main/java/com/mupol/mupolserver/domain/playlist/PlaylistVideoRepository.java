package com.mupol.mupolserver.domain.playlist;

import com.mupol.mupolserver.domain.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistVideoRepository extends JpaRepository<PlaylistVideo, Long> {

    Optional <Long> countByPlaylistId(Long playlistId);

    Optional <List<PlaylistVideo>> findByPlaylistId(Long playlistId);

    Optional <List<PlaylistVideo>> findAllByVideo(Video video);

    boolean existsPlaylistVideoByPlaylistAndVideo(Playlist playlist, Video video);

    Optional <PlaylistVideo> deleteByPlaylistIdAndVideoId(Long playlistId, Long videoId);
}
