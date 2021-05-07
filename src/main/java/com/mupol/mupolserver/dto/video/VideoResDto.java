package com.mupol.mupolserver.dto.video;

import com.mupol.mupolserver.domain.instrument.Instrument;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VideoResDto {

    @ApiModelProperty(notes ="Video Id")
    private Long id;

    @ApiModelProperty(notes = "비디오 제목")
    private String title;

    @ApiModelProperty(notes = "원곡 제목")
    private String origin_title;

    @ApiModelProperty(notes = "설명")
    private String detail;

    @ApiModelProperty(notes = "공개 여부")
    private Boolean is_private;

    @ApiModelProperty(notes ="created at")
    private LocalDateTime createdAt;

    @ApiModelProperty(notes ="updated at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(notes ="file url")
    private String fileUrl;

    @ApiModelProperty(notes ="악기 목록")
    private List<Instrument> instrument_list;

    @ApiModelProperty(notes ="조회수")
    private int view_num;

    @ApiModelProperty(notes ="좋아요 수")
    private int like_num;

    @ApiModelProperty(notes = "User Id")
    private Long userId;
}
