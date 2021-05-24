package com.mupol.mupolserver.domain.playlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistVideoRepository extends JpaRepository<PlaylistVideo, Long> {

    Optional <Long> countByPlaylistId(Long playlistId);

    Optional <List<PlaylistVideo>> findByPlaylistId(Long playlistId);

    Optional <PlaylistVideo> deleteByPlaylistIdAndVideoId(Long playlistId, Long videoId);
}
