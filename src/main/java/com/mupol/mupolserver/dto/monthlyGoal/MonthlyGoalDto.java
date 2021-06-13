package com.mupol.mupolserver.dto.monthlyGoal;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MonthlyGoalDto {
    private Integer year;
    private Integer month;
    private Integer goalNumber;
    private Integer achieveNumber;
}
