package com.mupol.mupolserver.dto.comment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResDto {
    @ApiModelProperty(notes ="Video Comment Id")
    private Long id;

    @ApiModelProperty(notes ="댓글 작성자 Id")
    private Long userId;

    @ApiModelProperty(notes ="댓글 작성자 이름")
    private String userName;

    @ApiModelProperty(notes ="댓글 작성자 프로필사진")
    private String userProfile;

    @ApiModelProperty(notes ="Video Id")
    private Long videoId;

    @ApiModelProperty(notes ="댓글 내용")
    private String content;

    @ApiModelProperty(notes ="댓글 작성 시간")
    private Long createdAt;

    @ApiModelProperty(notes ="댓글 수정 시간")
    private Long updatedAt;
    
}
