package com.mupol.mupolserver.domain.sound;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoundRepository extends JpaRepository<Sound, Long> {
    Optional<List<Sound>> findSoundsByUserId(Long userId);
    Optional<Sound> findSoundByIdAndUserId(Long userId, Long soundId);
}
