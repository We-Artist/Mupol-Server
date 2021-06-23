package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.like.Like;
import com.mupol.mupolserver.domain.like.LikeRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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

    public void delete(User user, Video video) {
        Optional<Like> like = likeRepository.findByUserAndVideo(user, video);
        like.ifPresent(likeRepository::delete);
    }

    public boolean isLiked(User user, Video video) {
        return likeRepository.findByUserAndVideo(user, video).isPresent();
    }

    public long getVideoLikeNum (Video video) {
        Optional<List<Like>> likeList = likeRepository.findAllByVideo(video);
        if(likeList.isEmpty())
            return 0;
        return likeList.get().size();
    }

    public List<Video> getLikedVideos(User user) {
        Optional<List<Like>> likeList = likeRepository.findAllByUserOrderByCreatedAt(user);
        if(likeList.isEmpty()) return Collections.emptyList();

        List<Video> videoList = new ArrayList<>();
        for(Like l: likeList.get()) {
            videoList.add(l.getVideo());
        }
        return videoList;
    }
}
