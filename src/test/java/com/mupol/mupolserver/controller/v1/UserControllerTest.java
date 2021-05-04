package com.mupol.mupolserver.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.user.FollowersResDto;
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

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private final SnsType testUserProvider = SnsType.test;
    private final String baseUrl = "/v1/user";

    private Gson gson;
    private Type typeOfT;
    private User[] testUser = new User[2];
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        log.info("Before Test");

        // 한글 깨짐 해결
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 필터 추가
                .alwaysDo(print())
                .build();

        for(int i=0; i<testUser.length; ++i) {
            testUser[i] = User.builder()
                    .snsId(testUserSnsId + i)
                    .provider(testUserProvider)
                    .username("테스트뮤폴러-" + i)
                    .favoriteInstrument(new ArrayList<>(Arrays.asList(Instrument.piccolo, Instrument.drum)))
                    .birth(LocalDate.parse("1996-03-18"))
                    .terms(true)
                    .isMajor(true)
                    .role(User.Role.USER)
                    .build();
            userRepository.save(testUser[i]);
        }

        jwt = getJwt();
        gson = new Gson();
        typeOfT = new TypeToken<ListResult<FollowersResDto>>(){}.getType();
    }

    @Test
    void getUserProfile() throws Exception {
        mockMvc.perform(get(baseUrl+"/me").header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getUserProfileFail() throws Exception {
        jwt = "invalid jwt";

        mockMvc.perform(get(baseUrl+"/me").header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUserProfile() throws Exception {
        mockMvc.perform(delete(baseUrl+"/me").header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUserProfileFail() throws Exception {
        mockMvc.perform(delete(baseUrl+"/me").header("Authorization", jwt))
                .andExpect(status().isOk());

        // 이중 삭제
        mockMvc.perform(delete(baseUrl+"/me").header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void followUser() throws Exception {
        // follow user
        mockMvc.perform(post(baseUrl + "/friendship/follow/" + testUser[1].getId())
                .header("Authorization",jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void unfollowUser() throws Exception {
        followUser();
        // unfollow user
        mockMvc.perform(post(baseUrl + "/friendship/unfollow/" + testUser[1].getId())
                .header("Authorization",jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getFollowerList() throws Exception {
        followUser();

        String followerResult = mockMvc.perform(get(baseUrl + "/"+ testUser[1].getId()+"/followers/")
                .header("Authorization",jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        ListResult<FollowersResDto> response = gson.fromJson(followerResult, typeOfT);
        List<FollowersResDto> followerList = response.getData();
        boolean followerExist = false;

        for(FollowersResDto f: followerList) {
            if (testUser[0].getId().equals(f.getUserId())) {
                followerExist = true;
                break;
            }
        }
        if(!followerExist)
            throw new IllegalArgumentException("follow failed");
    }

    @Test
    public void getFollowingList() throws Exception {
        followUser();
        String followingResult = mockMvc.perform(get(baseUrl + "/"+ testUser[0].getId()+"/followings/")
                .header("Authorization",jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        // check following list
        ListResult<FollowersResDto> response = gson.fromJson(followingResult, typeOfT);
        List<FollowersResDto> followingList = response.getData();
        boolean followingExist = false;

        for(FollowersResDto f: followingList) {
            if (testUser[1].getId().equals(f.getUserId())) {
                followingExist = true;
                break;
            }
        }
        if(!followingExist)
            throw new IllegalArgumentException("follow failed");
    }

    @Test
    public void getFollowerListFailByUnknownUser() throws Exception {
        mockMvc.perform(get(baseUrl + "/"+ -1 +"/followings/")
                .header("Authorization",jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void getFollowingListFailByUnknownUser() throws Exception {
        mockMvc.perform(get(baseUrl + "/"+ -1 +"/followers/")
                .header("Authorization",jwt)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    private String getJwt() throws Exception {
        String content = "{\n" +
                "    \"provider\": \""+ testUserProvider +"\",\n" +
                "    \"accessToken\": \""+ testUserSnsId + 0 +"\"\n" +
                "}";

        String signinUrl = "/v1/auth/signin";
        String result = mockMvc.perform(post(signinUrl)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse().getContentAsString();

        Map data = objectMapper.readValue(result, Map.class);
        return (String) data.get("data");
    }
}
