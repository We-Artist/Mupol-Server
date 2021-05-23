package com.mupol.mupolserver.domain.playlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Optional <List<Playlist>> findPlaylistByUserId(Long userId);
}
