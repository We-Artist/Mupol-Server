package com.mupol.mupolserver.domain.follower;

import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.user.User;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "follower")
public class Follower extends BaseTime {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="from_id")
    private User from;

    @ManyToOne
    @JoinColumn(name="to_id")
    private User to;

    @Setter
    private boolean isFollowEachOther;
}
