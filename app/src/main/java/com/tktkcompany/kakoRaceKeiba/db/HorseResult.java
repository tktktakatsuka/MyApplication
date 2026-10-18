package com.tktkcompany.kakoRaceKeiba.db;

import com.tktkcompany.kakoRaceKeiba.util.Lang;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

/**
 * raceResult ノードの1頭分の結果。
 */
public class HorseResult {
    public final String key;
    public final String horseName;
    public final String kaisaibi;
    public final String kaisaijo;
    public final String raceNo;
    public final String raceTitle;
    public final String tyaku;
    public final String popular;

    public HorseResult(String key, String horseName, String kaisaibi, String kaisaijo,
                       String raceNo, String raceTitle, String tyaku, String popular) {
        this.key = key;
        this.horseName = horseName;
        this.kaisaibi = kaisaibi;
        this.kaisaijo = kaisaijo;
        this.raceNo = raceNo;
        this.raceTitle = raceTitle;
        this.tyaku = tyaku;
        this.popular = popular;
    }

    /** 例: 「10/18(日) 東京11R ○○ステークス 3着（2番人気）」 */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(nz(WeekendDays.formatShort(kaisaibi))).append(" ").append(Lang.ja() ? nz(kaisaijo) + nz(raceNo) + "R" : Lang.course(kaisaijo) + " R" + nz(raceNo));
        if (raceTitle != null && !raceTitle.isEmpty()) sb.append(" ").append(raceTitle);
        sb.append(" ").append(Lang.rank(tyaku));
        if (popular != null && !popular.isEmpty()) sb.append(Lang.t("（" + popular + "番人気）", " (" + Lang.popularity(popular) + ")"));
        return sb.toString();
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }
}
