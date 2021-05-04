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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MonthlyGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final String baseUrl = "/v1/goal";

    private User testUser;
    private final String testUserSnsId = "hvjakeb3423";
    private final SnsType testUserProvider = SnsType.test;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        log.info("Before Test");

        // 한글 깨짐 해결
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 필터 추가
                .alwaysDo(print())
                .build();

        testUser = User.builder()
                .snsId(testUserSnsId)
                .provider(testUserProvider)
                .username("테스트뮤폴러-")
                .favoriteInstrument(new ArrayList<>(Arrays.asList(Instrument.piccolo, Instrument.drum)))
                .birth(LocalDate.parse("1996-03-18"))
                .terms(true)
                .isMajor(true)
                .role(User.Role.USER)
                .build();
        userRepository.save(testUser);

        jwt = getJwt();
    }

    @Test
    void createGoal() throws Exception {
        mockMvc.perform(post(baseUrl + "/new")
                .content("{\"goalNum\": 100}")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void createGoalFail() throws Exception {
        createGoal();
        mockMvc.perform(post(baseUrl + "/new")
                .content("{\"goalNum\": 100}")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getExistingGoal() throws Exception {
        createGoal();

        int y = LocalDate.now().getYear();
        int m = LocalDate.now().getMonthValue();

        mockMvc.perform(get(baseUrl + "/" + y + "/" + m)
                .header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getNonExistingGoal() throws Exception {
        createGoal();

        int y = LocalDate.now().getYear() + 1;
        int m = LocalDate.now().getMonthValue();

        mockMvc.perform(get(baseUrl + "/" + y + "/" + m)
                .header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void listAllGoals() throws Exception {
        createGoal();

        mockMvc.perform(get(baseUrl + "/all")
                .header("Authorization", jwt))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private String getJwt() throws Exception {

        String content = "{\n" +
                "    \"provider\": \"" + testUserProvider + "\",\n" +
                "    \"accessToken\": \"" + testUserSnsId + "\"\n" +
                "}";

        String result = mockMvc.perform(post("/v1/auth/signin")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse().getContentAsString();

        Map data = objectMapper.readValue(result, Map.class);
        return (String) data.get("data");
    }
}
