package com.mupol.mupolserver.dto.video;

import com.mupol.mupolserver.domain.instrument.Instrument;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class VideoResDto {

    @ApiModelProperty(notes ="Video Id")
    private Long id;

    @ApiModelProperty(notes = "비디오 제목")
    private String title;

    @ApiModelProperty(notes = "원곡 제목")
    private String originTitle;

    @ApiModelProperty(notes = "설명")
    private String detail;

    @ApiModelProperty(notes = "공개 여부")
    private Boolean isPrivate;

    @ApiModelProperty(notes ="created at")
    private Long createdAt;

    @ApiModelProperty(notes ="updated at")
    private Long updatedAt;

    @ApiModelProperty(notes ="file url")
    private String fileUrl;

    @ApiModelProperty(notes ="악기 목록")
    private List<Instrument> instrumentList;

    @ApiModelProperty(notes ="댓글 수")
    private int commentNum;

    @ApiModelProperty(notes = "좋아요 flag")
    private Boolean likeFlag;

    @ApiModelProperty(notes ="좋아요 수")
    private Long likeNum;

    @ApiModelProperty(notes = "User Id")
    private Long userId;

    @ApiModelProperty(notes = "Thumbnail")
    private String thumbnailUrl;

    @ApiModelProperty(notes = "길이")
    private Long length;
}
