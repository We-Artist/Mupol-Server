package com.mupol.mupolserver.controller.v1;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.comment.Comment;
import com.mupol.mupolserver.domain.response.ListResult;
import com.mupol.mupolserver.domain.response.SingleResult;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.domain.video.VideoRepository;
import com.mupol.mupolserver.dto.comment.CommentReqDto;
import com.mupol.mupolserver.dto.comment.CommentResDto;
import com.mupol.mupolserver.service.CommentService;
import com.mupol.mupolserver.service.ResponseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@Api(tags = {"5. Comment"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/comment")
public class CommentController {

    private final UserRepository userRepository;
    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;
    private final VideoRepository videoRepository;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 댓글 등록", notes = "")
    @PostMapping(value = "/{videoId}/new")
    public ResponseEntity<SingleResult<CommentResDto>> addVideo(
            @RequestHeader("Authorization") String jwt,
            @ApiParam(value = "metaData") @RequestPart CommentReqDto metaData,
            @PathVariable String videoId
    ) throws IOException, InterruptedException {
        User user = userRepository.findById(Long.valueOf(jwtTokenProvider.getUserPk(jwt))).orElseThrow(CUserNotFoundException::new);
        Video video = videoRepository.findById(Long.valueOf(videoId)).orElseThrow(()->new IllegalArgumentException("not exist video"));

        CommentResDto dto = commentService.uploadComment(user, video, metaData);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 댓글 전체 조회", notes = "")
    @GetMapping("/{videoId}")
    public ResponseEntity<ListResult<CommentResDto>> getVideoList(
            @RequestHeader(value = "Authorization") String jwt,
            @PathVariable String videoId
    ) {
        Long userId = Long.valueOf(jwtTokenProvider.getUserPk(jwt));
        List<CommentResDto> dtoList = commentService.getSndDtoList(commentService.getComments(Long.valueOf(videoId)));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getListResult(dtoList));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 댓글 수정", notes = "")
    @PatchMapping("/{commentId}")
    public ResponseEntity<SingleResult<CommentResDto>> updateComment(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String commentId,
            @RequestBody String content
    ) {
        if (content == null || content.equals(""))
            throw new IllegalArgumentException("content is empty");
        Comment comment = commentService.updateContent(Long.valueOf(commentId), content);
        CommentResDto dto = commentService.getSndDto(comment);
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult(dto));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "jwt 토큰", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "비디오 댓글 삭제", notes = "")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<SingleResult<String>> deleteComment(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String commentId
    ) {
        commentService.deleteComment(Long.valueOf(commentId));
        return ResponseEntity.status(HttpStatus.OK).body(responseService.getSingleResult("removed"));
    }


}
