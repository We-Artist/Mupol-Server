package com.mupol.mupolserver.domain.video;

import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "video")
public class Video extends BaseTime {
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
    private int view_num;

    @Setter
    private int like_num;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Instrument> instrument_list = new ArrayList<>();

}
