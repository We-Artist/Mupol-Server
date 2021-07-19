package com.mupol.mupolserver.domain.block;

import com.mupol.mupolserver.domain.block.Block;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    @Transactional
    Optional<Block> deleteByBlockerAndBlocked(User blocker, User blocked);
    Optional<Boolean> existsByBlockerAndBlocked(User blocker, User blocked);
    Optional<List<Block>> findBlockedByBlocker(User blocker);
}