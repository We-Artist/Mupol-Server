package com.mupol.mupolserver.domain.video;

import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.hashtag.Hashtag;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Proxy(lazy = false)
@Entity
@Table(name = "video")
public class Video extends BaseTime implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Setter
    private String title;

    @Setter
    private String origin_title;

    @Setter
    private String detail;

    @Setter
    private Boolean is_private;

    @Setter
    private String fileUrl;

    @Setter
    @Column(name="view_num")
    private int viewNum;

    @Setter
    @Column(name="like_num")
    private int likeNum;

    @Setter
    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<Instrument> instruments = new ArrayList<>();

    @Setter
    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<Hashtag> hashtags = new ArrayList<>();

    @Setter
    private String thumbnailUrl;

}
