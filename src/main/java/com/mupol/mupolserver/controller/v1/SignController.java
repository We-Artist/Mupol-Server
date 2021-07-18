package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.ImageUploadFailException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.auth.SigninReqDto;
import com.mupol.mupolserver.dto.auth.SignupReqDto;
import com.mupol.mupolserver.dto.user.UserQuitDto;
import com.mupol.mupolserver.service.*;
import com.mupol.mupolserver.service.firebase.FcmMessageService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Api(tags = {"2. Sign"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auth")
public class SignController {

    private final UserService userService;
    private final SignService signService;
    private final ResponseService responseService;
    private final S3Service s3Service;
    private final JwtTokenProvider jwtTokenProvider;
    private final PlaylistService playlistService;
    private final VideoService videoService;

    @ApiOperation(value = "소셜 로그인")
    @PostMapping(value = "/signin")
    public ResponseEntity<SingleResult<String>> signinByProvider(
            @ApiParam(value = "json") @RequestBody SigninReqDto signinReqDto
    ) {
        User user = userService.getUserByProviderAndToken(SnsType.valueOf(signinReqDto.getProvider()), signinReqDto.getAccessToken());
        String jwt = jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRole());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(jwt));
    }

    @ApiOperation(value = "소셜 계정 가입", notes = "성공시 jwt 토큰을 반환합니다")
    @PostMapping(value = "/signup")
    public ResponseEntity<SingleResult<String>> signupProvider(
            @ApiParam(value = "json") @RequestBody SignupReqDto dto
    ) throws IOException {
        System.out.println("");
        User newUser = signService.getUserFromDto(dto);
        userService.save(newUser);
        try {
            System.out.println("사진가져오기");
            MultipartFile profileImage = signService.getProfileImage(dto.getProvider(), dto.getAccessToken());
            if (profileImage != null) {
                String profileImageUrl = s3Service.uploadProfileImage(profileImage, newUser.getId());
                newUser.setProfileImageUrl(profileImageUrl);
                userService.save(newUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ImageUploadFailException();
        }

        //회원가입 시 기본 보관함 생성
        playlistService.createPlaylist(newUser, "default", true);

        String jwt = jwtTokenProvider.createToken(String.valueOf(newUser.getId()), newUser.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseService.getSingleResult(jwt));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "로그아웃")
    @PostMapping(value = "/signout")
    public ResponseEntity<SingleResult<String>> signOut(@RequestHeader("Authorization") String jwt) {
        User user = userService.getUserByJwt(jwt);
        userService.signout(user);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("sign out success"));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "탈퇴")
    @DeleteMapping(value = "/quit")
    public ResponseEntity<SingleResult<String>> quitByProvider(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UserQuitDto dto) throws IOException {
        User user = userService.getUserByJwt(jwt);
        videoService.deleteUserVideo(user.getId());
        userService.quitUser(user, dto.getContent().toString());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("quit user"));
    }

}
