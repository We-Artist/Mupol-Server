package com.mupol.mupolserver.domain.sound;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SoundRepository extends JpaRepository<Sound, Long> {
    Optional<List<Sound>> findSoundsByUserId(Long userId);
    Optional<Sound> findSoundByIdAndUserId(Long userId, Long soundId);
    Optional<List<Sound>> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}
