package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.dto.playlist.PlaylistMoveVideoDto;
import com.mupol.mupolserver.dto.playlist.PlaylistResDto;
import com.mupol.mupolserver.dto.playlist.PlaylistVideoAddDto;
import com.mupol.mupolserver.dto.playlist.PlaylistVideoDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import com.mupol.mupolserver.service.*;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@Api(tags = {"10. playlist"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/playlist")
public class PlaylistController {

    private final UserService userService;
    private final PlaylistService playlistService;
    private final ResponseService responseService;
    private final VideoService videoService;
    private final LikeService likeService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록 생성")
    @PostMapping(value = "/new")
    public ResponseEntity<SingleResult<PlaylistResDto>> createPlaylist(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "playlist name") @RequestParam String name
    ) {
        User user = userService.getUserByJwt(jwt);
        PlaylistResDto dto = playlistService.createPlaylist(user, name, false);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록 삭제")
    @DeleteMapping("/delete/{playlistId}")
    public ResponseEntity<SingleResult<String>> deletePlaylist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String playlistId
    ) {
        playlistService.deletePlaylist(Long.valueOf(playlistId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록 제목 수정", notes = "")
    @PatchMapping("/update/{playlistId}")
    public ResponseEntity<SingleResult<PlaylistResDto>> updatePlaylist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String playlistId,
            @RequestBody String name
    ) {
        PlaylistResDto dto = playlistService.getSndDto(playlistService.updateName(Long.valueOf(playlistId), name));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자의 모든 재생 목록 조회")
    @GetMapping(value = "/view")
    public ResponseEntity<ListResult<PlaylistResDto>> viewPlaylist(
            @RequestHeader("Authorization") String jwt) throws IOException, InterruptedException {
        User user = userService.getUserByJwt(jwt);
        List<PlaylistResDto> dto = playlistService.getSndDtoList(playlistService.getPlaylists(user.getId()));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "좋아요 누른 영상 목록 조회")
    @GetMapping(value = "/view/like")
    public ResponseEntity<ListResult<VideoResDto>> viewLikedPlaylist(
            @RequestHeader("Authorization") String jwt
    ) {
        User user = userService.getUserByJwt(jwt);
        List<Video> videoList = likeService.getLikedVideos(user);
        List<VideoResDto> dto = videoService.getVideoWithCommentDtoList(user, videoList);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "개별 재생 목록 조회")
    @GetMapping(value = "/view/{playlistId}")
    public ResponseEntity<SingleResult<PlaylistResDto>> viewSinglePlaylist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String playlistId
    ) {
        PlaylistResDto dto = playlistService.getSndDto(playlistService.getPlaylist(Long.valueOf(playlistId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "개별 재생 목록의 동영상들 조회")
    @GetMapping(value = "/view/video/{playlistId}")
    public ResponseEntity<ListResult<VideoResDto>> viewPlaylistVideoes(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String playlistId
    ) {
        User user = userService.getUserByJwt(jwt);
        List<Video> videoList = playlistService.getPlaylistVideoes(Long.valueOf(playlistId));
        List<VideoResDto> dto = videoService.getVideoWithCommentDtoList(user, videoList);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록에 동영상 추가")
    @PostMapping(value = "/add/video/{playlistId}")
    public ResponseEntity<SingleResult<PlaylistVideoDto>> addPlaylistVideoes(
            @RequestHeader("Authorization") String jwt,
            @RequestBody PlaylistVideoAddDto dto
    ) throws IOException, InterruptedException {
        PlaylistVideoDto playlistVideoDto = playlistService.addPlaylistVideo(dto.getPlaylistId(), dto.getVideoId());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(playlistVideoDto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록에 동영상 삭제")
    @DeleteMapping(value = "/delete/video/{playlistId}/{videoId}")
    public ResponseEntity<SingleResult<String>> deletePlaylistVideoes(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String playlistId,
            @PathVariable String videoId) throws IOException, InterruptedException {
        playlistService.deletePlaylistVideo(Long.valueOf(playlistId), Long.valueOf(videoId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록 간 동영상 이동")
    @PostMapping(value = "/move/video")
    public ResponseEntity<SingleResult<PlaylistVideoDto>> moveVideoToAnotherPlaylist(
            @RequestHeader("Authorization") String jwt,
            @RequestBody PlaylistMoveVideoDto dto
    ) {
        playlistService.deletePlaylistVideo(dto.getCurrentPlaylistId(), dto.getVideoId());
        PlaylistVideoDto playlistVideoDto = playlistService.addPlaylistVideo(dto.getTargetPlaylistId(), dto.getVideoId());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(playlistVideoDto));
    }

}
