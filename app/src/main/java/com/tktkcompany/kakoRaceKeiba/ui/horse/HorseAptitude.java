package com.tktkcompany.kakoRaceKeiba.ui.horse;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.util.Lang;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 1頭の条件別の成績（芝・ダート別、距離別、競馬場別）。画面に依存しない部分。
 * レースの条件は結果の行の「… ダ1200m 13頭 …」（hassou_time 列）や出馬表の race_info から読む
 */
public final class HorseAptitude {

    private static final Pattern COURSE = Pattern.compile("(芝|ダ|障)[^\\s0-9]*?(\\d{3,4})m");

    /** 条件1つ分の成績 */
    public static final class Row {
        public final String label;
        public int starts, wins, top3;
        /** 次走と同じ条件なら true（画面で目立たせる） */
        public boolean matchesNext;

        Row(String label) {
            this.label = label;
        }

        /** 「1-0-1-2」のような着度数ではなく、出走・勝利・複勝率で出す */
        public String record() {
            return Lang.t(starts + "戦" + wins + "勝", starts + " starts, " + wins + " wins");
        }

        public long top3Rate() {
            return HorseRecord.percent(top3, starts);
        }
    }

    /** 区切り（見出しと行） */
    public static final class Group {
        public final String title;
        public final List<Row> rows = new ArrayList<>();

        Group(String title) {
            this.title = title;
        }
    }

    private HorseAptitude() {
    }

    /** 「芝」「ダート」「障害」。読めなければ null */
    @Nullable
    static String surface(@Nullable String info) {
        if (info == null) return null;
        Matcher m = COURSE.matcher(info);
        if (!m.find()) return null;
        switch (m.group(1)) {
            case "芝":
                return Lang.t("芝", "Turf");
            case "ダ":
                return Lang.t("ダート", "Dirt");
            default:
                return Lang.t("障害", "Jump");
        }
    }

    /** 距離の区分（短距離〜長距離）。読めなければ null */
    @Nullable
    static String distance(@Nullable String info) {
        if (info == null) return null;
        Matcher m = COURSE.matcher(info);
        if (!m.find()) return null;
        int meters = Integer.parseInt(m.group(2));
        if (meters <= 1400) return Lang.t("〜1400m", "≤1400m");
        if (meters <= 1800) return Lang.t("1500〜1800m", "1500–1800m");
        if (meters <= 2200) return Lang.t("1900〜2200m", "1900–2200m");
        return Lang.t("2300m〜", "≥2300m");
    }

    /**
     * 成績を条件ごとにまとめる。next は次走（あれば、同じ条件の行に印を付ける）。
     * 出走の無い区分は出さない
     */
    public static List<Group> of(List<SupabaseManager.RaceResult> results, @Nullable SupabaseManager.RaceEntry next) {
        Map<String, Row> surfaces = new LinkedHashMap<>();
        Map<String, Row> distances = new LinkedHashMap<>();
        Map<String, Row> courses = new LinkedHashMap<>();
        for (SupabaseManager.RaceResult r : results) {
            Integer rank = HorseRecord.rank(r.getRank());
            if (rank == null) continue;
            add(surfaces, surface(r.getHassouTime()), rank);
            add(distances, distance(r.getHassouTime()), rank);
            add(courses, r.getKaisaijo() != null ? Lang.course(r.getKaisaijo()) : null, rank);
        }
        if (next != null) {
            mark(surfaces, surface(next.getRaceInfo()));
            mark(distances, distance(next.getRaceInfo()));
            mark(courses, next.getKaisaijo() != null ? Lang.course(next.getKaisaijo()) : null);
        }
        List<Group> groups = new ArrayList<>();
        addGroup(groups, Lang.t("芝・ダート", "Surface"), surfaces);
        addGroup(groups, Lang.t("距離", "Distance"), distances);
        addGroup(groups, Lang.t("競馬場", "Racecourse"), courses);
        return groups;
    }

    private static void add(Map<String, Row> rows, @Nullable String label, int rank) {
        if (label == null) return;
        Row row = rows.get(label);
        if (row == null) {
            row = new Row(label);
            rows.put(label, row);
        }
        row.starts++;
        if (rank == 1) row.wins++;
        if (rank <= 3) row.top3++;
    }

    private static void mark(Map<String, Row> rows, @Nullable String label) {
        if (label != null && rows.containsKey(label)) rows.get(label).matchesNext = true;
    }

    private static void addGroup(List<Group> groups, String title, Map<String, Row> rows) {
        if (rows.isEmpty()) return;
        Group group = new Group(title);
        group.rows.addAll(rows.values());
        // 出走の多い順
        group.rows.sort((a, b) -> Integer.compare(b.starts, a.starts));
        groups.add(group);
    }
}
