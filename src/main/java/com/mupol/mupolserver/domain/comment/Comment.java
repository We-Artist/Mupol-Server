package com.mupol.mupolserver.domain.comment;

import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "video_comment")
public class Comment extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Video video;

    @Setter
    private String content;

}
