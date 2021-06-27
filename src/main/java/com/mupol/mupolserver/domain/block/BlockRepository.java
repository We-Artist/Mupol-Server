package com.mupol.mupolserver.domain.block;

import com.mupol.mupolserver.domain.comment.Block;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    Optional<Video> deleteByBlockerAndBlocked(User blocker, User blocked);
}