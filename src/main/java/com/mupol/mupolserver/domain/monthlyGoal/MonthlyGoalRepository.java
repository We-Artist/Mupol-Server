package com.mupol.mupolserver.domain.monthlyGoal;

import com.mupol.mupolserver.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MonthlyGoalRepository extends JpaRepository<MonthlyGoal, Long> {
    Optional<MonthlyGoal> findMonthlyGoalByUserAndStartDate(User user, LocalDate startDate);
    Optional<List<MonthlyGoal>> findAllByUserOrderByStartDateDesc(User user);
}
