package com.mupol.mupolserver.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

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
        Calendar cal = Calendar.getInstance();
        int lastDate = cal.getActualMaximum(Calendar.DATE);
        return LocalDateTime.of(LocalDate.of(year, month, lastDate), LocalTime.of(23,59,59));
    }
}
