package com.mupol.mupolserver.domain.viewHistory;

import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.video.Video;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Proxy(lazy = false)
@Entity
@Table(name = "viewHistory")
public class ViewHistory extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Video video;
}
