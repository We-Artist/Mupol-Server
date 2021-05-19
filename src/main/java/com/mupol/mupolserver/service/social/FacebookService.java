package com.mupol.mupolserver.service.social;

import com.mupol.mupolserver.domain.social.facebook.FacebookProfile;
import com.mupol.mupolserver.domain.user.SnsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class FacebookService implements SocialService {
    private final RestTemplate restTemplate;

    @Value("${spring.social.facebook.url}")
    private String facebookProfileUrl;

    public FacebookProfile getFacebookProfile(String token) {
        return restTemplate.getForEntity(facebookProfileUrl + token, FacebookProfile.class).getBody();
    }

    @Override
    public String getSnsId(String token) {
        return Objects.requireNonNull(getFacebookProfile(token)).getId();
    }

    @Override
    public String getProfileImageUrl(String token) {
        return Objects.requireNonNull(getFacebookProfile(token)).getPicture();
    }

    @Override
    public String getEmail(String token) {
        return Objects.requireNonNull(getFacebookProfile(token)).getEmail();
    }

    @Override
    public SnsType getSnsType() {
        return SnsType.facebook;
    }
}
