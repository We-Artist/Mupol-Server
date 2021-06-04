package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoalRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.monthlyGoal.MonthlyGoalDto;
import com.mupol.mupolserver.util.MonthExtractor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
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
        if(goal.isEmpty())
            return null;
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
        return MonthlyGoalDto.builder()
                .achieveNumber(goal.getAchieveNumber())
                .goalNumber(goal.getGoalNumber())
                .startDate(goal.getStartDate().atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
                .build();
    }

    public List<MonthlyGoalDto> getDtoList(List<MonthlyGoal> goalList) {
        List<MonthlyGoalDto> dtoList = new ArrayList<>();
        for(MonthlyGoal goal: goalList) {
            dtoList.add(getDto(goal));
        }
        return dtoList;
    }

    public MonthlyGoal save(MonthlyGoal monthlyGoal) {
        return monthlyGoalRepository.save(monthlyGoal);
    }

    public List<MonthlyGoal> getAllGoals(User user) {
        return monthlyGoalRepository.findAllByUserOrderByStartDateDesc(user)
                .orElseThrow(()-> new IllegalArgumentException("illegal user"));
    }
}
