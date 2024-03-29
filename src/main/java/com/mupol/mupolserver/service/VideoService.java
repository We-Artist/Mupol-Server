package com.mupol.mupolserver.service;

import com.amazonaws.util.IOUtils;
import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.advice.exception.sign.UserDoesNotAgreeException;
import com.mupol.mupolserver.domain.block.Block;
import com.mupol.mupolserver.domain.block.BlockRepository;
import com.mupol.mupolserver.domain.comment.Comment;
import com.mupol.mupolserver.domain.common.CacheKey;
import com.mupol.mupolserver.domain.common.MediaType;
import com.mupol.mupolserver.domain.follower.Follower;
import com.mupol.mupolserver.domain.follower.FollowerRepository;
import com.mupol.mupolserver.domain.hashtag.Hashtag;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.like.Like;
import com.mupol.mupolserver.domain.notification.TargetType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.domain.video.VideoRepository;
import com.mupol.mupolserver.domain.viewHistory.ViewHistory;
import com.mupol.mupolserver.domain.viewHistory.ViewHistoryRepository;
import com.mupol.mupolserver.dto.video.*;
import com.mupol.mupolserver.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
    private final NotificationService notificationService;
    private final BlockRepository blockRepository;

//    @Caching(evict = {
//            @CacheEvict(value = CacheKey.VIDEO_ID, key = "#user.getId().toString()"),
//            @CacheEvict(value = CacheKey.VIDEOS_USER_ID, key = "#user.getId().toString()"),
//            @CacheEvict(value = CacheKey.VIDEOS_KEYWORD, allEntries = true),
//            @CacheEvict(value = CacheKey.MONTH_VIDEOS, key = "#user.getId().toString()")
//    })

    @Async
    @Transactional
    public void uploadVideo(String filePath, User user, VideoReqDto metaData) throws IOException, InterruptedException {

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
                for (String hash : hashtags) hashtagList.add(Hashtag.valueOf(hash));
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
        ffmpegService.splitMedia(filePath, MediaType.Video);

        //get thumbnail
        ffmpegService.createThumbnail(filePath);

        //get ratio
        VideoWidthHeightDto widthHeightDto = ffmpegService.getVideoWidthAndHeight(filePath);
        video.setWidth(widthHeightDto.getWidth());
        video.setHeight(widthHeightDto.getHeight());

        //get video duration(length)
        Long length = ffmpegService.getMediaLength(filePath, MediaType.Video);
        video.setLength(length);

        // upload split video
        String fileUrl = s3Service.uploadMediaFolder(new File(filePath), userId, videoId, MediaType.Video);
        video.setFileUrl(fileUrl);

        //upload thumbnail
        File thumbnail = new File( filePath + "thumbnail.png");
        FileInputStream input = new FileInputStream(thumbnail);
        MultipartFile multipartFile = new MockMultipartFile("thumbnail", thumbnail.getName(), "text/plain", IOUtils.toByteArray(input));
        String thumbnailUrl = s3Service.uploadThumbnail(multipartFile, userId, videoId);
        video.setThumbnailUrl(thumbnailUrl);

        videoRepository.save(video);

        // remove dir
        deleteFolder(new File(filePath));

        notificationService.send(
                user,
                user,
                video,
                false,
                TargetType.video_posted
        );

//        return getVideoWithSaveDto(null, video);
    }

    //    @Cacheable(value = CacheKey.VIDEO_ID, key = "#videoId.toString()", unless = "#result == null")
    public Video getVideo(Long videoId) {
        return videoRepository.findById(videoId).orElseThrow(() -> new IllegalArgumentException("not exist video"));
    }

    //    @Cacheable(value = CacheKey.VIDEOS_USER_ID, key = "#userId", unless = "#result == null")
    public List<Video> getVideos(Long userId) {
        return videoRepository.findVideosByUserId(userId).orElseThrow();
    }

    public void likeVideo(User user, Video video) throws IOException {
        if (!likeService.isLiked(user, video)) {
            likeService.save(Like.builder().user(user).video(video).build());
            notificationService.send(
                    user,
                    video.getUser(),
                    video,
                    followerService.isFollowingUser(video.getUser(), user),
                    TargetType.like
            );
        } else {
            likeService.delete(user, video);
        }
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

    public VideoPageDto getHotVideo(int pageNum, User user) {
        List<Block> blockedList = new ArrayList<>();
        blockedList = blockRepository.findBlockedByBlocker(user).orElseThrow();

        List<Long> blockedIdList = new ArrayList<>();
        for (Block b : blockedList)
            blockedIdList.add(b.getBlocked().getId());

        PageRequest pageRequest = PageRequest.of(pageNum, 20);

        LocalDate now = LocalDate.now();
        LocalDate monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
        LocalDate sunday = now.withDayOfWeek(DateTimeConstants.SUNDAY);
        LocalDateTime start = LocalDateTime.of(java.time.LocalDate.of(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth()),
                LocalTime.of(0, 0, 0));

        LocalDateTime end = LocalDateTime.of(java.time.LocalDate.of(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth()),
                LocalTime.of(23, 59, 59));

        List<Long> videoIdList = new ArrayList<>();
        videoIdList = viewHistoryRepository.getHotVideoList(start, end).orElseThrow();

        VideoPageDto dto = new VideoPageDto();
        Page<Video> result;
        if (blockedList.isEmpty())
            result = videoRepository.findByIdInOrderByViewNumDesc(videoIdList, pageRequest).orElseThrow();
        else
            result = videoRepository.findByIdInAndUserIdNotInOrderByViewNumDesc(videoIdList, blockedIdList, pageRequest).orElseThrow();
        dto.setVideoList(result.getContent());

        boolean prev = result.getNumber() - 1 >= 0 && result.getNumber() - 1 <= result.getTotalPages() - 1;
        boolean next = result.getNumber() + 1 >= 0 && result.getNumber() + 1 <= result.getTotalPages() - 1;

        dto.setHasPrevPage(prev);
        dto.setHasNextPage(next);

        return dto;
    }

    public VideoPageDto getNewVideo(int pageNum, User user) {
        List<Block> blockedList = new ArrayList<>();
        blockedList = blockRepository.findBlockedByBlocker(user).orElseThrow();

        List<Long> blockedIdList = new ArrayList<>();
        for (Block b : blockedList)
            blockedIdList.add(b.getBlocked().getId());

        PageRequest pageRequest = PageRequest.of(pageNum, 20);
        VideoPageDto dto = new VideoPageDto();
        Page<Video> result;
        result = videoRepository.findAllByOrderByCreatedAtDesc(pageRequest).orElseThrow();
        if (blockedList.isEmpty())
            result = videoRepository.findAllByOrderByCreatedAtDesc(pageRequest).orElseThrow();
        else
            result = videoRepository.findAllByUserIdNotInOrderByCreatedAtDesc(blockedIdList, pageRequest).orElseThrow();
        dto.setVideoList(result.getContent());

        boolean prev = result.getNumber() - 1 >= 0 && result.getNumber() - 1 <= result.getTotalPages() - 1;
        boolean next = result.getNumber() + 1 >= 0 && result.getNumber() + 1 <= result.getTotalPages() - 1;

        dto.setHasPrevPage(prev);
        dto.setHasNextPage(next);

        return dto;
    }

    public Video getRandomVideo(User user) {
        List<Block> blockedList = new ArrayList<>();
        blockedList = blockRepository.findBlockedByBlocker(user).orElseThrow();

        List<Long> blockedIdList = new ArrayList<>();
        for (Block b : blockedList)
            blockedIdList.add(b.getBlocked().getId());

        if (blockedIdList.isEmpty())
            return videoRepository.getRandomVideo().orElseThrow();
        else
            return videoRepository.getRandomVideo(blockedIdList).orElseThrow();
    }

    public void deleteVideo(Long userId, Long videoId) {
        if (videoRepository.findByIdAndUserId(videoId, userId).isEmpty())
            throw new IllegalArgumentException("invalid videoId or invalid user");
        s3Service.deleteMedia(userId, videoId, MediaType.Video);
        videoRepository.deleteById(videoId);
    }

    public void deleteUserVideo(Long userId){
        List<Video> videoList = videoRepository.findVideosByUserId(userId).orElseThrow();

        for (Video v : videoList) {
            deleteVideo(userId, v.getId());
        }
    }

    public VideoPageDto getUserVideoList(Long userId, int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 20);

        VideoPageDto dto = new VideoPageDto();
        Page<Video> result = videoRepository.findAllByUserId(userId, pageRequest).orElseThrow();
        dto.setVideoList(result.getContent());

        boolean prev = result.getNumber() - 1 >= 0 && result.getNumber() - 1 <= result.getTotalPages() - 1;
        boolean next = result.getNumber() + 1 >= 0 && result.getNumber() + 1 <= result.getTotalPages() - 1;

        dto.setHasPrevPage(prev);
        dto.setHasNextPage(next);

        return dto;
    }

    public VideoPageDto getFollowingVideo(User user, int pageNum) {
        List<Block> blockedList = new ArrayList<>();
        blockedList = blockRepository.findBlockedByBlocker(user).orElseThrow();

        List<Long> blockedIdList = new ArrayList<>();
        for (Block b : blockedList)
            blockedIdList.add(b.getBlocked().getId());

        PageRequest pageRequest = PageRequest.of(pageNum, 20);

        List<Follower> followersList = followerRepository.findToIdByFromId(user.getId()).orElseThrow();
        List<Long> followerIdList = followersList.stream().map(Follower::getId).collect(Collectors.toList());

        VideoPageDto dto = new VideoPageDto();
        Page<Video> result;
        result = videoRepository.findByUserIdInOrderByCreatedAtDesc(followerIdList, pageRequest).orElseThrow();
        if (blockedList.isEmpty())
            result = videoRepository.findByUserIdInOrderByCreatedAtDesc(followerIdList, pageRequest).orElseThrow();
        else
            result = videoRepository.findByUserIdInAndUserIdNotInOrderByCreatedAtDesc(followerIdList, blockedIdList, pageRequest).orElseThrow();
        dto.setVideoList(result.getContent());

        boolean prev = result.getNumber() - 1 >= 0 && result.getNumber() - 1 <= result.getTotalPages() - 1;
        boolean next = result.getNumber() + 1 >= 0 && result.getNumber() + 1 <= result.getTotalPages() - 1;

        dto.setHasPrevPage(prev);
        dto.setHasNextPage(next);

        return dto;
    }

    public VideoPageDto getInstVideo(User user, int pageNum) {
        List<Block> blockedList = new ArrayList<>();
        blockedList = blockRepository.findBlockedByBlocker(user).orElseThrow();

        List<Long> blockedIdList = new ArrayList<>();
        for (Block b : blockedList)
            blockedIdList.add(b.getBlocked().getId());

        PageRequest pageRequest = PageRequest.of(pageNum, 20);

        List<Instrument> instrumentList = new ArrayList<>();
        instrumentList = user.getFavoriteInstrument();

        VideoPageDto dto = new VideoPageDto();
        Page<Video> result;
        result = videoRepository.findAllByInstrumentsInOrderByCreatedAtDesc(instrumentList, pageRequest).orElseThrow();
        if (blockedList.isEmpty())
            result = videoRepository.findAllByInstrumentsInOrderByCreatedAtDesc(instrumentList, pageRequest).orElseThrow();
        else
            result = videoRepository.findAllByInstrumentsInAndUserIdNotInOrderByCreatedAtDesc(instrumentList, blockedIdList, pageRequest).orElseThrow();
        dto.setVideoList(result.getContent());

        boolean prev = result.getNumber() - 1 >= 0 && result.getNumber() - 1 <= result.getTotalPages() - 1;
        boolean next = result.getNumber() + 1 >= 0 && result.getNumber() + 1 <= result.getTotalPages() - 1;

        dto.setHasPrevPage(prev);
        dto.setHasNextPage(next);

        return dto;
    }

    public List<VideoResDto> getVideoWithCommentDtoList(User user, List<Video> videoList) {
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
                .createdAt(TimeUtils.getUnixTimestamp(video.getCreatedAt()))
                .fileUrl(video.getFileUrl())
                .instrumentList(video.getInstruments())
                .userId(video.getUser().getId())
                .likeNum(likeService.getVideoLikeNum(video))
                .likeFlag(user != null && likeService.isLiked(user, video))
                .commentNum(commentList.size())
                .commentResDtoList(commentService.getCommentDtoList(commentList))
                .hashtagList(video.getHashtags())
                .isFollowing(user != null && followerService.isFollowingUser(user, video.getUser()))
                .saveFlag(user != null && playlistService.amISavedVideo(user, video))
                .saveNum(playlistService.getSavedVideoCount(video))
                .width(video.getWidth())
                .height(video.getHeight())
                .isRepresentativeVideo(user != null && video.getUser().getId().equals(user.getId()) && user.getRepresentativeVideoId().equals(video.getId()))
                .isMine(user != null && video.getUser().getId().equals(user.getId()))
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
                .createdAt(TimeUtils.getUnixTimestamp(video.getCreatedAt()))
                .fileUrl(video.getFileUrl())
                .instrumentList(video.getInstruments())
                .commentNum(commentService.getComments(video.getId()).size())
                .userId(video.getUser().getId())
                .likeNum(likeService.getVideoLikeNum(video))
                .likeFlag(user != null && likeService.isLiked(user, video))
                .hashtagList(video.getHashtags())
                .saveFlag(user != null && playlistService.amISavedVideo(user, video))
                .saveNum(playlistService.getSavedVideoCount(video))
                .build();
    }

    public VideoResDto getVideoWithCommentDto(User user, Video video) {
        List<Comment> commentList = commentService.getComments(video.getId());
        return VideoResDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .thumbnailUrl(video.getThumbnailUrl())
                .length(video.getLength())
                .originTitle(video.getOriginTitle())
                .detail(video.getDetail())
                .isPrivate(video.getIsPrivate())
                .createdAt(TimeUtils.getUnixTimestamp(video.getCreatedAt()))
                .fileUrl(video.getFileUrl())
                .instrumentList(video.getInstruments())
                .userId(video.getUser().getId())
                .likeNum(likeService.getVideoLikeNum(video))
                .likeFlag(user != null && likeService.isLiked(user, video))
                .commentNum(commentList.size())
                .build();
    }

    public ViewHistoryDto getViewHistory(ViewHistory viewHistory) {
        ViewHistoryDto dto = new ViewHistoryDto();

        dto.setCreatedAt(TimeUtils.getUnixTimestamp(viewHistory.getCreatedAt()));
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
        for (File subFile : Objects.requireNonNull(file.listFiles())) {
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

    public List<Video> getNextVideoList(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();

        Optional<List<Video>> videos = videoRepository.findTop10ByUserIdAndCreatedAtGreaterThan(video.getUser().getId(), video.getCreatedAt());
        return videos.orElse(Collections.emptyList());
    }

    //    @Cacheable(value = CacheKey.MONTH_VIDEOS, key = "#user.getId().toString()", unless = "#result == null")
    public List<VideoWithSaveDto> getVideoAtMonth(User user, int year, int month) {
        LocalDateTime start = TimeUtils.getStartDate(year, month);
        LocalDateTime end = TimeUtils.getEndDate(year, month);

        List<Video> videoList = videoRepository.findAllByUserIdAndCreatedAtBetween(user.getId(), start, end)
                .orElseThrow(() -> new IllegalArgumentException("video list error"));

        return getVideoWitheSaveDtoList(user, videoList);
    }

    public Integer getVideoCountAtMonth(User user, int year, int month) {
        return getVideoAtMonth(user, year, month).size();
    }

    public void setViewOption(User user, Video video, Boolean isPrivate) {
        if (user != video.getUser()) throw new UserDoesNotAgreeException("invalid user");
        video.setIsPrivate(!isPrivate);
        videoRepository.save(video);
    }
}
