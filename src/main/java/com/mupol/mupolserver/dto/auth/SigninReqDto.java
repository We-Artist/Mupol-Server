package com.mupol.mupolserver.dto.auth;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;

@Getter
public class SigninReqDto {

    @ApiModelProperty(notes = "kakao/facebook/apple/google")
    private String provider;

    @ApiModelProperty(notes = "각 sns에서 제공받은 token")
    private String accessToken;
}
