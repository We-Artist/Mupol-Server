package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoal;
import com.mupol.mupolserver.domain.monthlyGoal.MonthlyGoalRepository;
import com.mupol.mupolserver.domain.user.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Service
public class MonthlyGoalService {
    private final MonthlyGoalRepository monthlyGoalRepository;
    private final SoundService soundService;
    private final VideoService videoService;

    public MonthlyGoal getMonthlyGoal(User user, LocalDate startDate) {
        return monthlyGoalRepository.findMonthlyGoalByUserAndStartDate(user, startDate)
                .orElseThrow(() -> new IllegalArgumentException("goal does not exist"));
    }

    public boolean isGoalExist(User user, LocalDate startDate) {
        return monthlyGoalRepository.findMonthlyGoalByUserAndStartDate(user, startDate).isPresent();
    }

    public void update(User user) {
        int y = LocalDate.now().getYear();
        int m = LocalDate.now().getMonthValue();
        LocalDate startDate = LocalDate.of(y, m, 1);
        MonthlyGoal monthlyGoal = getMonthlyGoal(user, startDate);
        int year = monthlyGoal.getStartDate().getYear();
        int month = monthlyGoal.getStartDate().getMonthValue();
        int videoCnt = videoService.getVideoCountAtMonth(user, year, month);
        int soundCnt = soundService.getSoundCountAtMonth(user, year, month);
        monthlyGoal.setAchieveNumber(videoCnt + soundCnt);
        monthlyGoalRepository.save(monthlyGoal);
    }

    public MonthlyGoal save(MonthlyGoal monthlyGoal) {
        return monthlyGoalRepository.save(monthlyGoal);
    }

    public List<MonthlyGoal> getAllGoals(User user) {
        return monthlyGoalRepository.findAllByUserOrderByStartDateDesc(user)
                .orElseThrow(()-> new IllegalArgumentException("illegal user"));
    }
}
