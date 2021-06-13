package com.mupol.mupolserver.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

public class MonthExtractor {
    public static LocalDate getCurrentMonthFirstDate() {
        int y = LocalDate.now().getYear();
        int m = LocalDate.now().getMonthValue();
        return LocalDate.from(getStartDate(y, m));
    }

    public static LocalDate getCurrentMonthLastDate() {
        int y = LocalDate.now().getYear();
        int m = LocalDate.now().getMonthValue();
        return LocalDate.from(getEndDate(y, m));
    }

    public static LocalDateTime getStartDate(int year, int month) {
        return LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.of(0,0,0));
    }

    public static LocalDateTime getEndDate(int year, int month) {
        int lastDate = YearMonth.of(year,month).atEndOfMonth().getDayOfMonth();
        return LocalDateTime.of(LocalDate.of(year, month, lastDate), LocalTime.of(23,59,59));
    }
}
