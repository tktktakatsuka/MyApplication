package com.tktkcompany.kakoRaceKeiba.ui.entries;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 出走馬の近走から、予想の手がかりになる印（レース間隔・距離変更・乗り替わり・同条件の成績）を作る。
 * 画面に依存しない計算だけを置く。
 */
public final class EntryForm {

    /** 「芝2000m」「ダ1400m」「障芝3000m」などから馬場と距離を取り出す */
    private static final Pattern COURSE = Pattern.compile("(芝|ダ|障)\\D{0,2}(\\d{3,4})m");
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 休み明けとみなす間隔（日） */
    static final int LONG_REST_DAYS = 70;

    private EntryForm() {
    }

    /** 近走1走分。SupabaseManager.RaceResult から必要な項目だけを受け取る */
    public static final class Run {
        final String date;
        final String raceInfo;
        final String rank;
        final String jockey;

        public Run(String date, String raceInfo, String rank, String jockey) {
            this.date = date;
            this.raceInfo = raceInfo;
            this.rank = rank;
            this.jockey = jockey;
        }
    }

    /** 馬場（"芝" / "ダ" / "障"）。取れなければ null */
    public static String surface(String raceInfo) {
        Matcher m = raceInfo != null ? COURSE.matcher(raceInfo) : null;
        return m != null && m.find() ? m.group(1) : null;
    }

    /** 距離（m）。取れなければ null */
    public static Integer meters(String raceInfo) {
        Matcher m = raceInfo != null ? COURSE.matcher(raceInfo) : null;
        return m != null && m.find() ? Integer.parseInt(m.group(2)) : null;
    }

    /** 「芝2000」のような短い表記。取れなければ空文字 */
    public static String courseLabel(String raceInfo) {
        String s = surface(raceInfo);
        Integer d = meters(raceInfo);
        return s != null && d != null ? s + d : "";
    }

    /**
     * 今回のレースと近走（新しい順）から印を作る。
     *
     * @param raceDate   今回の開催日 yyyyMMdd
     * @param raceInfo   今回の条件（距離・馬場を含む文字列）
     * @param jockey     今回の騎手（未定なら null）
     * @param runs       近走（新しい順）
     */
    public static List<String> badges(String raceDate, String raceInfo, String jockey, List<Run> runs) {
        List<String> badges = new ArrayList<>();
        if (runs == null || runs.isEmpty()) return badges;
        Run last = runs.get(0);

        // レース間隔
        Long days = daysBetween(last.date, raceDate);
        if (days != null && days > 0) {
            if (days >= LONG_REST_DAYS) {
                badges.add("休み明け");
            } else if (days <= 7) {
                badges.add("連闘");
            } else {
                badges.add("中" + (days / 7 - 1) + "週");
            }
        }

        // 馬場・距離の変更
        String thisSurface = surface(raceInfo);
        String lastSurface = surface(last.raceInfo);
        if (thisSurface != null && lastSurface != null && !thisSurface.equals(lastSurface)) {
            badges.add(lastSurface + "→" + thisSurface);
        }
        Integer thisMeters = meters(raceInfo);
        Integer lastMeters = meters(last.raceInfo);
        if (thisMeters != null && lastMeters != null) {
            int diff = thisMeters - lastMeters;
            if (diff > 0) badges.add("距離延長 +" + diff + "m");
            else if (diff < 0) badges.add("距離短縮 " + diff + "m");
        }

        // 乗り替わり（今回の騎手が決まっているときだけ）
        if (jockey != null && !jockey.isEmpty() && last.jockey != null && !last.jockey.isEmpty()
                && !sameJockey(jockey, last.jockey)) {
            badges.add("乗り替わり");
        }

        // 同じ馬場・距離での成績（1着-2着-3着-着外）
        if (thisSurface != null && thisMeters != null) {
            int[] record = new int[4];
            int count = 0;
            for (Run run : runs) {
                if (!thisSurface.equals(surface(run.raceInfo)) || !thisMeters.equals(meters(run.raceInfo))) continue;
                Integer rank = parseRank(run.rank);
                if (rank == null) continue;
                record[Math.min(rank, 4) - 1]++;
                count++;
            }
            if (count > 0) {
                badges.add("同条件 " + record[0] + "-" + record[1] + "-" + record[2] + "-" + record[3]);
            }
        }
        return badges;
    }

    static Long daysBetween(String from, String to) {
        try {
            return ChronoUnit.DAYS.between(LocalDate.parse(from, YMD), LocalDate.parse(to, YMD));
        } catch (Exception e) {
            return null;
        }
    }

    static Integer parseRank(String rank) {
        if (rank == null) return null;
        try {
            int r = Integer.parseInt(rank.trim());
            return r >= 1 ? r : null;
        } catch (NumberFormatException e) {
            return null; // 中止・除外など
        }
    }

    /**
     * 騎手名の表記ゆれを無視して比べる。
     * 出馬表では「吉村誠」「ルメー」のように先頭3文字に縮められることがあるので、前方一致で同じとみなす。
     */
    static boolean sameJockey(String a, String b) {
        String x = normalizeJockey(a);
        String y = normalizeJockey(b);
        if (x.isEmpty() || y.isEmpty()) return x.equals(y);
        return x.startsWith(y) || y.startsWith(x);
    }

    private static String normalizeJockey(String s) {
        return s.replaceAll("[\\s　▲△☆◇★]", "");
    }
}
