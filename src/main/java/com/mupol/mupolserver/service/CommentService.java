package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.comment.Comment;
import com.mupol.mupolserver.domain.comment.CommentRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.dto.comment.CommentReqDto;
import com.mupol.mupolserver.dto.comment.CommentResDto;
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

    public CommentResDto uploadComment(User user, Video video, CommentReqDto metaData) throws IOException, InterruptedException {

        Comment comment = Comment.builder()
                .user(user)
                .video(video)
                .content(metaData.getContent())
                .build();
        commentRepository.save(comment);

        return getSndDto(comment);
    }

    public CommentResDto getSndDto(Comment snd) {

        CommentResDto dto = new CommentResDto();

        dto.setId(snd.getId());
        dto.setVideoId(snd.getVideo().getId());
        dto.setContent(snd.getContent());
        dto.setCreatedAt(snd.getCreatedAt());
        dto.setUpdatedAt(snd.getModifiedDate());
        dto.setUserId(snd.getUser().getId());
        return dto;
    }

    public List<CommentResDto> getSndDtoList(List<Comment> commentList) {
        return commentList.stream().map(this::getSndDto).collect(Collectors.toList());
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


