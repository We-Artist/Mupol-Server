package com.mupol.mupolserver.service.social;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.domain.user.SnsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleService implements SocialService {

    @Value("${spring.social.google.id_token}")
    private String googleProfileUrl;

    public String getGoogleProfile(String accessToken){
        String templateUrl = googleProfileUrl + accessToken;
        log.info(templateUrl);

        try {
            URL url = new URL(templateUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty("Authorization", "Bearer"+accessToken);

            int responseCode = conn.getResponseCode();
            log.info(String.valueOf(responseCode));
            if (responseCode == 200){

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = "";
                String result = "";

                while ((line = br.readLine()) != null) {
                    result += line;
                }

                log.info("result: "+ result);

                return result;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new CUserNotFoundException();
    }

    @Override
    public String getSnsId(String token) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(getGoogleProfile(token));
        return element.getAsJsonObject().get("id").getAsString();
    }

    @Override
    public String getProfileImageUrl(String token) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(getGoogleProfile(token));
        return element.getAsJsonObject().get("picture").getAsString();
    }

    @Override
    public String getEmail(String token) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(getGoogleProfile(token));
        return element.getAsJsonObject().get("email").getAsString();
    }

    @Override
    public SnsType getSnsType() {
        return SnsType.google;
    }
}
