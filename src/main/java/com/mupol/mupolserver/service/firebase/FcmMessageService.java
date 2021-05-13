package com.mupol.mupolserver.service.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.mupol.mupolserver.domain.fcm.FcmMessage;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Service
public class FcmMessageService {

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/175394342857/messages:send";
    private final String firebaseConfigPath = "firebase/firebase-service-key.json";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googlapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    public void sendMessageTo(String targetToken, String title, String body) throws IOException {
        String message = makeMessage(targetToken, title, body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + getAccessToken());
        HttpEntity<String> request = new HttpEntity<String>(message, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
        System.out.println(response.getBody());
    }

    private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build())
                        .build())
                .validate_only(false)
                .build();

        return objectMapper.writeValueAsString(fcmMessage);
    }
}
