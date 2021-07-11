package com.mupol.mupolserver.dto.block;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockResDto {
    @ApiModelProperty(notes ="Block Id")
    private Long id;

    @ApiModelProperty(notes ="차단한 사람 Id")
    private Long blockerId;

    @ApiModelProperty(notes ="차단 당한 사람 Id")
    private Long blockedId;
}
