package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.social.kakao.KakaoProfile;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.service.social.KakaoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Api(tags = {"2. Sign"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auth")
public class SignController {

    private final UserRepository userRepository;
    private final KakaoService kakaoService;
    private final JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "소셜 로그인")
    @PostMapping(value = "/signin/{provider}")
    public String signinByProvider(
            @ApiParam(value = "서비스 제공자 provider", required = true, defaultValue = "kakao") @PathVariable String provider,
            @ApiParam(value = "소셜 access_token", required = true) @RequestParam String accessToken
    ) {

        String snsId = "";

        // TODO: sns 별로 snsId 가져오기
        if(provider.equals(SnsType.kakao.getType())) {
            KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);
            snsId = String.valueOf(profile.getId());
        }else if(provider.equals(SnsType.apple.getType())) {
            return "not yet";
        }else if(provider.equals(SnsType.google.getType())) {
            return "not yet";
        }else if(provider.equals(SnsType.facebook.getType())) {
            return "not yet";
        }

        User user = userRepository.findBySnsIdAndProvider(snsId, SnsType.valueOf(provider)).orElseThrow(() -> new IllegalArgumentException("no such user"));
        return jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRole());
    }

    @ApiOperation(value = "소셜 계정 가입")
    @PostMapping(value = "/signup/{provider}")
    public String signupProvider(
            @ApiParam(value = "서비스 제공자", required = true, defaultValue = "kakao") @PathVariable String provider,
            @ApiParam(value = "소셜 access_token", required = true) @RequestParam String accessToken,
            @ApiParam(value = "닉네임", required = true) @RequestParam String name,
            @ApiParam(value = "관심악기", required = false) @RequestParam List<String> instruments,
            @ApiParam(value = "약관동의여부", required = true) @RequestParam boolean isAgreed,
            @ApiParam(value = "취미여부", required = true) @RequestParam boolean isMajor,
            @ApiParam(value = "생년월일", required = true) @RequestParam  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birth
            ) {

        String snsId = "";

        // TODO: sns 별로 profile 가져오기
        if(provider.equals(SnsType.kakao.getType())) {
            KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);
            snsId = String.valueOf(profile.getId());
        }else if(provider.equals(SnsType.apple.getType())) {
            return "not yet";
        }else if(provider.equals(SnsType.google.getType())) {
            return "not yet";
        }else if(provider.equals(SnsType.facebook.getType())) {
            return "not yet";
        }

        Optional<User> user = userRepository.findBySnsIdAndProvider(snsId, SnsType.valueOf(provider));

        if (user.isPresent()) {
            return "[ERROR] existing user";
//            throw new IllegalArgumentException("existing user");
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

        return jwtTokenProvider.createToken(String.valueOf(newUser.getId()), newUser.getRole());
    }
}
