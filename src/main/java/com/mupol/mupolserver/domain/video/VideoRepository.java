package com.mupol.mupolserver.domain.video;

import com.mupol.mupolserver.domain.instrument.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<List<Video>> findVideosByUserId(Long userId);

    Optional<List<Video>> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<List<Video>> findAllByOrderByLikeNumDesc();

    Optional<List<Video>> findAllByOrderByCreatedAtDesc();

    Optional<List<Video>> findAllByTitleContains(String title);

    Optional<List<Video>> findAllByInstrumentsContains(Instrument instrument);

    Optional<Integer> countAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
