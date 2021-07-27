package com.mupol.mupolserver.dto.common;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ReportVideoDto {
    private Long userId;
    private Long videoId;
    private String reason;
    private Integer type;
}
