package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.*;
import com.mupol.mupolserver.advice.exception.sign.UserDoesNotAgreeException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.auth.SigninReqDto;
import com.mupol.mupolserver.dto.auth.SignupReqDto;
import com.mupol.mupolserver.service.ResponseService;
import com.mupol.mupolserver.service.S3Service;
import com.mupol.mupolserver.service.social.FacebookService;
import com.mupol.mupolserver.service.social.KakaoService;
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
import java.util.Optional;

@Slf4j
@Api(tags = {"2. Sign"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auth")
public class SignController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;
    private final S3Service s3Service;
    private final KakaoService kakaoService;
    private final FacebookService facebookService;

    @ApiOperation(value = "소셜 로그인")
    @PostMapping(value = "/signin")
    public ResponseEntity<SingleResult<String>> signinByProvider(
            @ApiParam(value = "json") @RequestBody SigninReqDto signinReqDto
    ) {
        String provider = signinReqDto.getProvider();
        String accessToken = signinReqDto.getAccessToken();

        String snsId = getSnsId(provider, accessToken);
        SnsType snsType = SnsType.valueOf(provider);
        User user = userRepository.findBySnsIdAndProvider(snsId, snsType).orElseThrow(CUserNotFoundException::new);
        String jwt = jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRole());
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(jwt));
    }

    // TODO 이미지 가져오기
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


        if (!terms) throw new UserDoesNotAgreeException();

        String snsId = getSnsId(provider, accessToken);
        SnsType snsType = SnsType.valueOf(provider);
        List<Instrument> instrumentList = new ArrayList<>();

        Optional<User> user = userRepository.findBySnsIdAndProvider(snsId, snsType);

        // ID 중복확인
        if (user.isPresent()) throw new CUserIdDuplicatedException();

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
        userRepository.save(newUser);

        try {
            MultipartFile profileImage = getProfileImage(provider, accessToken);
            if (profileImage != null) {
                String profileImageUrl = s3Service.uploadProfileImage(profileImage, newUser.getId());
                newUser.setProfileImageUrl(profileImageUrl);
                userRepository.save(newUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ImageUploadFailException();
        }

        String jwt = jwtTokenProvider.createToken(String.valueOf(newUser.getId()), newUser.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseService.getSingleResult(jwt));
    }

    // TODO: sns 별로 profile 가져오기
    private String getSnsId(String provider, String accessToken) {
        String snsId;
        if (provider.equals(SnsType.kakao.getType())) {
            snsId = kakaoService.getSnsId(accessToken);
        } else if (provider.equals(SnsType.facebook.getType())) {
            snsId = facebookService.getSnsId(accessToken);
        } else if (provider.equals(SnsType.apple.getType())) {
            throw new SnsNotSupportedException();
        } else if (provider.equals(SnsType.google.getType())) {
            throw new SnsNotSupportedException();
        } else if (provider.equals(SnsType.test.getType())) {
            snsId = accessToken;
        } else {
            throw new SnsNotSupportedException();
        }
        return snsId;
    }

    private MultipartFile getProfileImage(String provider, String accessToken) {
        MultipartFile profileImageFile = null;
        try {
            if (provider.equals(SnsType.kakao.getType())) {
                profileImageFile = kakaoService.getProfileImage(accessToken);
            } else if (provider.equals(SnsType.facebook.getType())) {
                throw new SnsNotSupportedException();
            } else if (provider.equals(SnsType.apple.getType())) {
                throw new SnsNotSupportedException();
            } else if (provider.equals(SnsType.google.getType())) {
                throw new SnsNotSupportedException();
            } else if (provider.equals(SnsType.test.getType())) {
                return null;
            } else {
                throw new SnsNotSupportedException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return profileImageFile;
    }
}
