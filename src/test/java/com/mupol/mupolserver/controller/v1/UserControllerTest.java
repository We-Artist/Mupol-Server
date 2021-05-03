package com.mupol.mupolserver.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testUserSnsId = "hvjakeb3423";
    private final SnsType testUserSnsType = SnsType.test;
    private final String baseUrl = "/v1/user";
    private final String signinUrl = "/v1/auth/signin";

    @BeforeEach
    void setUp() throws Exception {
        log.info("Before Test");

        // 한글 깨짐 해결
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 필터 추가
                .alwaysDo(print())
                .build();

        // sign in test 를 위한 회원가입
        userRepository.save(User.builder()
                .snsId(testUserSnsId)
                .provider(testUserSnsType)
                .username("테스트뮤폴러")
                .favoriteInstrument(new ArrayList<>(Arrays.asList(Instrument.piccolo, Instrument.drum)))
                .birth(LocalDate.parse("1996-03-18"))
                .terms(true)
                .isMajor(true)
                .role(User.Role.USER)
                .build());
    }

    @Test
    void getUserProfile() throws Exception {
        String content = "{\n" +
                "    \"provider\": \""+testUserSnsType.toString()+"\",\n" +
                "    \"accessToken\": \""+ testUserSnsId +"\"\n" +
                "}";

        String result = mockMvc.perform(post(signinUrl)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse().getContentAsString();

        Map data = objectMapper.readValue(result, Map.class);
        String jwt = (String) data.get("data");

        mockMvc.perform(get(baseUrl+"/me").header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getUserProfileFail() throws Exception {
        String jwt = "invalid jwt";

        mockMvc.perform(get(baseUrl+"/me").header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUserProfile() throws Exception {
        String content = "{\n" +
                "    \"provider\": \""+testUserSnsType.toString()+"\",\n" +
                "    \"accessToken\": \""+ testUserSnsId +"\"\n" +
                "}";

        String result = mockMvc.perform(post(signinUrl)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse().getContentAsString();

        Map data = objectMapper.readValue(result, Map.class);
        String jwt = (String) data.get("data");

        mockMvc.perform(delete(baseUrl+"/me").header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUserProfileFail() throws Exception {
        String content = "{\n" +
                "    \"provider\": \""+testUserSnsType.toString()+"\",\n" +
                "    \"accessToken\": \""+ testUserSnsId +"\"\n" +
                "}";

        String result = mockMvc.perform(post(signinUrl)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse().getContentAsString();

        Map data = objectMapper.readValue(result, Map.class);
        String jwt = (String) data.get("data");

        mockMvc.perform(delete(baseUrl+"/me").header("Authorization", jwt))
                .andExpect(status().isOk());

        // 이중 삭제
        mockMvc.perform(delete(baseUrl+"/me").header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
