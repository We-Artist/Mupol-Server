package com.mupol.mupolserver.service;

import com.mupol.mupolserver.advice.exception.SnsNotSupportedException;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.service.social.FacebookService;
import com.mupol.mupolserver.service.social.GoogleService;
import com.mupol.mupolserver.service.social.KakaoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Service
public class SignService {

    private final KakaoService kakaoService;
    private final FacebookService facebookService;
    private final GoogleService googleService;

    public String getSnsId(String provider, String accessToken) {
        String snsId;
        if (provider.equals(SnsType.kakao.getType())) {
            snsId = kakaoService.getSnsId(accessToken);
        } else if (provider.equals(SnsType.facebook.getType())) {
            snsId = facebookService.getSnsId(accessToken);
        }  else if (provider.equals(SnsType.google.getType())) {
            snsId = googleService.getSnsId(accessToken);
        } else if (provider.equals(SnsType.test.getType())) {
            snsId = accessToken;
        } else if (provider.equals(SnsType.apple.getType())) {
            throw new SnsNotSupportedException();
        }else {
            throw new SnsNotSupportedException();
        }
        return snsId;
    }

    // TODO: sns 별로 profile 가져오기
    public MultipartFile getProfileImage(String provider, String accessToken) {
        MultipartFile profileImageFile = null;
        try {
            if (provider.equals(SnsType.kakao.getType())) {
                profileImageFile = kakaoService.getProfileImage(accessToken);
            } else if (provider.equals(SnsType.facebook.getType())) {
                return null;
            } else if (provider.equals(SnsType.apple.getType())) {
                return null;
            } else if (provider.equals(SnsType.google.getType())) {
                profileImageFile = googleService.getProfileImage(accessToken);
            } else if (provider.equals(SnsType.test.getType())) {
                return null;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return profileImageFile;
    }
}
