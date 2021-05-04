package com.mupol.mupolserver.domain.followers;

import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.user.User;
import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "followers")
public class Followers extends BaseTime {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="from_id")
    private User from;

    @ManyToOne
    @JoinColumn(name="to_id")
    private User to;
}
