package com.mupol.mupolserver.dto.notification;

import com.mupol.mupolserver.domain.notification.TargetType;
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
    private String senderName;
    private String senderProfileImageUrl;
    private Long createdAt;
    private boolean isRead;
    private TargetType targetType;

    @ApiParam("연결되는 영상 id")
    private Long videoId;

    @ApiParam("연결되는 유저 id")
    private Long userId;
}
