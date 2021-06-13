package com.mupol.mupolserver.service;

import com.amazonaws.util.IOUtils;
import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.domain.comment.Comment;
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
import com.mupol.mupolserver.dto.video.*;
import com.mupol.mupolserver.util.MonthExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
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
    private final PlaylistService playlistService;
    private final CommentService commentService;
    private final FollowerService followerService;

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

        //get ratio
        VideoWidthHeightDto widthHeightDto = ffmpegService.getVideoWidthAndHeight(videoFile, userId, videoId);
        video.setWidth(widthHeightDto.getWidth());
        video.setHeight(widthHeightDto.getHeight());

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

        return getVideoWithSaveDto(null, video);
    }

//    @Cacheable(value = CacheKey.VIDEO_ID, key = "#videoId.toString()", unless = "#result == null")
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
        if(videoRepository.findByIdAndUserId(videoId, userId).isEmpty())
            throw new IllegalArgumentException("invalid videoId or invalid user");
        s3Service.deleteMedia(userId, videoId, MediaType.Video);
        videoRepository.deleteById(videoId);
    }

    public List<Video> getUserVideoList(Long userId, int pageNum) {
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

    public List<VideoWithCommentDto> getVideoWithCommentDtoList(User user, List<Video> videoList) {
        return videoList.stream().map((video) -> getVideoWithCommentDto(user, video)).collect(Collectors.toList());
    }

    public List<VideoWithSaveDto> getVideoWitheSaveDtoList(User user, List<Video> videoList) {
        return videoList.stream().map((video) -> getVideoWithSaveDto(user, video)).collect(Collectors.toList());
    }

    public VideoViewDto getVideoViewDto(User user, Video video) {
        List<Comment> commentList = commentService.getComments(video.getId());
        return VideoViewDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .thumbnailUrl(video.getThumbnailUrl())
                .length(video.getLength())
                .originTitle(video.getOriginTitle())
                .detail(video.getDetail())
                .isPrivate(video.getIsPrivate())
                .createdAt(video.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
                .fileUrl(video.getFileUrl())
                .instrumentList(video.getInstruments())
                .viewNum(video.getViewNum())
                .userId(video.getUser().getId())
                .likeNum(likeService.getVideoLikeNum(video))
                .likeFlag(user != null && likeService.isLiked(user, video))
                .commentNum(commentList.size())
                .commentResDtoList(commentService.getCommentDtoList(commentList))
                .hashtagList(video.getHashtags())
                .isFollowing(user != null && followerService.isFollowingUser(user, video.getUser()))
                .saveFlag(user != null && playlistService.amISavedVideo(user,video))
                .saveNum(playlistService.getSavedVideoCount(video))
                .width(video.getWidth())
                .height(video.getHeight())
                // TODO: 다음 추천 영상 목록
                //.nextVideoList()
                .build();
    }

    public VideoWithSaveDto getVideoWithSaveDto(User user, Video video) {
        return VideoWithSaveDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .thumbnailUrl(video.getThumbnailUrl())
                .length(video.getLength())
                .originTitle(video.getOriginTitle())
                .detail(video.getDetail())
                .isPrivate(video.getIsPrivate())
                .createdAt(video.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
                .fileUrl(video.getFileUrl())
                .instrumentList(video.getInstruments())
                .viewNum(video.getViewNum())
                .userId(video.getUser().getId())
                .likeNum(likeService.getVideoLikeNum(video))
                .likeFlag(user != null && likeService.isLiked(user, video))
                .hashtagList(video.getHashtags())
                .saveFlag(user != null && playlistService.amISavedVideo(user,video))
                .saveNum(playlistService.getSavedVideoCount(video))
                .build();
    }

    public VideoWithCommentDto getVideoWithCommentDto(User user, Video video) {
        List<Comment> commentList = commentService.getComments(video.getId());
        return VideoWithCommentDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .thumbnailUrl(video.getThumbnailUrl())
                .length(video.getLength())
                .originTitle(video.getOriginTitle())
                .detail(video.getDetail())
                .isPrivate(video.getIsPrivate())
                .createdAt(video.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
                .fileUrl(video.getFileUrl())
                .instrumentList(video.getInstruments())
                .viewNum(video.getViewNum())
                .userId(video.getUser().getId())
                .likeNum(likeService.getVideoLikeNum(video))
                .likeFlag(user != null && likeService.isLiked(user, video))
                .commentNum(commentList.size())
                .build();
    }

    public ViewHistoryDto getViewHistory(ViewHistory viewHistory) {
        ViewHistoryDto dto = new ViewHistoryDto();

        dto.setCreatedAt(viewHistory.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli());
        dto.setId(viewHistory.getId());
        dto.setVideoId(viewHistory.getVideo().getId());

        return dto;

    }

    public Video addViewNum(Long videoId) {
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

//    @Cacheable(value = CacheKey.VIDEOS_KEYWORD, key = "#keyword", unless = "#result == null")
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
    public List<VideoWithSaveDto> getVideoAtMonth(User user, int year, int month) {
        LocalDateTime start = MonthExtractor.getStartDate(year, month);
        LocalDateTime end = MonthExtractor.getEndDate(year, month);

        List<Video> videoList = videoRepository.findAllByUserIdAndCreatedAtBetween(user.getId(), start, end)
                .orElseThrow(() -> new IllegalArgumentException("video list error"));

        return getVideoWitheSaveDtoList(user, videoList);
    }

    public Integer getVideoCountAtMonth(User user, int year, int month) {
        return getVideoAtMonth(user, year, month).size();
    }
}
