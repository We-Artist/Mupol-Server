package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.domain.video.VideoRepository;
import com.mupol.mupolserver.dto.video.VideoReqDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final S3Service s3Service;

    public VideoResDto uploadVideo(MultipartFile videoFile, User user, VideoReqDto metaData) throws IOException {
        Video video = Video.builder()
                .title(metaData.getTitle())
                .user(user)
                .build();
        videoRepository.save(video);

        String url = s3Service.uploadVideo(videoFile, user.getId(), video.getId());
        video.setFileUrl(url);
        videoRepository.save(video);

        return getSndDto(video);
    }

    public Video getVideo(Long videoId) {
        return videoRepository.findById(videoId).orElseThrow(() -> new IllegalArgumentException("not exist video"));
    }

    public List<Video> getVideos(Long userId) {
        return videoRepository.findVideosByUserId(userId).orElseThrow();
    }

    public Video updateTitle(Long videoId, String title) {
        Video video = getVideo(videoId);
        video.setTitle(title);
        videoRepository.save(video);

        return video;
    }

    public void deleteVideo(Long userId, Long videoId) {
        s3Service.deleteVideo(userId, videoId);
        videoRepository.deleteById(videoId);
    }

    public List<VideoResDto> getSndDtoList(List<Video> VideoList) {
        return VideoList.stream().map(this::getSndDto).collect(Collectors.toList());
    }

    public VideoResDto getSndDto(Video snd) {
        VideoResDto dto = new VideoResDto();
        dto.setId(snd.getId());
        dto.setTitle(snd.getTitle());
        dto.setOrigin_title(snd.getOrigin_title());
        dto.setIs_private(snd.getIs_private());
        dto.setUserId(snd.getUser().getId());
        dto.setFileUrl(snd.getFileUrl());
        return dto;
    }
}
