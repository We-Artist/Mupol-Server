package com.mupol.mupolserver.domain.playlist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mupol.mupolserver.domain.common.BaseTime;
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
@Table(name = "playlistVideo")
public class PlaylistVideo extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Playlist playlist;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Video video;
}
