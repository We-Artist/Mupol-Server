package com.mupol.mupolserver.service.social;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.internal.filter.ValueNode;
import com.mupol.mupolserver.domain.social.facebook.FacebookProfile;
import com.mupol.mupolserver.domain.social.google.GoogleProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleService {
    private final RestTemplate restTemplate;

    @Value("${spring.social.google.id_token}")
    private String googleProfileUrl;

    public String getSnsId(String accessToken) {
        String templateUrl = googleProfileUrl + accessToken;
        String snsId = "";

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

                    JsonParser parser = new JsonParser();
                    log.info("result: "+ result);
                    JsonElement element = parser.parse(result);

                    snsId = element.getAsJsonObject().get("id").getAsString();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return snsId;
    }
}
