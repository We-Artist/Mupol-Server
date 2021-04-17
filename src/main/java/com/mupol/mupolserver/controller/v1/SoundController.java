package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.sound.Sound;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.sound.SoundReqDto;
import com.mupol.mupolserver.dto.sound.SoundResDto;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.SoundService;
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
@Api(tags = {"4. Sound"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/sound")
public class SoundController {

    private final UserRepository userRepository;
    private final SoundService soundService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "녹음본 업로드", notes = "")
    @PostMapping(value = "/new", consumes = {"multipart/form-data"})
    public ResponseEntity<SingleResult<SoundResDto>> addSound(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "metaData") @RequestPart SoundReqDto metaData,
            @ApiParam(value = "음성파일") @RequestPart(value = "soundFile", required = false) MultipartFile soundFile
    ) throws IOException, InterruptedException {
        User user = userRepository.findById(Long.valueOf(jwtTokenProvider.getUserPk(jwt))).orElseThrow(CUserNotFoundException::new);
        if (soundFile == null || soundFile.isEmpty())
            throw new IllegalArgumentException("File is null");
        SoundResDto dto = soundService.uploadSound(soundFile, user, metaData);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "녹음본 전체 조회", notes = "")
    @GetMapping("/me/all")
    public ResponseEntity<ListResult<SoundResDto>> getSoundList(
            @RequestHeader(value = "Authorization") String jwt
    ) {
        Long userId = Long.valueOf(jwtTokenProvider.getUserPk(jwt));
        List<SoundResDto> dtoList = soundService.getSndDtoList(soundService.getSounds(userId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "녹음본 개별 조회", notes = "")
    @GetMapping("/me/{soundId}")
    public ResponseEntity<SingleResult<SoundResDto>> getSound(
            @PathVariable String soundId
    ) {
        SoundResDto dto = soundService.getSndDto(soundService.getSound(Long.valueOf(soundId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "녹음본 제목 수정", notes = "")
    @PutMapping("/me/{soundId}")
    public ResponseEntity<SingleResult<SoundResDto>> updateSoundTitle(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String soundId,
            @RequestBody String title
    ) {
        if (title == null || title.equals(""))
            throw new IllegalArgumentException("title is empty");
        Sound sound = soundService.updateTitle(Long.valueOf(soundId), title);
        SoundResDto dto = soundService.getSndDto(sound);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "녹음본 삭제", notes = "")
    @DeleteMapping("/me/{soundId}")
    public ResponseEntity<SingleResult<String>> deleteSound(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String soundId
    ) {
        Long userId = Long.valueOf(jwtTokenProvider.getUserPk(jwt));
        soundService.deleteSound(userId, Long.valueOf(soundId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }
}
