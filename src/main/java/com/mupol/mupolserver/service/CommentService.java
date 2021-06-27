package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.comment.Comment;
import com.mupol.mupolserver.domain.comment.CommentRepository;
import com.mupol.mupolserver.domain.notification.TargetType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.dto.comment.CommentResDto;
import com.mupol.mupolserver.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final FollowerService followerService;

    public CommentResDto uploadComment(User user, Video video, String content) throws IOException, InterruptedException {

        Comment comment = Comment.builder()
                .user(user)
                .video(video)
                .content(content)
                .build();
        commentRepository.save(comment);

        notificationService.send(
                user,
                video.getUser(),
                video,
                followerService.isFollowingUser(video.getUser(), user),
                TargetType.comment
        );

        return getCommentDto(comment);
    }

    public CommentResDto getCommentDto(Comment comment) {

        CommentResDto dto = new CommentResDto();

        dto.setId(comment.getId());
        dto.setUserName(comment.getUser().getUsername());
        dto.setVideoId(comment.getVideo().getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(TimeUtils.getUnixTimestamp(comment.getCreatedAt()));
        dto.setUpdatedAt(TimeUtils.getUnixTimestamp(comment.getModifiedDate()));
        dto.setUserId(comment.getUser().getId());
        return dto;
    }

    public List<CommentResDto> getCommentDtoList(List<Comment> commentList) {
        return commentList.stream().map(this::getCommentDto).collect(Collectors.toList());
    }

    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("not exist comment"));
    }

    public List<Comment> getComments(Long videoId) {
        return commentRepository.findCommentsByVideoId(videoId).orElseThrow(()->new IllegalArgumentException("not exist comment"));
    }

    public Comment updateContent(Long commentId, String content) {
        Comment comment = getComment(commentId);
        comment.setContent(content);
        commentRepository.save(comment);

        return comment;
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

}


