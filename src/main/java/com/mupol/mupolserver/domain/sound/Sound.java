package com.mupol.mupolserver.domain.sound;

import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "sound")
public class Sound extends BaseTime implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Setter
    private int bpm;

    @Setter
    private String title;

    @Setter
    private String fileUrl;
}
