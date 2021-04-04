package com.mupol.mupolserver.service.social;

import com.google.gson.Gson;
import com.mupol.mupolserver.domain.social.kakao.KakaoProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class KakaoService {
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
            if (response.getStatusCode() == HttpStatus.OK)
                return gson.fromJson(response.getBody(), KakaoProfile.class);
        } catch (Exception e) {
            System.out.println(e);
        }
        throw new IllegalArgumentException("no such user");
    }
}
