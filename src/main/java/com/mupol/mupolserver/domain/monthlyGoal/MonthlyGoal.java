package com.mupol.mupolserver.domain.monthlyGoal;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mupol.mupolserver.domain.common.BaseTime;
import com.mupol.mupolserver.domain.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDate;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "monthly_goal")
public class MonthlyGoal extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private int goalNumber;

    @Column(nullable = false)
    private int achieveNumber;
}
