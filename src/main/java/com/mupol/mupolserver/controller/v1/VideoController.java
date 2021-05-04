package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.video.VideoReqDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.UserService;
import com.mupol.mupolserver.service.VideoService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 업로드")
    @PostMapping(value = "/new", consumes = {"multipart/form-data"})
    public ResponseEntity<SingleResult<VideoResDto>> addVideo(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "metaData") @RequestPart VideoReqDto metaData,
            @ApiParam(value = "영상파일") @RequestPart(value = "videoFile", required = false) MultipartFile videoFile
    ) throws IOException, InterruptedException {
        User user = userService.getUserByJwt(jwt);
        if(videoFile == null || videoFile.isEmpty())
            throw new IllegalArgumentException("File is null");
        VideoResDto dto = videoService.uploadVideo(videoFile, user, metaData);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 전체 조회")
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
}
