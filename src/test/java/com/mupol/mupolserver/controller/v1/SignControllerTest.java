package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @Autowired
    private UserRepository userRepository;

    private final String testUserSnsId = "1234";
    private final SnsType testUserSnsType = SnsType.test;

    @BeforeEach
    public void setUp() throws  Exception {
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
                .birth(LocalDate.parse("1996-03-18"))
                .isAgreed(true)
                .isMajor(true)
                .role(User.Role.USER)
                .build());
    }

    @Test
    public void signin() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("accessToken", testUserSnsId.toString());
        params.add("provider", testUserSnsType.toString());
        mockMvc.perform(post("/v1/auth/signin/"+testUserSnsType).params(params))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void signinFailInvalidToken() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("accessToken", "-1");
        params.add("provider", testUserSnsType.toString());
        mockMvc.perform(post("/v1/auth/signin/"+testUserSnsType).params(params))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void signinFailInvalidProvider() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("accessToken", "-1");
        params.add("provider", "invalidProvider");
        mockMvc.perform(post("/v1/auth/signin/invalidProvider").params(params))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

//    @Test
//    public void signup() throws Exception {
//
//    }
//
//    @Test
//    public void signupFail() throws Exception {
//
//    }
}
