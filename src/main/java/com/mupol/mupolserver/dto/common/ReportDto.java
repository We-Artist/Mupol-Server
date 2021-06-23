package com.mupol.mupolserver.dto.common;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ReportDto {
    private String title;
    private String type;
    private String name;
    private String email;
    private String content;
}
