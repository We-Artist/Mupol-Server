package com.mupol.mupolserver.dto.video;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ViewHistoryDto {

    @ApiModelProperty(notes ="View History Id")
    private Long id;

    @ApiModelProperty(notes ="created at")
    private LocalDateTime createdAt;

    @ApiModelProperty(notes = "User Id")
    private Long userId;

    @ApiModelProperty(notes = "Video Id")
    private Long VideoId;
}
