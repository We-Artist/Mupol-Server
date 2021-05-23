package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.playlist.PlaylistReqDto;
import com.mupol.mupolserver.dto.playlist.PlaylistResDto;
import com.mupol.mupolserver.service.PlaylistService;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.UserService;
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

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록 생성")
    @PostMapping(value = "/new")
    public ResponseEntity<SingleResult<PlaylistResDto>> createPlaylist(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "metaData") @RequestPart PlaylistReqDto metaData) throws IOException, InterruptedException {
        User user = userService.getUserByJwt(jwt);
        PlaylistResDto dto = playlistService.createPlaylist(user, metaData);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록 삭제")
    @DeleteMapping("/delete")
    public ResponseEntity<SingleResult<String>> deletePlaylist(
            @PathVariable String playlistId
    ) {
        playlistService.deletePlaylist(Long.valueOf(playlistId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "재생 목록 제목 수정", notes = "")
    @PutMapping("/update")
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
    @ApiOperation(value = "재생 목록 조회")
    @PostMapping(value = "/view")
    public ResponseEntity<ListResult<PlaylistResDto>> createPlaylist(
            @RequestHeader("Authorization") String jwt) throws IOException, InterruptedException {
        User user = userService.getUserByJwt(jwt);
        List<PlaylistResDto> dto = playlistService.getSndDtoList(playlistService.getPlaylists(user.getId()));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dto));
    }




}
