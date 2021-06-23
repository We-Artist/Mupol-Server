package com.mupol.mupolserver.domain.like;

import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndVideo(User user, Video video);
    Optional<List<Like>> findAllByVideo(Video video);
}
