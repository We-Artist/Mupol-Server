package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.notification.Notification;
import com.mupol.mupolserver.domain.notification.NotificationRepository;
import com.mupol.mupolserver.domain.notification.TargetType;
import com.mupol.mupolserver.domain.user.User;
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
            String title,
            String body,
            TargetType type,
            Long targetId
    ) throws IOException {
        Long videoId = null;
        Long userId = null;

        if(type == TargetType.follow)
            userId = targetId;
        else if(type == TargetType.comment || type == TargetType.like)
            videoId = targetId;

        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .sender(sender)
                .receiver(receiver)
                .videoId(videoId)
                .userId(userId)
                .isRead(false)
                .build();

        fcmMessageService.sendMessageTo(receiver.getFcmToken(), title, body, type, targetId);
        notificationRepository.save(notification);
    }

    public List<NotificationDto> getReceivedNotification(User user) {
        Optional<List<Notification>> res = notificationRepository.findAllByReceiverOrderByCreatedAtDesc(user);
        return getNotificationDtos(res);
    }

    public List<NotificationDto> getReceivedNotification(User user, int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 20);
        Optional<List<Notification>> res = notificationRepository.findAllByReceiverOrderByCreatedAtDesc(user, pageRequest);
        return getNotificationDtos(res);
    }

    private List<NotificationDto> getNotificationDtos(Optional<List<Notification>> res) {
        List<NotificationDto> dtoList = new ArrayList<>();
        if (res.isEmpty())
            return Collections.emptyList();
        for (Notification noti : res.get()) {
            dtoList.add(NotificationDto.builder()
                    .title(noti.getTitle())
                    .body(noti.getBody())
                    .senderName(noti.getSender().getUsername())
                    .senderProfileImageUrl(noti.getSenderProfileImageUrl())
                    .createdAt(noti.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
                    .userId(noti.getUserId())
                    .videoId(noti.getVideoId())
                    .isRead(noti.isRead())
                    .build());
        }
        return dtoList;
    }

    public UnreadNotificationNumberDto getUnreadNotificationNumber(User user) {
        Optional<List<Notification>> res = notificationRepository.findAllByReceiverAndIsReadIsFalse(user);
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