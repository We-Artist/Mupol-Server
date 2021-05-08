package com.mupol.mupolserver.service;

import com.amazonaws.util.IOUtils;
import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.domain.common.MediaType;
import com.mupol.mupolserver.domain.hashtag.Hashtag;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.domain.video.VideoRepository;
import com.mupol.mupolserver.dto.video.VideoReqDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final S3Service s3Service;
    private final FFmpegService ffmpegService;

    @Value("${ffmpeg.path.upload}")
    private String fileBasePath;

    public VideoResDto uploadVideo(MultipartFile videoFile, User user, VideoReqDto metaData) throws IOException, InterruptedException {

        List<String> instruments = metaData.getInstrument_list();
        List<Instrument> instrumentList = new ArrayList<>();

        try {
            for (String inst : instruments) instrumentList.add(Instrument.valueOf(inst));
        } catch (Exception e) {
            throw new InstrumentNotExistException();
        }

        List<String> hashtags = metaData.getHashtag_list();
        List<Hashtag> hashtagList = new ArrayList<>();

        try {
            for (String inst : hashtags) hashtagList.add(Hashtag.valueOf(inst));
        } catch (Exception e) {
            throw new InstrumentNotExistException();
        }

        Video video = Video.builder()
                .title(metaData.getTitle())
                .origin_title(metaData.getOrigin_title())
                .detail(metaData.getDetail())
                .is_private(metaData.getIs_private())
                .instrument_list(instrumentList)
                .hashtag_list(hashtagList)
                .view_num(0)
                .user(user)
                .build();
        videoRepository.save(video);

        Long userId = user.getId();
        Long videoId = video.getId();

        // split video
        ffmpegService.splitMedia(videoFile, userId, videoId, MediaType.Video);

        //get thumbnail
        ffmpegService.createThumbnail(videoFile, userId, videoId);

        // upload split video
        File folder = new File(fileBasePath + userId + "/" + videoId);
        String fileUrl = s3Service.uploadMediaFolder(folder, userId, videoId, MediaType.Video);
        video.setFileUrl(fileUrl);

        //upload thumbnail
        File thumbnail = new File(fileBasePath + userId + "/" + videoId + "/thumbnail.png");
        FileInputStream input = new FileInputStream(thumbnail);
        MultipartFile multipartFile = new MockMultipartFile("thumbnail", thumbnail.getName(), "text/plain", IOUtils.toByteArray(input));
        String thumbnailUrl = s3Service.uploadThumbnail(multipartFile, userId, videoId);
        video.setThumbnailUrl(thumbnailUrl);

        videoRepository.save(video);

        // remove dir
        deleteFolder(new File(fileBasePath + userId));

        return getVideoDto(video);
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

    public Video likeVideo(Long userId, Long videoId){
        Video video = getVideo(videoId);
        video.setLike_num(video.getLike_num()+1);
        videoRepository.save(video);

        return video;
    }

    public Video viewVideo(Long userId, Long videoId){
        Video video = getVideo(videoId);
        video.setView_num(video.getView_num()+1);
        videoRepository.save(video);

        return video;
    }

    public void deleteVideo(Long userId, Long videoId) {
        s3Service.deleteMedia(userId, videoId, MediaType.Video);
        videoRepository.deleteById(videoId);
    }

    public List<VideoResDto> getVideoDtoList(List<Video> VideoList) {
        return VideoList.stream().map(this::getVideoDto).collect(Collectors.toList());
    }

    public VideoResDto getVideoDto(Video video) {

        VideoResDto dto = new VideoResDto();

        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setOrigin_title(video.getOrigin_title());
        dto.setDetail(video.getDetail());
        dto.setIs_private(video.getIs_private());
        dto.setCreatedAt(video.getCreatedAt());
        dto.setUpdatedAt(video.getModifiedDate());
        dto.setFileUrl(video.getFileUrl());
        dto.setInstrument_list(video.getInstrument_list());
        dto.setView_num(video.getView_num());
        dto.setUserId(video.getUser().getId());
        dto.setLike_num(video.getLike_num());
        dto.setHashtag_list(video.getHashtag_list());
        return dto;
    }

    static void deleteFolder(File file){
        for (File subFile : file.listFiles()) {
            if(subFile.isDirectory()) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }

    public List<VideoResDto> getVideoAtMonth(User user, int year, int month) {
        Calendar cal = Calendar.getInstance();
        int lastDate = cal.getActualMaximum(Calendar.DATE);

        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.of(0,0,0));
        LocalDateTime end = LocalDateTime.of(LocalDate.of(year, month, lastDate), LocalTime.of(23,59,59));

        List<Video> videoList = videoRepository.findAllByUserIdAndCreatedAtBetween(user.getId(), start, end)
                .orElseThrow(() -> new IllegalArgumentException("video list error"));

        return getVideoDtoList(videoList);
    }
}
