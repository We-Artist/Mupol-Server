package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.notification.TargetType;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.dto.video.VideoReqDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import com.mupol.mupolserver.service.*;
import com.mupol.mupolserver.util.MonthExtractor;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Api(tags = {"3. Video"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/video")
public class VideoController {

    private final UserService userService;
    private final VideoService videoService;
    private final MonthlyGoalService monthlyGoalService;
    private final NotificationService notificationService;
    private final FollowerService followerService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 업로드")
    @PostMapping(value = "/new", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<SingleResult<VideoResDto>> addVideo(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "title") @RequestParam String title,
            @ApiParam(value = "origin title") @RequestParam String originTitle,
            @ApiParam(value = "detail") @RequestParam String detail,
            @ApiParam(value = "is private (true/false)") @RequestParam boolean isPrivate,
            @ApiParam(value = "instrument list") @RequestParam(required = false) List<String> instrumentList,
            @ApiParam(value = "hashtag list") @RequestParam(required = false) List<String> hashtagList,
            @ApiParam(value = "영상파일") @RequestParam(value = "videoFile") MultipartFile videoFile
    ) throws IOException, InterruptedException {
        VideoReqDto metaData = VideoReqDto.builder()
                .title(title)
                .originTitle(originTitle)
                .detail(detail)
                .isPrivate(isPrivate)
                .instrumentList(instrumentList)
                .hashtagList(hashtagList)
                .build();
        User user = userService.getUserByJwt(jwt);
        if(videoFile == null || videoFile.isEmpty())
            throw new IllegalArgumentException("File is null");
        VideoResDto dto = videoService.uploadVideo(videoFile, user, metaData);
        if(monthlyGoalService.isGoalExist(user, MonthExtractor.getCurrentMonthFirstDate())) {
            monthlyGoalService.update(user);
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "내 비디오 전체 조회")
    @GetMapping("/all")
    public ResponseEntity<ListResult<VideoResDto>> getVideoList(
            @RequestHeader(value = "Authorization") String jwt
    ) {
        User user = userService.getUserByJwt(jwt);
        List<VideoResDto> dtoList = videoService.getVideoDtoList(videoService.getVideos(user.getId()));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 개별 조회")
    @GetMapping("/{videoId}")
    public ResponseEntity<SingleResult<VideoResDto>> getVideo(
            @PathVariable String videoId
    ) {
        VideoResDto dto = videoService.getVideoDto(videoService.getVideo(Long.valueOf(videoId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 삭제")
    @DeleteMapping("/{videoId}")
    public ResponseEntity<SingleResult<String>> deleteVideo(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String videoId
    ) {
        User user = userService.getUserByJwt(jwt);
        videoService.deleteVideo(user.getId(), Long.valueOf(videoId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 좋아요")
    @PatchMapping("/like/{videoId}")
    public ResponseEntity<SingleResult<String>> likeVideo(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String videoId
    ) throws IOException {
        User user = userService.getUserByJwt(jwt);
        Video video = videoService.getVideo(Long.valueOf(videoId));
        videoService.likeVideo(user, video);
        notificationService.send(
                user,
                video.getUser(),
                video,
                followerService.isAlreadyFollowed(video.getUser(), user),
                TargetType.like
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("video like"));
    }

    @ApiOperation(value = "비디오 조회수 추가")
    @PatchMapping("/view/{videoId}")
    public ResponseEntity<SingleResult<VideoResDto>> viewNumVideo(
            @PathVariable String videoId
    ) {
        VideoResDto dto = videoService.getVideoDto(videoService.addViewNum(Long.valueOf(videoId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiOperation(value = "최신 영상 조회 (20개씩)")
    @GetMapping("/view/new/{page}")
    public ResponseEntity<ListResult<VideoResDto>> viewNewVideo(
            @PathVariable int page
    ) {
        List<VideoResDto> dtoList = videoService.getVideoDtoList(videoService.getNewVideo(page));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiOperation(value = "인기 영상 조회 (20개씩)")
    @GetMapping("/view/hot/{page}")
    public ResponseEntity<ListResult<VideoResDto>> viewHotVideo(
            @PathVariable int page
    ) {
        List<VideoResDto> dtoList = videoService.getVideoDtoList(videoService.getHotVideo(page));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "팔로우 계정 영상 조회 (20개씩)")
    @GetMapping("/view/follow/user/{page}")
    public ResponseEntity<ListResult<VideoResDto>> viewFollowUserVideo(
            @RequestHeader("Authorization") String jwt,
            @PathVariable int page
    ) {
        User user = userService.getUserByJwt(jwt);
        List<VideoResDto> dtoList = videoService.getVideoDtoList(videoService.getFollowingVideo(user, page));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "팔로우 악기 영상 조회 (20개씩)")
    @GetMapping("/view/follow/inst/{page}")
    public ResponseEntity<ListResult<VideoResDto>> viewFollowInstVideo(
            @RequestHeader("Authorization") String jwt,
            @PathVariable int page
    ) {
        User user = userService.getUserByJwt(jwt);
        List<VideoResDto> dtoList = videoService.getVideoDtoList(videoService.getInstVideo(user, page));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiOperation(value = "추천 영상 조회")
    @GetMapping("/view/recommendation")
    public ResponseEntity<SingleResult<VideoResDto>> viewRecommendationVideo() {
        VideoResDto dto = videoService.getVideoDto(videoService.getRandomVideo());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiOperation(value = "특정인의 비디오 전체 조회(20개씩)")
    @GetMapping("/view/all/{userId}/{page}")
    public ResponseEntity<ListResult<VideoResDto>> getUserVideoList(
            @PathVariable String userId,
            @PathVariable int page
    ) {
        List<VideoResDto> dtoList = videoService.getVideoDtoList(videoService.getUserVideoList(Long.valueOf(userId), page));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }
}
