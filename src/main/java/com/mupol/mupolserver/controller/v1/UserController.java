package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.user.ProfileUpdateReqDto;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.S3Service;
import com.mupol.mupolserver.service.UserService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Api(tags = {"1. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ResponseService responseService;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 리스트 조회", notes = "모든 회원을 조회한다")
    @GetMapping("/")
    public ResponseEntity<ListResult<User>> findAllUser() {
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(userRepository.findAll()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
    @GetMapping("/{userId}")
    public ResponseEntity<SingleResult<User>> findUserById(@ApiParam(value = "회원 ID", required = true) @PathVariable long userId) {
        User user = userRepository.findById(userId).orElseThrow(CUserNotFoundException::new);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "본인 계정 조회")
    @GetMapping("/me")
    public ResponseEntity<SingleResult<User>> getMyProfile(@RequestHeader("Authorization") String jwt) {
        User user = userRepository.findById(Long.valueOf(jwtTokenProvider.getUserPk(jwt))).orElseThrow(CUserNotFoundException::new);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "본인 계정 삭제")
    @DeleteMapping("/me")
    public ResponseEntity<SingleResult<String>> deleteAccount(@RequestHeader("Authorization") String jwt) {
        try {
            userRepository.deleteById(Long.valueOf(jwtTokenProvider.getUserPk(jwt)));
        } catch (Exception e) {
            throw new CUserNotFoundException();
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "프로필 이미지 변경", notes = "profileImage를 null로 처리하면 user의 profileImage는 삭제됩니다.")
    @PostMapping("/me/profile-image")
    public ResponseEntity<SingleResult<User>> updateProfileImage(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "이미지 파일") @RequestParam(required = false) MultipartFile profileImage
    ) throws IOException {

        long userId = Long.parseLong(jwtTokenProvider.getUserPk(jwt));
        User user = userRepository.findById(userId).orElseThrow(CUserNotFoundException::new);

        if (profileImage == null || profileImage.isEmpty()) {
            user.setProfileImageUrl(null);
        } else {
            String profileImageUrl = s3Service.uploadProfileImage(profileImage, userId);
            user.setProfileImageUrl(profileImageUrl);
        }
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "프로필 데이터 수정")
    @PostMapping("/me")
    public ResponseEntity<SingleResult<User>> updateProfile(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "프로필 정보") @RequestBody ProfileUpdateReqDto dto
    ) {

        long userId = Long.parseLong(jwtTokenProvider.getUserPk(jwt));
        User user = userRepository.findById(userId).orElseThrow(CUserNotFoundException::new);

        List<String> instruments = dto.getInstruments();
        List<Instrument> instrumentList = new ArrayList<>();

        if (instruments != null) {
            try {
                for (String inst : instruments) instrumentList.add(Instrument.valueOf(inst));
            } catch (Exception e) {
                throw new InstrumentNotExistException();
            }
        }

        if(!userService.validateUsername(dto.getUsername()))
            throw new IllegalArgumentException("올바르지 않은 이름입니다.");

        user.setBio(dto.getBio());
        user.setUsername(dto.getUsername());
        user.setFavoriteInstrument(instrumentList);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(user));
    }

    @ApiOperation(value = "프로필 데이터 수정")
    @PostMapping("/validate-name")
    public ResponseEntity<SingleResult<Boolean>> updateProfile(
            @ApiParam(value = "username") @RequestBody String username
    ) {
        if(!userService.validateUsername(username)) throw new IllegalArgumentException("올바르지 않은 이름입니다.");
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(true));
    }
}
