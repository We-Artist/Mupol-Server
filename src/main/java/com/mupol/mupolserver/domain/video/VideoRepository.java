package com.mupol.mupolserver.domain.video;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<List<Video>> findVideosByUserId(Long userId);
    Optional<Video> findVideoByIdAndUserId(Long userId, Long videoId);
}
