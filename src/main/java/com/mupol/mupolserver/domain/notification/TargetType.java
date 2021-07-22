package com.mupol.mupolserver.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TargetType {
    like("video"),
    comment("video"),
    follow("user"),
    video_posted("video_posted"),
    sound_posted("sound_posted")
    ;

    private final String type;
}
