package com.mupol.mupolserver.service.social;

import com.google.gson.Gson;
import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.domain.social.kakao.KakaoProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

@Slf4j
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
            System.out.println(gson.fromJson(response.getBody(), KakaoProfile.class));
            if (response.getStatusCode() == HttpStatus.OK)
                return gson.fromJson(response.getBody(), KakaoProfile.class);
        } catch (Exception e) {
            System.out.println(e);
        }
        throw new CUserNotFoundException();
    }

    public String getSnsId(String accessToken) {
        return String.valueOf(this.getKakaoProfile(accessToken).getId());
    }

    public MultipartFile getProfileImage(String accessToken) throws IOException {
        String imageUrl = getKakaoProfile(accessToken)
                .getKakao_account()
                .getProfile()
                .getProfile_image_url();

        // 카카오톡 프로필 이미지가 존재하지 않을 경우
        if(imageUrl == null) {
            return null;
        }

        BufferedImage img = ImageIO.read(new URL(imageUrl));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return new MockMultipartFile("profile_image","profile_image.jpg" ,"image/jpg", baos.toByteArray());
    }
}