package com.mupol.mupolserver.domain.video;

import com.mupol.mupolserver.domain.instrument.Instrument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<List<Video>> findVideosByUserId(Long userId);

    Optional<List<Video>> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<List<Video>> findAllByOrderByCreatedAtDesc();

    Optional<List<Video>> findAllByTitleContains(String title);

    Optional<List<Video>> findAllByInstrumentsContains(Instrument instrument);

    Optional<Integer> countAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "select * from video order by rand() limit 1", nativeQuery=true)
    Optional<Video> getRandomVideo();

    Optional<Page<Video>> findByUserIdInOrderByCreatedAtDesc(List<Long> userId, Pageable pageRequest);

    Optional<Page<Video>> findAllByInstrumentsInOrderByCreatedAtDesc(List<Instrument> instrumentList, Pageable pageRequest);

    Optional<Page<Video>> findAllByOrderByCreatedAtDesc(Pageable pageRequest);

    Optional<Page<Video>> findAllByUserId(Long userId, Pageable pageRequest);

    Optional<Page<Video>> findByIdInOrderByViewNumDesc(List<Long> videoId, Pageable pageRequest);

    Optional<Video> findByIdAndUserId(Long id, Long userId);
}
