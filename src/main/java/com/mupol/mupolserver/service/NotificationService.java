package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.notification.Notification;
import com.mupol.mupolserver.domain.notification.NotificationRepository;
import com.mupol.mupolserver.domain.notification.TargetType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.dto.notification.NotificationDto;
import com.mupol.mupolserver.dto.notification.UnreadNotificationNumberDto;
import com.mupol.mupolserver.service.firebase.FcmMessageService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmMessageService fcmMessageService;

    public void send(
            User sender,
            User receiver,
            Object target,
            boolean isFollowingUser,
            TargetType type
    ) throws IOException {
        Long videoId = null;
        Long userId = null;
        Long targetId = null;

        if(target.getClass() == Video.class) {
            videoId = ((Video) target).getId();
            targetId = videoId;
        }
        else if(target.getClass() == User.class) {
            userId = ((User) target).getId();
            targetId = userId;
        }

        String title;
        String body = null;
        switch (type) {
            case comment:
                title = sender.getUsername() + "님이 게시물에 댓글을 작성하였습니다.";
                break;
            case like:
                title = sender.getUsername() + "님이 회원님의 게시물을 좋아합니다.";
                break;
            case follow:
                title = sender.getUsername() + "님이 회원님을 팔로우하였습니다.";
                break;
            default:
                throw new IllegalArgumentException("invalid notification type");
        }

        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .sender(sender)
                .receiver(receiver)
                .videoId(videoId)
                .userId(userId)
                .targetType(type)
                .isRead(false)
                .isFollowingUser(isFollowingUser)
                .build();

        fcmMessageService.sendMessageTo(receiver.getFcmToken(), title, body, type, targetId, isFollowingUser);
        notificationRepository.save(notification);
    }

    public List<NotificationDto> getReceivedNotification(User user) {
        Optional<List<Notification>> res = notificationRepository.findAllByReceiverOrderByCreatedAtDesc(user);
        if (res.isEmpty())
            return Collections.emptyList();
        return getNotificationDtos(res.get());
    }

    public List<NotificationDto> getReceivedNotification(User user, int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 20);
        Optional<List<Notification>> res = notificationRepository.findAllByReceiverOrderByCreatedAtDesc(user, pageRequest);
        if (res.isEmpty())
            return Collections.emptyList();
        return getNotificationDtos(res.get());
    }

    private List<NotificationDto> getNotificationDtos(List<Notification> res) {
        List<NotificationDto> dtoList = new ArrayList<>();
        for (Notification noti : res) {
            dtoList.add(NotificationDto.builder()
                    .title(noti.getTitle())
                    .body(noti.getBody())
                    .senderName(noti.getSender().getUsername())
                    .senderProfileImageUrl(noti.getSenderProfileImageUrl())
                    .createdAt(noti.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
                    .userId(noti.getUserId())
                    .videoId(noti.getVideoId())
                    .isRead(noti.isRead())
                    .isFollowingUser(noti.isFollowingUser())
                    .targetType(noti.getTargetType())
                    .build());
        }
        return dtoList;
    }

    public UnreadNotificationNumberDto getUnreadNotificationNumber(User user) {
        Optional<List<Notification>> res = notificationRepository.findAllByReceiverAndReadIsFalse(user);
        if (res.isEmpty())
            return UnreadNotificationNumberDto.builder().number(0).build();
        return UnreadNotificationNumberDto.builder().number(res.get().size()).build();
    }

    public void readNotification(Long notificationId, User received) {
        Notification noti = notificationRepository.findByIdAndReceiver(notificationId, received)
                .orElseThrow(() -> new IllegalArgumentException("invalid notification id"));
        noti.setRead(true);
        notificationRepository.save(noti);
    }
}