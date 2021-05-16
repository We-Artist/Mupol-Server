package com.mupol.mupolserver.dto.notification;

import io.swagger.annotations.ApiParam;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class NotificationDto {
    private String title;
    private String body;
    private String senderProfileImageUrl;
    private LocalDateTime createdAt;

    @ApiParam("연결되는 영상 id")
    private Long videoId;

    @ApiParam("연결되는 유저 id")
    private Long userId;
}
