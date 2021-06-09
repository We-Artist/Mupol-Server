package com.mupol.mupolserver.service;

import com.amazonaws.util.IOUtils;
import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.domain.common.CacheKey;
import com.mupol.mupolserver.domain.common.MediaType;
import com.mupol.mupolserver.domain.follower.FollowerRepository;
import com.mupol.mupolserver.domain.hashtag.Hashtag;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.like.Like;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.domain.video.VideoRepository;
import com.mupol.mupolserver.domain.viewHistory.ViewHistory;
import com.mupol.mupolserver.domain.viewHistory.ViewHistoryRepository;
import com.mupol.mupolserver.dto.video.VideoReqDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import com.mupol.mupolserver.dto.video.ViewHistoryDto;
import com.mupol.mupolserver.util.MonthExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final FollowerRepository followerRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final S3Service s3Service;
    private final FFmpegService ffmpegService;
    private final LikeService likeService;

    @Value("${ffmpeg.path.upload}")
    private String fileBasePath;

    @Caching(evict = {
            @CacheEvict(value = CacheKey.VIDEO_ID, key = "#user.getId().toString()"),
            @CacheEvict(value = CacheKey.VIDEOS_USER_ID, key = "#user.getId().toString()"),
            @CacheEvict(value = CacheKey.VIDEOS_KEYWORD, allEntries = true),
            @CacheEvict(value = CacheKey.MONTH_VIDEOS, key = "#user.getId().toString()")
    }
    )
    public VideoResDto uploadVideo(MultipartFile videoFile, User user, VideoReqDto metaData) throws IOException, InterruptedException {

        System.out.println(metaData);
        log.info(metaData.getTitle());
        log.info(metaData.getOriginTitle());
        log.info(metaData.getDetail());
        List<String> instruments = metaData.getInstrumentList();
        List<Instrument> instrumentList = new ArrayList<>();

        try {
            if (instruments != null)
                for (String inst : instruments) {
                    log.info(inst);
                    instrumentList.add(Instrument.valueOf(inst));
                }
        } catch (Exception e) {
            e.printStackTrace();
            throw new InstrumentNotExistException();
        }

        List<String> hashtags = metaData.getHashtagList();
        List<Hashtag> hashtagList = new ArrayList<>();

        try {
            if (hashtags != null)
                for (String inst : hashtags) hashtagList.add(Hashtag.valueOf(inst));
        } catch (Exception e) {
            throw new IllegalArgumentException("not supported hashtag");
        }

        Video video = Video.builder()
                .title(metaData.getTitle())
                .originTitle(metaData.getOriginTitle())
                .detail(metaData.getDetail())
                .isPrivate(metaData.getIsPrivate())
                .instruments(instrumentList)
                .hashtags(hashtagList)
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

        //get video duration(length)
        Long length = ffmpegService.getVideoLength(videoFile, userId, videoId);
        video.setLength(length);

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

    @Cacheable(value = CacheKey.VIDEO_ID, key = "#videoId.toString()", unless = "#result == null")
    public Video getVideo(Long videoId) {
        return videoRepository.findById(videoId).orElseThrow(() -> new IllegalArgumentException("not exist video"));
    }

//    @Cacheable(value = CacheKey.VIDEOS_USER_ID, key = "#userId", unless = "#result == null")
    public List<Video> getVideos(Long userId) {
        return videoRepository.findVideosByUserId(userId).orElseThrow();
    }

    public void likeVideo(User user, Video video) {
        likeService.save(Like.builder()
                .user(user)
                .video(video)
                .build());
    }

    public ViewHistoryDto createViewHistory(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();

        ViewHistory viewHistory = ViewHistory
                .builder()
                .video(video)
                .build();
        viewHistoryRepository.save(viewHistory);

        return getViewHistory(viewHistory);
    }

    public List<Video> getHotVideo(int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 20);

        LocalDate now = LocalDate.now();
        LocalDate monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
        LocalDate sunday = now.withDayOfWeek(DateTimeConstants.SUNDAY);
        LocalDateTime start = LocalDateTime.of(java.time.LocalDate.of(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth()),
                LocalTime.of(0, 0, 0));

        LocalDateTime end = LocalDateTime.of(java.time.LocalDate.of(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth()),
                LocalTime.of(23, 59, 59));

        List<Long> weekVideoIdList = new ArrayList<>();
        weekVideoIdList = viewHistoryRepository.findVideoIdByCreatedAtBetween(start, end).orElseThrow();

        List<Long> hotVideoIdList = new ArrayList<>();
        hotVideoIdList = viewHistoryRepository.getHotVideoList(weekVideoIdList).orElseThrow();

        List<Video> videoList = new ArrayList<>();
        videoList = videoRepository.findByIdInOrderByViewNumDesc(hotVideoIdList, pageRequest).orElseThrow();

        return videoList;
    }

    public List<Video> getNewVideo(int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 20);
        return videoRepository.findAllByOrderByCreatedAtDesc(pageRequest).orElseThrow();
    }

    public Video getRandomVideo() {
        return videoRepository.getRandomVideo().orElseThrow();
    }

    public void deleteVideo(Long userId, Long videoId) {
        s3Service.deleteMedia(userId, videoId, MediaType.Video);
        videoRepository.deleteById(videoId);
    }

    public List<Video> getUserVideoList(Long userId, int pageNum){
        System.out.println(userId);
        PageRequest pageRequest = PageRequest.of(pageNum, 20);

        List<Video> videoList = new ArrayList<>();
        videoList = videoRepository.findAllByUserId(userId, pageRequest).orElseThrow();
        System.out.println(videoList.toArray().length);

        return videoList;
    }

    public List<Video> getFollowingVideo(User user, int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 20);

        List<Long> followersList = new ArrayList<>();
        followersList = followerRepository.findToIdByFromId(user.getId()).orElseThrow();

        List<Video> videoList = new ArrayList<>();
        videoList = videoRepository.findByUserIdInOrderByCreatedAtDesc(followersList, pageRequest).orElseThrow();

        return videoList;
    }

    public List<Video> getInstVideo(User user, int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 20);

        List<Instrument> instrumentList = new ArrayList<>();
        instrumentList = user.getFavoriteInstrument();

        Optional<List<Video>> videos = videoRepository.findAllByInstrumentsInOrderByCreatedAtDesc(instrumentList, pageRequest);

        if (videos.isEmpty()) return Collections.emptyList();
        return videos.get();
    }

    public List<VideoResDto> getVideoDtoList(List<Video> videoList) {
        return videoList.stream().map(this::getVideoDto).collect(Collectors.toList());
    }

    public VideoResDto getVideoDto(Video video) {
        VideoResDto dto = new VideoResDto();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setOriginTitle(video.getOriginTitle());
        dto.setDetail(video.getDetail());
        dto.setIsPrivate(video.getIsPrivate());
        dto.setCreatedAt(video.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli());
        dto.setUpdatedAt(video.getModifiedDate().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli());
        dto.setFileUrl(video.getFileUrl());
        dto.setInstrumentList(video.getInstruments());
        dto.setViewNum(video.getViewNum());
        dto.setUserId(video.getUser().getId());
        dto.setLikeNum(likeService.getVideoLikeNum(video));
        dto.setHashtagList(video.getHashtags());
        dto.setThumbnailUrl(video.getThumbnailUrl());
        dto.setLength(video.getLength());

        return dto;
    }

    public ViewHistoryDto getViewHistory(ViewHistory viewHistory) {
        ViewHistoryDto dto = new ViewHistoryDto();

        dto.setCreatedAt(viewHistory.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli());
        dto.setId(viewHistory.getId());
        dto.setVideoId(viewHistory.getVideo().getId());

        return dto;

    }

    public Video addViewNum(Long videoId){
        Video video = getVideo(videoId);
        createViewHistory(videoId);     //view history table
        video.setViewNum(video.getViewNum() + 1);       //video table
        videoRepository.save(video);

        return video;
    }

    static void deleteFolder(File file) {
        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }

    @Cacheable(value = CacheKey.VIDEOS_KEYWORD, key = "#keyword", unless = "#result == null")
    public List<Video> getVideoByTitle(String keyword) {
        Optional<List<Video>> videos = videoRepository.findAllByTitleContains(keyword);
        if (videos.isEmpty()) return Collections.emptyList();
        return videos.get();
    }

    public List<Video> getVideoByInstrument(Instrument instrument) {
        Optional<List<Video>> videos = videoRepository.findAllByInstrumentsContains(instrument);
        if (videos.isEmpty()) return Collections.emptyList();
        return videos.get();
    }

//    @Cacheable(value = CacheKey.MONTH_VIDEOS, key = "#user.getId().toString()", unless = "#result == null")
    public List<VideoResDto> getVideoAtMonth(User user, int year, int month) {
        LocalDateTime start = MonthExtractor.getStartDate(year, month);
        LocalDateTime end = MonthExtractor.getEndDate(year, month);

        List<Video> videoList = videoRepository.findAllByUserIdAndCreatedAtBetween(user.getId(), start, end)
                .orElseThrow(() -> new IllegalArgumentException("video list error"));

        return getVideoDtoList(videoList);
    }

    public Integer getVideoCountAtMonth(User user, int year, int month) {
        return getVideoAtMonth(user, year, month).size();
    }
}
