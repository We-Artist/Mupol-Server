package com.mupol.mupolserver.domain.notification;

import com.mupol.mupolserver.domain.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<List<Notification>> findAllByReceiverAndIsReadIsFalse(User receiver);
    Optional<Notification> findByIdAndReceiver(Long id, User receiver);
    Optional<List<Notification>> findAllByReceiverOrderByCreatedAtDesc(User receiver);
    Optional<List<Notification>> findAllByReceiverOrderByCreatedAtDesc(User receiver, PageRequest pageRequest);
}
