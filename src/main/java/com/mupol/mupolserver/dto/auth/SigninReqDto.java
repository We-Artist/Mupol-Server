package com.mupol.mupolserver.dto.auth;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class SigninReqDto {

    @ApiModelProperty(notes = "kakao/facebook/apple/google")
    private String provider;

    @ApiModelProperty(notes = "각 sns에서 제공받은 token")
    private String accessToken;

    @ApiModelProperty(notes = "새로 생성한 fcm 토큰")
    private String fcmToken;
}
