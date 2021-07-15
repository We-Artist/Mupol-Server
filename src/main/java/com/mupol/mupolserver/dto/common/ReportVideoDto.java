package com.mupol.mupolserver.dto.common;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ReportVideoDto {
    private Long id;
    private Long reporterId;
    private Long reportedVidId;
    private String type;
    private String reportVideoContent;
}
