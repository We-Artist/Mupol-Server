package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.*;
import com.mupol.mupolserver.advice.exception.sign.UserDoesNotAgreeException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.auth.SigninReqDto;
import com.mupol.mupolserver.dto.auth.SignupReqDto;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.S3Service;
import com.mupol.mupolserver.service.SignService;
import com.mupol.mupolserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ApiOperation(value = "소셜 로그인")
    @PostMapping(value = "/signin")
    public ResponseEntity<SingleResult<String>> signinByProvider(
            @ApiParam(value = "json") @RequestBody SigninReqDto signinReqDto
    ) {
        User user = userService.getUserByProviderAndToken(signinReqDto.getProvider(), signinReqDto.getAccessToken());
        String jwt = jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRole());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(jwt));
    }

    @ApiOperation(value = "소셜 계정 가입", notes = "성공시 jwt 토큰을 반환합니다")
    @PostMapping(value = "/signup")
    public ResponseEntity<SingleResult<String>> signupProvider(
            @ApiParam(value = "json") @RequestBody SignupReqDto signupReqDto
    ) {
        String provider = signupReqDto.getProvider();
        String accessToken = signupReqDto.getAccessToken();
        String name = signupReqDto.getName();
        boolean terms = signupReqDto.isTerms();
        boolean isMajor = signupReqDto.isMajor();
        List<String> instruments = signupReqDto.getInstruments();
        LocalDate birth = signupReqDto.getBirth();
        String snsId = signService.getSnsId(provider, accessToken);
        SnsType snsType = SnsType.valueOf(provider);
        List<Instrument> instrumentList = new ArrayList<>();

        if (!terms) throw new UserDoesNotAgreeException();
        if (userService.isUserExist(provider, accessToken)) throw new CUserIdDuplicatedException();
        if (!userService.validateUsername(name)) throw new IllegalArgumentException("올바르지 않은 이름입니다.");

        // 악기 구분
        if (instruments != null) {
            try {
                for (String inst : instruments) instrumentList.add(Instrument.valueOf(inst));
            } catch (Exception e) {
                throw new InstrumentNotExistException();
            }
        }

        User newUser = User.builder()
                .snsId(snsId)
                .provider(snsType)
                .username(name)
                .favoriteInstrument(instrumentList)
                .isMajor(isMajor)
                .terms(true)
                .birth(birth)
                .role(User.Role.USER)
                .build();
        userService.save(newUser);

        try {
            MultipartFile profileImage = signService.getProfileImage(provider, accessToken);
            if (profileImage != null) {
                String profileImageUrl = s3Service.uploadProfileImage(profileImage, newUser.getId());
                newUser.setProfileImageUrl(profileImageUrl);
                userService.save(newUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ImageUploadFailException();
        }

        String jwt = jwtTokenProvider.createToken(String.valueOf(newUser.getId()), newUser.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseService.getSingleResult(jwt));
    }
}
