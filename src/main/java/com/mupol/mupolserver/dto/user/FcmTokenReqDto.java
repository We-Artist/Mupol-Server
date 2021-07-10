package com.mupol.mupolserver.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FcmTokenReqDto {
    private String token;
}
