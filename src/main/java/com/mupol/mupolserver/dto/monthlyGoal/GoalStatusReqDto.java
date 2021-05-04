package com.mupol.mupolserver.dto.monthlyGoal;

import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.dto.sound.SoundResDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class GoalStatusReqDto {
    private MonthlyGoal currentGoal;
    private List<VideoResDto> videoList;
    private List<SoundResDto> soundList;

}
