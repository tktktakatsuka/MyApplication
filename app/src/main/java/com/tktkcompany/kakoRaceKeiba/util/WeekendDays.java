package com.tktkcompany.kakoRaceKeiba.util;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeekendDays {

    // 先月と今月の開催候補日（土日・祝日の月曜）のうち、今日以前の日付を取得するメソッド
    public static List<String> getPastWeekendsInCurrentMonth() {
        List<String> pastWeekends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        // 先月の初日から今月の末日までループ（今日より後は除外）
        LocalDate lastDay = currentMonth.atEndOfMonth();
        for (LocalDate date = currentMonth.minusMonths(1).atDay(1); !date.isAfter(lastDay); date = date.plusDays(1)) {
            if (date.isAfter(today)) {
                break;
            }
            if (isRaceDayCandidate(date)) {
                pastWeekends.add(date.format(formatter));
            }
        }

        Collections.sort(pastWeekends);
        return pastWeekends;
    }

    // 今日から1週間以内の開催候補日（土日・祝日の月曜）を取得するメソッド
    public static List<LocalDate> getUpcomingRaceDays() {
        List<LocalDate> days = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            if (isRaceDayCandidate(date)) {
                days.add(date);
            }
        }
        return days;
    }

    // 土日と祝日の月曜（JRAの月曜開催）を開催候補日とみなす
    public static boolean isRaceDayCandidate(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return true;
        return dow == DayOfWeek.MONDAY && JapaneseHolidays.isHoliday(date);
    }

    // yyyyMMdd を「2026/10/12 (月)」の形にする（形式が違う場合はそのまま返す）
    public static String formatWithDayOfWeek(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) return dateStr;
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + getDayOfWeek(dateStr);
        } catch (DateTimeParseException e) {
            return dateStr;
        }
    }

    // yyyyMMdd を「10/12(月)」の形にする（形式が違う場合はそのまま返す）
    public static String formatShort(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) return dateStr;
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return date.format(DateTimeFormatter.ofPattern("M/d")) + getDayOfWeek(dateStr).trim();
        } catch (DateTimeParseException e) {
            return dateStr;
        }
    }

    public static String getDayOfWeek(String dateStr) {
        try {
            // 8桁の日付フォーマットを定義
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(dateStr, formatter);
            // 曜日を取得
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            // 日本語の曜日名を返す
            switch (dayOfWeek) {
                case MONDAY:
                    return " (月)";
                case TUESDAY:
                    return " (火)";
                case WEDNESDAY:
                    return " (水)";
                case THURSDAY:
                    return " (木)";
                case FRIDAY:
                    return " (金)";
                case SATURDAY:
                    return " (土)";
                case SUNDAY:
                    return " (日)";
                default:
                    return "";
            }
        } catch (DateTimeParseException e) {
            return "日付の形式が正しくありません。";
        }
    }
}
