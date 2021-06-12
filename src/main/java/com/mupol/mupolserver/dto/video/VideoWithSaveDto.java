package com.mupol.mupolserver.dto.video;

import com.mupol.mupolserver.domain.hashtag.Hashtag;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Setter
@Getter
public class VideoWithSaveDto extends VideoResDto {

    @ApiModelProperty(notes = "해시태그 목록")
    private List<Hashtag> hashtagList;

    @ApiModelProperty(notes = "저장 수")
    private Integer saveNum;

    @ApiModelProperty(notes = "저장 flag")
    private Boolean saveFlag;
}
