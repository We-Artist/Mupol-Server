package com.mupol.mupolserver.domain.search;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SearchRepository extends JpaRepository<Search, Long> {
    Optional<List<Search>> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
