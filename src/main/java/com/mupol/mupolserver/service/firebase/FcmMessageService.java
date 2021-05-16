package com.mupol.mupolserver.service.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.mupol.mupolserver.domain.fcm.FcmMessage;
import com.mupol.mupolserver.domain.notification.TargetType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor
@Service
public class FcmMessageService {

    @Value("${fcm.apiUrl}")
    private String API_URL;
    private String firebaseConfigPath = "firebase/firebase-service-key.json";
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googlapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    public void sendMessageTo(String targetToken, String title, String body, TargetType targetType, Long targetId) throws IOException {
        String message = makeMessage(targetToken, title, body, targetType, targetId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + getAccessToken());
        HttpEntity<String> request = new HttpEntity<String>(message, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
        System.out.println(response.getBody());
    }

    private String makeMessage(String targetToken, String title, String body, TargetType targetType, Long targetId) throws JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title).body(body).image(null).build())
                        .data(FcmMessage.Data.builder()
                                .target(targetType.getType()).targetId(targetId).build())
                        .build())
                .validate_only(false)
                .build();

        return objectMapper.writeValueAsString(fcmMessage);
    }
}
