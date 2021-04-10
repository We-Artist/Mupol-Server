package com.mupol.mupolserver.service.social;

import com.mupol.mupolserver.domain.social.facebook.FacebookProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class FacebookService {
    private final RestTemplate restTemplate;

    @Value("${spring.social.facebook.url}")
    private String facebookProfileUrl;

    public String getSnsId(String accessToken) {
        String templateUrl = facebookProfileUrl + accessToken;
        String snsId = "";
        try {
            ResponseEntity<FacebookProfile> response = restTemplate.getForEntity(templateUrl, FacebookProfile.class);
            log.info(Objects.requireNonNull(response.getBody()).getId());
            log.info(response.getBody().getEmail());
            snsId = Objects.requireNonNull(response.getBody()).getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return snsId;
    }
}
