package com.mupol.mupolserver.controller.v1;

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




}
