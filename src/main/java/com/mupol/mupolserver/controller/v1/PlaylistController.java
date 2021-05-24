package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.playlist.PlaylistReqDto;
import com.mupol.mupolserver.dto.playlist.PlaylistResDto;
import com.mupol.mupolserver.dto.playlist.PlaylistVideoDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import com.mupol.mupolserver.service.PlaylistService;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.UserService;
import com.mupol.mupolserver.service.VideoService;
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

    @ApiOperation(value = "재생 목록 생성")
    @PostMapping(value = "/new")
    public ResponseEntity<SingleResult<PlaylistResDto>> createPlaylist(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "metaData") @RequestPart PlaylistReqDto metaData) throws IOException, InterruptedException {
        User user = userService.getUserByJwt(jwt);
        System.out.println(metaData.getName());
        PlaylistResDto dto = playlistService.createPlaylist(user, metaData);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiOperation(value = "재생 목록 삭제")
    @DeleteMapping("/delete/{playlistId}")
    public ResponseEntity<SingleResult<String>> deletePlaylist(
            @PathVariable String playlistId
    ) {
        playlistService.deletePlaylist(Long.valueOf(playlistId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

    @ApiOperation(value = "재생 목록 제목 수정", notes = "")
    @PatchMapping("/update/{playlistId}")
    public ResponseEntity<SingleResult<PlaylistResDto>> updatePlaylist(
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

    @GetMapping(value = "/view/{playlistId}")
    public ResponseEntity<SingleResult<PlaylistResDto>> viewSinglePlaylist(
            @PathVariable String playlistId) throws IOException, InterruptedException {
        PlaylistResDto dto = playlistService.getSndDto(playlistService.getPlaylist(Long.valueOf(playlistId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiOperation(value = "개별 재생 목록의 동영상들 조회")
    @GetMapping(value = "/view/video/{playlistId}")
    public ResponseEntity<ListResult<VideoResDto>> viewPlaylistVideoes(
            @PathVariable String playlistId) throws IOException, InterruptedException {
        List<VideoResDto> dto = videoService.getVideoDtoList(playlistService.getPlaylistVideoes(Long.valueOf(playlistId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dto));
    }

    @ApiOperation(value = "재생 목록에 동영상 추가")
    @PostMapping(value = "/add/video/{playlistId}")
    public ResponseEntity<SingleResult<PlaylistVideoDto>> addPlaylistVideoes(
            @PathVariable String playlistId,
            @RequestBody String videoId) throws IOException, InterruptedException {
        PlaylistVideoDto dto = playlistService.addPlaylistVideo(Long.valueOf(playlistId), Long.valueOf(videoId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiOperation(value = "재생 목록에 동영상 삭제")
    @DeleteMapping(value = "/delete/video/{playlistId}")
    public ResponseEntity<SingleResult<String>> deletePlaylistVideoes(
            @PathVariable String playlistId,
            @RequestBody String videoId) throws IOException, InterruptedException {
        playlistService.deletePlaylistVideo(Long.valueOf(playlistId), Long.valueOf(videoId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

}
