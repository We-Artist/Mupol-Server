package com.mupol.mupolserver.domain.viewHistory;

import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    @Query(value = "select video from view_history where modified_date between :startTime and :endTime group by video order by count(video)", nativeQuery=true)
    Optional<List<Long>> getHotVideoList(@Param("startTime") LocalDate startTime, @Param("endTime") LocalDate endTime);



}
