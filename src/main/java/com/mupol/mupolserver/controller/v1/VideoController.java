package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.sign.UserDoesNotAgreeException;
import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.dto.monthlyGoal.MonthlyGoalDto;
import com.mupol.mupolserver.dto.video.*;
import com.mupol.mupolserver.service.*;
import com.mupol.mupolserver.util.TimeUtils;
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
    private final FollowerService followerService;
    private final FFmpegService ffmpegService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 업로드")
    @PostMapping(value = "/new", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<SingleResult<MonthlyGoalDto>> addVideo(
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
        if (user == null) throw new IllegalArgumentException("invalid user");
        if (videoFile == null || videoFile.isEmpty()) throw new IllegalArgumentException("File is null");
        String filePath = ffmpegService.saveTmpFile(videoFile, com.mupol.mupolserver.domain.common.MediaType.Video);
        videoService.uploadVideo(filePath, user, metaData);
        monthlyGoalService.update(user);
        MonthlyGoal goal = monthlyGoalService.getMonthlyGoal(user, TimeUtils.getCurrentMonthFirstDate());
        MonthlyGoalDto dto = monthlyGoalService.getDto(goal);
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
        if (user == null) throw new IllegalArgumentException("user not exist");
        List<VideoResDto> dtoList = videoService.getVideoWithCommentDtoList(user, videoService.getVideos(user.getId()));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 개별 조회")
    @GetMapping("/{videoId}")
    public ResponseEntity<SingleResult<VideoViewDto>> getVideo(
            @RequestHeader(name = "Authorization", required = false) String jwt,
            @PathVariable String videoId
    ) {
        User user = userService.getUserByJwt(jwt);
        Video video = videoService.getVideo(Long.valueOf(videoId));
        VideoViewDto dto = videoService.getVideoViewDto(user, video);
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
        if (user == null) throw new IllegalArgumentException("unauthorized");
        if (user != videoService.getVideo(Long.valueOf(videoId)).getUser()) throw new IllegalArgumentException("unauthorized");
        videoService.deleteVideo(user.getId(), Long.valueOf(videoId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 좋아요")
    @PatchMapping("/like/{videoId}")
    public ResponseEntity<SingleResult<String>> likeVideo(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @PathVariable String videoId
    ) throws IOException {
        User user = userService.getUserByJwt(jwt);
        if (user == null) throw new UserDoesNotAgreeException("invalid jwt");
        Video video = videoService.getVideo(Long.valueOf(videoId));
        videoService.likeVideo(user, video);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("video like"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 조회수 추가")
    @PatchMapping("/view/{videoId}")
    public ResponseEntity<SingleResult<VideoWithSaveDto>> viewNumVideo(
            @RequestHeader(name = "Authorization", required = false) String jwt,
            @PathVariable String videoId
    ) {
        User user = userService.getUserByJwt(jwt);
        VideoWithSaveDto dto = videoService.getVideoWithSaveDto(user, videoService.addViewNum(Long.valueOf(videoId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "최신 영상 조회 (20개씩)")
    @GetMapping("/view/new/{page}")
    public ResponseEntity<ListResult<VideoResDto>> viewNewVideo(
            @RequestHeader(name = "Authorization", required = false) String jwt,
            @PathVariable int page
    ) {
        User user = userService.getUserByJwt(jwt);
        VideoPageDto dto = videoService.getNewVideo(page, user);
        List<VideoResDto> dtoList = videoService.getVideoWithCommentDtoList(user, dto.getVideoList());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getPageListResult(dtoList, dto.isHasPrevPage(), dto.isHasNextPage()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "인기 영상 조회 (20개씩)")
    @GetMapping("/view/hot/{page}")
    public ResponseEntity<ListResult<VideoResDto>> viewHotVideo(
            @RequestHeader(name = "Authorization", required = false) String jwt,
            @PathVariable int page
    ) {
        User user = userService.getUserByJwt(jwt);
        VideoPageDto dto = videoService.getHotVideo(page, user);
        List<VideoResDto> dtoList = videoService.getVideoWithCommentDtoList(user, dto.getVideoList());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getPageListResult(dtoList, dto.isHasPrevPage(), dto.isHasNextPage()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "팔로우 계정 영상 조회 (20개씩)")
    @GetMapping("/view/follow/user/{page}")
    public ResponseEntity<ListResult<VideoResDto>> viewFollowUserVideo(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @PathVariable int page
    ) {
        User user = userService.getUserByJwt(jwt);
        if (user == null) throw new UserDoesNotAgreeException("invalid user");
        VideoPageDto dto = videoService.getFollowingVideo(user, page);
        List<VideoResDto> dtoList = videoService.getVideoWithCommentDtoList(user, dto.getVideoList());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getPageListResult(dtoList, dto.isHasPrevPage(), dto.isHasNextPage()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "팔로우 악기 영상 조회 (20개씩)")
    @GetMapping("/view/follow/inst/{page}")
    public ResponseEntity<ListResult<VideoResDto>> viewFollowInstVideo(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @PathVariable int page
    ) {
        User user = userService.getUserByJwt(jwt);
        if (user == null) throw new UserDoesNotAgreeException("invalid user");
        VideoPageDto dto = videoService.getInstVideo(user, page);
        List<VideoResDto> dtoList = videoService.getVideoWithCommentDtoList(user, dto.getVideoList());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getPageListResult(dtoList, dto.isHasPrevPage(), dto.isHasNextPage()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "추천 영상 조회")
    @GetMapping("/view/recommendation")
    public ResponseEntity<SingleResult<VideoResDto>> viewRecommendationVideo(
            @RequestHeader(name = "Authorization", required = false) String jwt
    ) {
        User user = userService.getUserByJwt(jwt);
        VideoResDto dto = videoService.getVideoWithCommentDto(user, videoService.getRandomVideo(user));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "특정인의 비디오 전체 조회(20개씩)")
    @GetMapping("/view/all/{userId}/{page}")
    public ResponseEntity<ListResult<VideoResDto>> getUserVideoList(
            @RequestHeader(name = "Authorization", required = false) String jwt,
            @PathVariable String userId,
            @PathVariable int page
    ) {
        User user = userService.getUserByJwt(jwt);
        VideoPageDto dto = videoService.getUserVideoList(Long.valueOf(userId), page);
        List<VideoResDto> dtoList = videoService.getVideoWithCommentDtoList(user, dto.getVideoList());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getPageListResult(dtoList, dto.isHasPrevPage(), dto.isHasNextPage()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "다음 영상 목록(최대 10개)")
    @GetMapping("/view/next/{videoId}")
    public ResponseEntity<ListResult<VideoResDto>> getNextVideoList(
            @RequestHeader(name = "Authorization", required = false) String jwt,
            @PathVariable String videoId
    ) {
        User user = userService.getUserByJwt(jwt);
        List<VideoResDto> dtoList = videoService.getVideoWithCommentDtoList(user, videoService.getNextVideoList(Long.valueOf(videoId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "영상 조회 옵션 설정")
    @PostMapping("/view-option")
    public ResponseEntity<SingleResult<String>> setVideoViewOption(
            @RequestHeader(name = "Authorization") String jwt,
            @ApiParam(value = "0:대표영상설정, 1:영상공개, 2:영상비공개") @RequestBody ViewOptionReqDto dto
    ) {
        User user = userService.getUserByJwt(jwt);
        Video video = videoService.getVideo(dto.getVideoId());
        Long option = dto.getOption();
        if (option == 0) {
            userService.setRepresentativeVideoId(user.getId(), video.getId());
            videoService.setViewOption(user, video, false);
        } else if (option == 1) {
            userService.deleteRepresentativeVideoId(user.getId());
            videoService.setViewOption(user, video, false);
        } else if (option == 2) {
            videoService.setViewOption(user, video, true);
        } else {
            throw new IllegalArgumentException("illegal view option");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("view option: " + dto.getOption()));
    }
}
