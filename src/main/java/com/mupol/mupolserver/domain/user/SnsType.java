package com.mupol.mupolserver.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SnsType {

    kakao("kakao"),
    google("google"),
    facebook("facebook"),
    apple("apple");

    private final String type;
}
