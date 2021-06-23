package com.mupol.mupolserver.service;

import com.mupol.mupolserver.advice.exception.CUserIdDuplicatedException;
import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.advice.exception.sign.UserDoesNotAgreeException;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.auth.SignupReqDto;
import com.mupol.mupolserver.service.firebase.FcmMessageService;
import com.mupol.mupolserver.service.social.*;
import com.mupol.mupolserver.util.ImageExtractor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class SignService {

    private final UserService userService;
    private final SocialServiceFactory socialServiceFactory;

    public User getUserFromDto(SignupReqDto dto) throws IOException {
        String accessToken = dto.getAccessToken();
        String name = dto.getName();
        boolean terms = dto.isTerms();
        boolean isMajor = dto.isMajor();
        List<String> instruments = dto.getInstruments();
        LocalDate birth = dto.getBirth();
        SnsType snsType = SnsType.valueOf(dto.getProvider());
        List<Instrument> instrumentList = new ArrayList<>();

        if (!terms) throw new UserDoesNotAgreeException();
        if (userService.isUserExist(snsType, accessToken)) throw new CUserIdDuplicatedException();
        if (!userService.validateUsername(name)) throw new IllegalArgumentException("올바르지 않은 이름입니다.");

        // 악기 구분
        if (instruments != null) {
            try {
                for (String inst : instruments) instrumentList.add(Instrument.valueOf(inst));
            } catch (Exception e) {
                throw new InstrumentNotExistException();
            }
        }

        return(User.builder()
                .snsId(userService.getSnsId(snsType, accessToken))
                .provider(snsType)
                .username(name)
                .favoriteInstrument(instrumentList)
                .isMajor(isMajor)
                .terms(true)
                .birth(birth)
                .email(userService.getEmailFromSocialProfile(snsType, accessToken))
                .fcmToken(dto.getFcmToken())
                .isNotificationAllowed(true)
                .role(User.Role.USER)
                .build());
    }

    public MultipartFile getProfileImage(String provider, String accessToken) throws IOException {
        String url = socialServiceFactory.getService(SnsType.valueOf(provider)).getProfileImageUrl(accessToken);
        if(url == null)
            return null;
        return ImageExtractor.getImageFile(url);
    }
}
