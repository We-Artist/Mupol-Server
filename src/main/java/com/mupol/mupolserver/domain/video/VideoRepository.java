package com.mupol.mupolserver.domain.video;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<List<Video>> findVideosByUserId(Long userId);
    Optional<List<Video>> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    Optional<List<Video>> findTop20ByOrderByLikeNumDesc();
    Optional<List<Video>> findTop20ByOrderByCreatedAtDesc();
}
