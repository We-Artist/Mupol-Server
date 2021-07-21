package com.mupol.mupolserver.dto.video;

import com.mupol.mupolserver.domain.hashtag.Hashtag;
import com.mupol.mupolserver.dto.comment.CommentResDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Setter
@Getter
public class VideoViewDto extends VideoResDto {
    @ApiModelProperty(notes = "댓글 목록")
    private List<CommentResDto> commentResDtoList;

    @ApiModelProperty(notes = "해시태그 목록")
    private List<Hashtag> hashtagList;

    @ApiModelProperty(notes = "저장 수")
    private Integer saveNum;

    @ApiModelProperty(notes = "저장 flag")
    private Boolean saveFlag;

    @ApiModelProperty(notes = "팔로우 여부")
    private Boolean isFollowing;

    @ApiModelProperty(notes = "다음 비디오 영상들")
    private List<VideoWithSaveDto> nextVideoList;

    @ApiModelProperty(notes = "가로 크기")
    private Long width;

    @ApiModelProperty(notes = "세로 크기")
    private Long height;

    @ApiModelProperty(notes = "내 비디오 여부")
    private Boolean isMine;

    @ApiModelProperty(notes = "대표 비디오 여부")
    private Boolean isRepresentativeVideo;
}
