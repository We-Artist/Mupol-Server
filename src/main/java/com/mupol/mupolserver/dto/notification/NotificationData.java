package com.mupol.mupolserver.dto.notification;

import com.mupol.mupolserver.domain.notification.TargetType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class NotificationData {
    private String  title;
    private String body;
    private TargetType targetType;
    private Long targetId;
    private boolean isFollowing;
}
