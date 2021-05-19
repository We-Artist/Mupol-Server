package com.mupol.mupolserver.service.social;

import com.google.gson.Gson;
import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.domain.social.kakao.KakaoProfile;
import com.mupol.mupolserver.domain.user.SnsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService implements SocialService {
    private final RestTemplate restTemplate;
    private final Gson gson;

    @Value("${spring.social.kakao.url.profile}")
    private String kakaoProfileUrl;

    public KakaoProfile getKakaoProfile(String accessToken) {
        // Set header : Content-type: application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(kakaoProfileUrl, request, String.class);
            System.out.println(response.getBody());
            System.out.println(gson.fromJson(response.getBody(), KakaoProfile.class));
            if (response.getStatusCode() == HttpStatus.OK)
                return gson.fromJson(response.getBody(), KakaoProfile.class);
        } catch (Exception e) {
            System.out.println(e);
        }
        throw new CUserNotFoundException();
    }

    @Override
    public String getSnsId(String accessToken) {
        return String.valueOf(this.getKakaoProfile(accessToken).getId());
    }

    @Override
    public String getProfileImageUrl(String token) {
        return getKakaoProfile(token)
                .getKakao_account()
                .getProfile()
                .getProfile_image_url();
    }

    @Override
    public String getEmail(String token) {
        KakaoProfile kakaoProfile = getKakaoProfile(token);
        log.info(kakaoProfile.getKakao_account().getProfile().toString());
        return kakaoProfile
                .getKakao_account()
                .getEmail();
    }

    @Override
    public SnsType getSnsType() {
        return SnsType.kakao;
    }
}