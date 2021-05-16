package com.mupol.mupolserver.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TargetType {
    like("video"),
    comment("video"),
    follow("user")
    ;

    private final String type;
}
