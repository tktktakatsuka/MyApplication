package com.tktkcompany.kakoRaceKeiba.util;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 表示の言語。端末の言語が日本語なら日本語、それ以外は英語で出す。
 * 画面の文言は res/values（英語）と res/values-ja（日本語）に置き、
 * Context の無いところ（通知の文面・日付など）はここで切り替える。
 * 馬名・レース名・騎手名はデータが日本語しか無いので、どちらの言語でもそのまま出す。
 */
public final class Lang {

    private static final Map<String, String> COURSES = new HashMap<>();
    private static final Map<String, String> BET_TYPES = new HashMap<>();
    static {
        String[][] courses = {{"札幌", "Sapporo"}, {"函館", "Hakodate"}, {"福島", "Fukushima"}, {"新潟", "Niigata"},
                {"東京", "Tokyo"}, {"中山", "Nakayama"}, {"中京", "Chukyo"}, {"京都", "Kyoto"}, {"阪神", "Hanshin"},
                {"小倉", "Kokura"}};
        for (String[] c : courses) COURSES.put(c[0], c[1]);
        String[][] types = {{"単勝", "Win"}, {"複勝", "Place"}, {"枠連", "Bracket Q"}, {"馬連", "Quinella"},
                {"ワイド", "Wide"}, {"馬単", "Exacta"}, {"3連複", "Trio"}, {"3連単", "Trifecta"}};
        for (String[] t : types) BET_TYPES.put(t[0], t[1]);
    }

    private Lang() {
    }

    /** 日本語で表示するか */
    public static boolean ja() {
        return "ja".equals(Locale.getDefault().getLanguage());
    }

    /** 日本語と英語のどちらかを返す */
    public static String t(String ja, String en) {
        return ja() ? ja : en;
    }

    /** 競馬場名（「東京」→「Tokyo」）。データの照合には使わず、表示にだけ使う */
    public static String course(@Nullable String course) {
        if (course == null || ja()) return course;
        String en = COURSES.get(course);
        return en != null ? en : course;
    }

    /** 券種（「馬連」→「Quinella」） */
    public static String betType(String type) {
        if (ja()) return type;
        String en = BET_TYPES.get(type);
        return en != null ? en : type;
    }

    /** 着順（"3" →「3着」/「3rd」、"取消" →「取消」/「Scratched」） */
    public static String rank(@Nullable String rank) {
        if (rank == null) return "-";
        if (rank.matches("\\d+")) return ja() ? rank + "着" : ordinal(Integer.parseInt(rank));
        if (ja()) return rank;
        switch (rank) {
            case "取消": return "Scratched";
            case "除外": return "Excluded";
            case "中止": return "DNF";
            case "失格": return "DQ";
            default: return rank;
        }
    }

    /** 人気（3 →「3番人気」/「3rd fav」） */
    public static String popularity(Object popular) {
        String p = String.valueOf(popular).trim();
        if (ja()) return p + "番人気";
        return p.matches("\\d+") ? ordinal(Integer.parseInt(p)) + " fav" : p;
    }

    /** 金額（4160 →「4,160円」/「¥4,160」） */
    public static String yen(int yen) {
        return ja() ? String.format("%,d円", yen) : String.format(Locale.US, "¥%,d", yen);
    }

    /** オッズ（"5.3" →「5.3倍」/「5.3」） */
    public static String odds(String odds) {
        return ja() ? odds + "倍" : odds;
    }

    /** 頭数（7 →「7頭」/「7 horses」） */
    public static String horses(int count) {
        return ja() ? count + "頭" : count + (count == 1 ? " horse" : " horses");
    }

    /** レース（"東京", 11 →「東京11R」/「Tokyo R11」） */
    public static String race(@Nullable String course, int raceNo) {
        return ja() ? course + raceNo + "R" : course(course) + " R" + raceNo;
    }

    public static String ordinal(int n) {
        int mod100 = n % 100;
        String suffix = (mod100 >= 11 && mod100 <= 13) ? "th"
                : n % 10 == 1 ? "st" : n % 10 == 2 ? "nd" : n % 10 == 3 ? "rd" : "th";
        return n + suffix;
    }
}
