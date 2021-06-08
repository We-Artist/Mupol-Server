package com.mupol.mupolserver.domain.viewHistory;

import com.mupol.mupolserver.domain.video.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    Optional <List<Video>> findByIdIn(List<Long> videoId, Pageable pageRequest);

    @Query(value = "select v.id " +
            "from mupol.video v " +
            "left join mupol.view_history vh on (vh.video_id = v.id) " +
            "where vh.video_id in (:history) " +
            "group by v.id " +
            "order by count(vh.video_id) desc;"
            , nativeQuery=true)
    Optional<List<Long>> getHotVideoList(@Param("history") List<Long> history);

    Optional<Long> countAllByVideoId(Long VideoId);

    @Query(value = "select video_id from view_history where created_at between :startTime and :endTime", nativeQuery=true)
    Optional <List<Long>> findVideoIdByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
