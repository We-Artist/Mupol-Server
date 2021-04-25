package com.mupol.mupolserver.dto.user;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ProfileUpdateReqDto {
    private String username;
    private String bio;
    private List<String> instruments;
}