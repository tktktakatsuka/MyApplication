package com.tktkcompany.kakoRaceKeiba.notification;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.List;

/**
 * お気に入り馬の結果の速報通知（レース確定直後）の文面を作る。
 * 画面や Firebase に依存しない文字列の組み立てだけを置く。
 */
public final class LiveResultNotices {

    /** 1頭分の結果 */
    public static final class Finish {
        final String horseName;
        /** "3"、"取消" など */
        @Nullable final String rank;
        @Nullable final String popular;
        @Nullable final String winOdds;
        @Nullable final String time;

        public Finish(String horseName, @Nullable String rank, @Nullable String popular,
                      @Nullable String winOdds, @Nullable String time) {
            this.horseName = horseName;
            this.rank = rank;
            this.popular = popular;
            this.winOdds = winOdds;
            this.time = time;
        }
    }

    private LiveResultNotices() {
    }

    /** 速報で知らせた結果を、夜のまとめの通知で2回知らせないための目印 */
    public static String key(String date, String course, int raceNo, String horseName) {
        return date + "|" + course + "|" + raceNo + "|" + horseName;
    }

    public static String title(String course, int raceNo, List<Finish> finishes) {
        if (finishes.size() == 1) {
            Finish f = finishes.get(0);
            String popular = f.popular != null ? "（" + f.popular + "番人気）" : "";
            if ("1".equals(f.rank)) return "★" + f.horseName + "が1着！" + popular;
            return "★" + f.horseName + " " + rankLabel(f.rank) + popular;
        }
        return "★" + course + raceNo + "R お気に入り馬の結果";
    }

    /** 1行目にレース、続けて結果、最後にメモへの案内 */
    public static String body(String date, String course, int raceNo, @Nullable String raceTitle, List<Finish> finishes) {
        StringBuilder sb = new StringBuilder();
        sb.append(WeekendDays.formatShort(date)).append(' ').append(course).append(raceNo).append('R');
        if (raceTitle != null && !raceTitle.trim().isEmpty()) sb.append(' ').append(raceTitle.trim());
        if (finishes.size() == 1) {
            Finish f = finishes.get(0);
            StringBuilder detail = new StringBuilder();
            if (f.winOdds != null) detail.append("単勝").append(f.winOdds).append('倍');
            if (f.time != null) {
                if (detail.length() > 0) detail.append('・');
                detail.append(f.time);
            }
            if (detail.length() > 0) sb.append('\n').append(detail);
        } else {
            for (Finish f : finishes) {
                sb.append('\n').append(rankLabel(f.rank)).append(' ').append(f.horseName);
                if (f.popular != null) sb.append("（").append(f.popular).append("番人気）");
            }
        }
        sb.append("\n📝 タップしてメモを残せます");
        return sb.toString();
    }

    /** "3" → "3着"、"取消" はそのまま */
    static String rankLabel(@Nullable String rank) {
        if (rank == null) return "-";
        return rank.matches("\\d+") ? rank + "着" : rank;
    }
}
