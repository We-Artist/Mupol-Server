package com.mupol.mupolserver.dto.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class UnreadNotificationNumberDto {
    private int number;
}
