package com.mupol.mupolserver.dto.auth;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
public class SignupReqDto {
    private String provider;
    private String accessToken;
    private String name;
    private boolean terms;
    private boolean isMajor;
    private List<String> instruments;
    private LocalDate birth;
}