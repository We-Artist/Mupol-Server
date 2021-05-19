package com.mupol.mupolserver.dto.sound;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SoundOptionDto {
    private Long id;
    private String title;
    private int bpm;
}
