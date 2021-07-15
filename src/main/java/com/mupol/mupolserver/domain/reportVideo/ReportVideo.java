package com.mupol.mupolserver.domain.reportVideo;

import com.mupol.mupolserver.domain.common.ReportType;
import com.mupol.mupolserver.domain.common.ReportVideoType;
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
@Table(name = "report_video")
public class ReportVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportedVid")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Video reportedVid;

    @Setter
    private String content;

    @Setter
    private ReportVideoType reportVideoType;
}
