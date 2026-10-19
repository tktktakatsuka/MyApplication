package com.tktkcompany.kakoRaceKeiba.db;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.util.Lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 今の単勝・複勝オッズ。開催日に PC のバッチ（live）が keibalab から取って race_entries に入れたものを読む
 * （発走1時間前からは5分ごと、それより前は30分ごとに更新される）。アプリから keibalab へは取りに行かない。
 */
public final class LiveOdds {

    /** 1頭分の単勝・複勝オッズ */
    public static final class HorseOdds {
        /** 単勝 "5.3"。発売前・取消なら null */
        @Nullable public final String win;
        /** 複勝の下限・上限 "1.9" "2.2"。無ければ null */
        @Nullable public final String placeMin;
        @Nullable public final String placeMax;
        @Nullable public final Integer popularity;

        public HorseOdds(@Nullable String win, @Nullable String placeMin, @Nullable String placeMax,
                         @Nullable Integer popularity) {
            this.win = win;
            this.placeMin = placeMin;
            this.placeMax = placeMax;
            this.popularity = popularity;
        }

        /** 「単勝 5.3倍（3番人気）　複勝 1.9〜2.2倍」。どちらも無ければ空文字 */
        public String label() {
            StringBuilder sb = new StringBuilder();
            boolean ja = Lang.ja();
            if (win != null) {
                sb.append(ja ? "単勝 " + win + "倍" : "Win " + win);
                if (popularity != null) {
                    sb.append(ja ? "（" + popularity + "番人気）" : " (" + Lang.popularity(popularity) + ")");
                }
            }
            if (placeMin != null) {
                if (sb.length() > 0) sb.append('　');
                sb.append(ja ? "複勝 " : "Place ").append(placeMin);
                if (placeMax != null && !placeMax.equals(placeMin)) sb.append(ja ? "〜" : "–").append(placeMax);
                if (ja) sb.append('倍');
            }
            return sb.toString();
        }
    }

    private LiveOdds() {
    }

    /** 馬番（先頭の0なし）→ 単勝・複勝オッズ。発売前なら空。必ずバックグラウンドスレッドから呼ぶこと */
    public static Map<String, HorseOdds> fetchOddsTableBlocking(String date, String course, int raceNo) throws Exception {
        return table(SupabaseManager.fetchRaceEntriesBlocking(date, course, raceNo));
    }

    /** 馬番（先頭の0なし）→ 単勝オッズ。発売前なら空。必ずバックグラウンドスレッドから呼ぶこと */
    public static Map<String, String> fetchWinOddsBlocking(String date, String course, int raceNo) throws Exception {
        Map<String, String> win = new HashMap<>();
        for (Map.Entry<String, HorseOdds> e : fetchOddsTableBlocking(date, course, raceNo).entrySet()) {
            if (e.getValue().win != null) win.put(e.getKey(), e.getValue().win);
        }
        return win;
    }

    /** 出馬表の行から 馬番（先頭の0なし）→ オッズ を作る。取消の馬とオッズの無い馬は入れない */
    static Map<String, HorseOdds> table(List<SupabaseManager.RaceEntry> entries) {
        Map<String, HorseOdds> odds = new HashMap<>();
        for (SupabaseManager.RaceEntry e : entries) {
            String number = stripZeros(e.getHorseNumber());
            if (number == null || e.getScratched()) continue;
            String win = numberOrNull(e.getWinOdds());
            String placeMin = numberOrNull(e.getPlaceOddsMin());
            String placeMax = numberOrNull(e.getPlaceOddsMax());
            if (win == null && placeMin == null) continue;
            Double popular = win != null ? toNumber(e.getPopular()) : null;
            odds.put(number, new HorseOdds(win, placeMin, placeMax, popular != null ? popular.intValue() : null));
        }
        return odds;
    }

    /** オッズの低い順の順位（＝人気）。オッズが数字でなければ（取消など）null */
    @Nullable
    public static Integer popularity(Map<String, String> odds, String horseNumber) {
        Double mine = toNumber(odds.get(stripZeros(horseNumber)));
        if (mine == null) return null;
        int rank = 1;
        for (String value : odds.values()) {
            Double other = toNumber(value);
            if (other != null && other < mine) rank++;
        }
        return rank;
    }

    /** 馬番からオッズを引く（"03" も "3" も同じ馬） */
    @Nullable
    public static String oddsOf(Map<String, String> odds, @Nullable String horseNumber) {
        String number = stripZeros(horseNumber);
        String value = number != null ? odds.get(number) : null;
        return toNumber(value) != null ? value : null;
    }

    @Nullable
    static String stripZeros(@Nullable String number) {
        if (number == null) return null;
        String t = number.trim().replaceFirst("^0+(?=\\d)", "");
        return t.matches("\\d+") ? t : null;
    }

    @Nullable
    private static String numberOrNull(@Nullable String s) {
        return toNumber(s) != null ? s.trim() : null;
    }

    @Nullable
    private static Double toNumber(@Nullable String s) {
        if (s == null) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
