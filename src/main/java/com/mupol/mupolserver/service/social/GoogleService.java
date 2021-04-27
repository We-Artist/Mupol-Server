package com.mupol.mupolserver.service.social;

import com.google.gson.Gson;
import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.domain.social.google.GoogleProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleService {
    private final RestTemplate restTemplate;

    @Value("${spring.social.google.id_token}")
    private String googleProfileUrl;

    public GoogleProfile getGoogleProfile(String accessToken){
        String templateUrl = googleProfileUrl + accessToken;

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

                Gson gson = new Gson();
                System.out.println(gson.fromJson(line, GoogleProfile.class).toString());
                return gson.fromJson(line, GoogleProfile.class);

               /* JsonParser parser = new JsonParser();
                log.info("result: "+ result);
                JsonElement element = parser.parse(result);
                System.out.println(element.getAsJsonObject().get("id").getAsString());

                googleProfile.setId(element.getAsJsonObject().get("id").getAsString());
                googleProfile.setEmail(element.getAsJsonObject().get("email").getAsString());
                googleProfile.setVerified_email(element.getAsJsonObject().get("verified_email").getAsBoolean());
                googleProfile.setPicture(element.getAsJsonObject().get("picture").getAsString());*/

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new CUserNotFoundException();
    }

    public String getSnsId(String accessToken) {
        return getGoogleProfile(accessToken).getEmail();
    }

    public MultipartFile getProfileImage(String accessToken){
        String imageUrl = getGoogleProfile(accessToken)
                .getPicture();

        //구글 프로필 이미지가 존재하지 않을 경우
        if(imageUrl == null) {
            return null;
        }

        BufferedImage img = null;
        try {
            img = ImageIO.read(new URL(imageUrl));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            return new MockMultipartFile("profile_image","profile_image.jpg" ,"image/jpg", baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
