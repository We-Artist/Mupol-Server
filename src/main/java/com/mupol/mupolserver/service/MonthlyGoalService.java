package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoalRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.monthlyGoal.GoalStatusResDto;
import com.mupol.mupolserver.dto.monthlyGoal.MonthlyGoalDto;
import com.mupol.mupolserver.util.MonthExtractor;
import lombok.AllArgsConstructor;
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
        if (goal.isEmpty())
            return MonthlyGoal.builder()
                    .startDate(startDate)
                    .build();
        return goal.get();
    }

    public boolean isGoalExist(User user, LocalDate startDate) {
        return monthlyGoalRepository.findMonthlyGoalByUserAndStartDate(user, startDate).isPresent();
    }

    public void update(User user) {
        LocalDate startDate = MonthExtractor.getCurrentMonthFirstDate();
        MonthlyGoal monthlyGoal = getMonthlyGoal(user, startDate);
        int year = monthlyGoal.getStartDate().getYear();
        int month = monthlyGoal.getStartDate().getMonthValue();
        int videoCnt = videoService.getVideoCountAtMonth(user, year, month);
        int soundCnt = soundService.getSoundCountAtMonth(user, year, month);
        monthlyGoal.setAchieveNumber(videoCnt + soundCnt);
        monthlyGoalRepository.save(monthlyGoal);
    }

    public MonthlyGoalDto getDto(MonthlyGoal goal) {
        if(goal.getUser() == null)
            return MonthlyGoalDto.builder()
                    .achieveNumber(0)
                    .goalNumber(0)
                    .year(goal.getStartDate().getYear())
                    .month(goal.getStartDate().getMonthValue())
                    .build();
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

        int year = MonthExtractor.getCurrentMonthFirstDate().getYear();
        int month = MonthExtractor.getCurrentMonthFirstDate().getMonthValue();
        LocalDate startDate;
        for (int i = 0; i < 24; ++i) {
            startDate = LocalDate.of(year, month, 1);
            result.add(GoalStatusResDto.builder()
                    .currentGoal(getDto(getMonthlyGoal(user, startDate)))
                    .soundList(soundService.getSoundAtMonth(user, year, month))
                    .videoList(videoService.getVideoAtMonth(user, year, month))
                    .build());
            month -= 1;
            if(month == 0) {
                month = 12;
                year -= 1;
            }
        }

        return result;
    }
}
