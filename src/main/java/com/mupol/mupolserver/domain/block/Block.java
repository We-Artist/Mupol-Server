package com.mupol.mupolserver.domain.block;

import com.mupol.mupolserver.domain.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_block")
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "blocker")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User blocker;

    @ManyToOne
    @JoinColumn(name = "blocked")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User blocked;

}
