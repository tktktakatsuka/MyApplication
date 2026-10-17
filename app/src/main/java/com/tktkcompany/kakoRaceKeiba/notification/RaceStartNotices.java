package com.tktkcompany.kakoRaceKeiba.notification;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.ui.entries.EntryForm;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * お気に入り馬の「まもなく出走」通知の発走時刻の読み取りと文面づくり。
 * 画面や Firebase に依存しない処理だけを置く。
 */
public final class RaceStartNotices {

    /** 発走の何分前に知らせるか */
    public static final int MINUTES_BEFORE = 30;

    private static final Pattern START_TIME = Pattern.compile("(\\d{1,2}):(\\d{2})発走");
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 1レース分の通知に載せる1頭 */
    public static final class Horse {
        final String name;
        @Nullable final String number;
        /** 単勝オッズ（"5.3"）。取れなければ null */
        @Nullable final String odds;
        @Nullable final Integer popularity;

        public Horse(String name, @Nullable String number, @Nullable String odds, @Nullable Integer popularity) {
            this.name = name;
            this.number = number;
            this.odds = odds;
            this.popularity = popularity;
        }
    }

    private RaceStartNotices() {
    }

    /** 開催日と「… 15:30発走 …」から発走日時を返す。読み取れなければ null */
    @Nullable
    public static LocalDateTime startTime(String date, @Nullable String raceInfo) {
        if (date == null || raceInfo == null) return null;
        Matcher m = START_TIME.matcher(raceInfo);
        if (!m.find()) return null;
        try {
            return LocalDate.parse(date, YMD).atTime(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        } catch (DateTimeException e) {
            return null;
        }
    }

    public static String title(List<Horse> horses) {
        if (horses.size() == 1) return "★" + horses.get(0).name + "がまもなく出走";
        return "★お気に入り馬" + horses.size() + "頭がまもなく出走";
    }

    /**
     * 「15:30発走 京都11R トルマリンS（ダ1800m）」の下に「5番 アンムート 単勝5.3倍（3番人気）」を並べる。
     * oddsTime はオッズを取った時刻の表示（"13:15"）。オッズが1頭も無ければ使わない
     */
    public static String body(LocalDateTime start, String course, int raceNo, @Nullable String raceTitle,
                              @Nullable String raceInfo, List<Horse> horses, @Nullable String oddsTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(start.format(DateTimeFormatter.ofPattern("H:mm"))).append("発走 ")
                .append(course).append(raceNo).append('R');
        if (raceTitle != null && !raceTitle.trim().isEmpty()) sb.append(' ').append(raceTitle.trim());
        String label = EntryForm.courseLabel(raceInfo);
        if (!label.isEmpty()) sb.append('（').append(label).append("m）");
        boolean anyOdds = false;
        for (Horse horse : horses) {
            sb.append('\n');
            if (horse.number != null && !horse.number.trim().isEmpty()) sb.append(horse.number.trim()).append("番 ");
            sb.append(horse.name);
            if (horse.odds != null) {
                anyOdds = true;
                sb.append(" 単勝").append(horse.odds).append('倍');
                if (horse.popularity != null) sb.append('（').append(horse.popularity).append("番人気）");
            }
        }
        if (anyOdds && oddsTime != null) sb.append("\nオッズは").append(oddsTime).append("時点");
        return sb.toString();
    }
}
