package com.mupol.mupolserver.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaType {
    Video("video"),
    Sound("sound");

    private final String value;
}