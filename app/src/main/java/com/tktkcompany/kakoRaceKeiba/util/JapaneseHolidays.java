package com.tktkcompany.kakoRaceKeiba.util;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.HashSet;
import java.util.Set;

/**
 * 日本の祝日を計算するユーティリティ（現行の祝日法ベース、2020年以降を想定）。
 * 祝日の月曜開催など、土日以外の開催日を拾うために使う。
 */
public class JapaneseHolidays {

    public static boolean isHoliday(LocalDate date) {
        return holidaysOf(date.getYear()).contains(date);
    }

    public static Set<LocalDate> holidaysOf(int year) {
        Set<LocalDate> base = new HashSet<>();
        base.add(LocalDate.of(year, 1, 1));   // 元日
        base.add(nthMonday(year, Month.JANUARY, 2));   // 成人の日
        base.add(LocalDate.of(year, 2, 11));  // 建国記念の日
        base.add(LocalDate.of(year, 2, 23));  // 天皇誕生日
        base.add(LocalDate.of(year, 3, vernalEquinoxDay(year)));   // 春分の日
        base.add(LocalDate.of(year, 4, 29));  // 昭和の日
        base.add(LocalDate.of(year, 5, 3));   // 憲法記念日
        base.add(LocalDate.of(year, 5, 4));   // みどりの日
        base.add(LocalDate.of(year, 5, 5));   // こどもの日
        base.add(nthMonday(year, Month.JULY, 3));      // 海の日
        base.add(LocalDate.of(year, 8, 11));  // 山の日
        base.add(nthMonday(year, Month.SEPTEMBER, 3)); // 敬老の日
        base.add(LocalDate.of(year, 9, autumnalEquinoxDay(year))); // 秋分の日
        base.add(nthMonday(year, Month.OCTOBER, 2));   // スポーツの日
        base.add(LocalDate.of(year, 11, 3));  // 文化の日
        base.add(LocalDate.of(year, 11, 23)); // 勤労感謝の日

        Set<LocalDate> result = new HashSet<>(base);

        // 国民の休日：祝日に挟まれた平日
        for (LocalDate d : base) {
            LocalDate next = d.plusDays(1);
            if (!base.contains(next) && base.contains(d.plusDays(2))
                    && next.getDayOfWeek() != DayOfWeek.SUNDAY) {
                result.add(next);
            }
        }

        // 振替休日：日曜の祝日の後、最初の祝日でない日
        for (LocalDate d : base) {
            if (d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                LocalDate sub = d.plusDays(1);
                while (result.contains(sub)) {
                    sub = sub.plusDays(1);
                }
                result.add(sub);
            }
        }
        return result;
    }

    private static LocalDate nthMonday(int year, Month month, int n) {
        return LocalDate.of(year, month, 1)
                .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))
                .plusWeeks(n - 1);
    }

    // 1980〜2099年で有効な近似式
    private static int vernalEquinoxDay(int year) {
        return (int) Math.floor(20.8431 + 0.242194 * (year - 1980) - Math.floor((year - 1980) / 4.0));
    }

    private static int autumnalEquinoxDay(int year) {
        return (int) Math.floor(23.2488 + 0.242194 * (year - 1980) - Math.floor((year - 1980) / 4.0));
    }
}
