package com.mupol.mupolserver.dto.monthlyGoal;

import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.sound.Sound;
import com.mupol.mupolserver.domain.video.Video;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class GoalStatusReqDto {
    private MonthlyGoal currentGoal;
    private List<Video> videoList;
    private List<Sound> soundList;

}
