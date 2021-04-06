package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.advice.exception.SnsNotSupportedException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.response.CommonResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.social.kakao.KakaoProfile;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.social.KakaoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Api(tags = {"2. Sign"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auth")
public class SignController {

    private final UserRepository userRepository;
    private final KakaoService kakaoService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;

    @ApiOperation(value = "소셜 로그인")
    @PostMapping(value = "/signin/{provider}")
    public ResponseEntity<SingleResult<String>> signinByProvider(
            @ApiParam(value = "서비스 제공자 provider", required = true, defaultValue = "kakao") @PathVariable String provider,
            @ApiParam(value = "소셜 access_token", required = true) @RequestParam String accessToken
    ) {

        String snsId = "";

        // TODO: sns 별로 snsId 가져오기
        if (provider.equals(SnsType.kakao.getType())) {
            KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);
            snsId = String.valueOf(profile.getId());
        } else if (provider.equals(SnsType.apple.getType())) {
            throw new SnsNotSupportedException();
        } else if (provider.equals(SnsType.google.getType())) {
            throw new SnsNotSupportedException();
        } else if (provider.equals(SnsType.facebook.getType())) {
            throw new SnsNotSupportedException();
        } else if (provider.equals(SnsType.test.getType())) {
            // test 용 sns type
            snsId = accessToken;
        } else {
            throw new SnsNotSupportedException();
        }

        User user = userRepository.findBySnsIdAndProvider(snsId, SnsType.valueOf(provider)).orElseThrow(CUserNotFoundException::new);
        String jwt = jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRole());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(jwt));
    }

    @ApiOperation(value = "소셜 계정 가입", notes = "성공시 jwt 토큰을 반환합니다")
    @PostMapping(value = "/signup/{provider}")
    public ResponseEntity<SingleResult<String>> signupProvider(
            @ApiParam(value = "kakao/google/apple/facebook", required = true, defaultValue = "kakao") @PathVariable String provider,
            @ApiParam(value = "소셜 access_token", required = true) @RequestParam String accessToken,
            @ApiParam(value = "닉네임", required = true) @RequestParam String name,
            @ApiParam(value = "관심악기") @RequestParam(required = false) List<String> instruments,
            @ApiParam(value = "약관동의여부", required = true) @RequestParam boolean isAgreed,
            @ApiParam(value = "취미여부", required = true) @RequestParam boolean isMajor,
            @ApiParam(value = "생년월일(yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birth
    ) {

        String snsId = "";

        // TODO: sns 별로 profile 가져오기
        if (provider.equals(SnsType.kakao.getType())) {
            KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);
            snsId = String.valueOf(profile.getId());
        } else if (provider.equals(SnsType.apple.getType())) {
            throw new SnsNotSupportedException();
        } else if (provider.equals(SnsType.google.getType())) {
            throw new SnsNotSupportedException();
        } else if (provider.equals(SnsType.facebook.getType())) {
            throw new SnsNotSupportedException();
        } else {
            throw new SnsNotSupportedException();
        }

        Optional<User> user = userRepository.findBySnsIdAndProvider(snsId, SnsType.valueOf(provider));

        if (user.isPresent()) {
            throw new CUserNotFoundException();
        }

        User newUser = User.builder()
                .snsId(snsId)
                .provider(SnsType.valueOf(provider))
                .username(name)
                .isMajor(isMajor)
                .isAgreed(isAgreed)
                .birth(birth)
                .role(User.Role.USER)
                .build();

        userRepository.save(newUser);

        String jwt = jwtTokenProvider.createToken(String.valueOf(newUser.getId()), newUser.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseService.getSingleResult(jwt));
    }
}
