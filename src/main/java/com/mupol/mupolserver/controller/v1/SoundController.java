package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.sound.Sound;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.sound.SoundOptionDto;
import com.mupol.mupolserver.dto.sound.SoundReqDto;
import com.mupol.mupolserver.dto.sound.SoundResDto;
import com.mupol.mupolserver.service.MonthlyGoalService;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.SoundService;
import com.mupol.mupolserver.service.UserService;
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
@Api(tags = {"4. Sound"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/sound")
public class SoundController {

    private final UserService userService;
    private final SoundService soundService;
    private final MonthlyGoalService monthlyGoalService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "녹음본 업로드", notes = "")
    @PostMapping(value = "/new",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<SingleResult<SoundResDto>> addSound(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "bpm") @RequestParam Integer bpm,
            @ApiParam(value = "title") @RequestParam String title,
            @ApiParam(value = "음성파일") @RequestParam(value = "soundFile") MultipartFile soundFile
    ) throws IOException, InterruptedException {
        User user = userService.getUserByJwt(jwt);
        if (soundFile == null || soundFile.isEmpty())
            throw new IllegalArgumentException("File is null");
        SoundResDto dto = soundService.uploadSound(soundFile, user, title, bpm);
        if(monthlyGoalService.isGoalExist(user, MonthExtractor.getCurrentMonthFirstDate())) {
            monthlyGoalService.update(user);
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "최근 녹음 설정 가져오기", notes = "최대 3개")
    @GetMapping(value = "/recent-options")
    public ResponseEntity<ListResult<SoundOptionDto>> getCurrentOptions(
            @RequestHeader(value = "Authorization") String jwt
    ) {
        User user = userService.getUserByJwt(jwt);
        List<SoundOptionDto> dto = soundService.getCurrentOptions(user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "녹음본 전체 조회", notes = "")
    @GetMapping("/me/all")
    public ResponseEntity<ListResult<SoundResDto>> getSoundList(
            @RequestHeader(value = "Authorization") String jwt
    ) {
        User user = userService.getUserByJwt(jwt);
        List<SoundResDto> dtoList = soundService.getSndDtoList(soundService.getSounds(user.getId()));
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
        User user = userService.getUserByJwt(jwt);
        soundService.deleteSound(user.getId(), Long.valueOf(soundId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }
}
