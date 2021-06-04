package com.mupol.mupolserver.dto.monthlyGoal;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MonthlyGoalDto {
    private Long startDate;
    private int goalNumber;
    private int achieveNumber;
}
