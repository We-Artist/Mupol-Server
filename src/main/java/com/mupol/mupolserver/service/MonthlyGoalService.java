package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoalRepository;
import com.mupol.mupolserver.domain.sound.Sound;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.monthlyGoal.GoalStatusResDto;
import com.mupol.mupolserver.dto.monthlyGoal.MonthlyGoalDto;
import com.mupol.mupolserver.dto.sound.SoundResDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import com.mupol.mupolserver.dto.video.VideoWithSaveDto;
import com.mupol.mupolserver.util.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class MonthlyGoalService {
    private final MonthlyGoalRepository monthlyGoalRepository;
    private final SoundService soundService;
    private final VideoService videoService;

    public MonthlyGoal getMonthlyGoal(User user, LocalDate startDate) {
        Optional<MonthlyGoal> goal = monthlyGoalRepository.findMonthlyGoalByUserAndStartDate(user, startDate);
        if (goal.isEmpty()) return null;
        return goal.get();
    }

    public boolean isGoalExist(User user, LocalDate startDate) {
        return monthlyGoalRepository.findMonthlyGoalByUserAndStartDate(user, startDate).isPresent();
    }

    public void update(User user) {
        LocalDate startDate = TimeUtils.getCurrentMonthFirstDate();
        MonthlyGoal monthlyGoal = getMonthlyGoal(user, startDate);
        if(monthlyGoal == null) return;
        int year = monthlyGoal.getStartDate().getYear();
        int month = monthlyGoal.getStartDate().getMonthValue();
        int videoCnt = videoService.getVideoCountAtMonth(user, year, month);
        int soundCnt = soundService.getSoundCountAtMonth(user, year, month);
        monthlyGoal.setAchieveNumber(videoCnt + soundCnt + 1);
        monthlyGoalRepository.save(monthlyGoal);
    }

    public MonthlyGoalDto getDto(MonthlyGoal goal) {
        if (goal == null) return null;
        return MonthlyGoalDto.builder()
                .achieveNumber(goal.getAchieveNumber())
                .goalNumber(goal.getGoalNumber())
                .year(goal.getStartDate().getYear())
                .month(goal.getStartDate().getMonthValue())
                .build();
    }

    public List<MonthlyGoalDto> getDtoList(List<MonthlyGoal> goalList) {
        List<MonthlyGoalDto> dtoList = new ArrayList<>();
        for (MonthlyGoal goal : goalList) {
            dtoList.add(getDto(goal));
        }
        return dtoList;
    }

    public MonthlyGoal save(MonthlyGoal monthlyGoal) {
        return monthlyGoalRepository.save(monthlyGoal);
    }

    public List<GoalStatusResDto> getAllGoals(User user) {
        List<GoalStatusResDto> result = new ArrayList<>();

        int year = TimeUtils.getCurrentMonthFirstDate().getYear();
        int month = TimeUtils.getCurrentMonthFirstDate().getMonthValue();
        LocalDate startDate;
        for (int i = 0; i < 24; ++i) {
            startDate = LocalDate.of(year, month, 1);
            MonthlyGoal goal = getMonthlyGoal(user, startDate);
            if(goal == null) continue;
            List<SoundResDto> sndList = soundService.getSoundAtMonth(user, year, month);
            List<VideoWithSaveDto> vidList = videoService.getVideoAtMonth(user, year, month);
            if (goal.getGoalNumber() == 0 && sndList.size() == 0 && vidList.size() == 0) continue;

            result.add(GoalStatusResDto.builder()
                    .currentGoal(getDto(goal))
                    .soundList(sndList)
                    .videoList(vidList)
                    .build());
            month -= 1;
            if (month == 0) {
                month = 12;
                year -= 1;
            }
        }

        return result;
    }
}
