package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.like.Like;
import com.mupol.mupolserver.domain.like.LikeRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public void save(Like like) {
        likeRepository.save(like);
    }

    public boolean isLiked(User user, Video video) {
        return likeRepository.existsLikeByUserAndVideo(user, video);
    }

    public long getVideoLikeNum (Video video) {
        Optional<List<Like>> likeList = likeRepository.findAllByVideo(video);
        if(likeList.isEmpty())
            return 0;
        return likeList.get().size();
    }
}
