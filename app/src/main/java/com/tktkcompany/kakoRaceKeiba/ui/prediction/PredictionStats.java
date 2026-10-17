package com.tktkcompany.kakoRaceKeiba.ui.prediction;

import androidx.annotation.Nullable;

import java.util.Map;

/**
 * ◎の成績（勝率・連対率・複勝率）を数える。結果が出たレースだけを数える。
 * 画面や Firebase に依存しない計算だけを置く。
 */
public final class PredictionStats {

    public int races;
    public int wins;
    public int top2;
    public int top3;

    /** ◎の馬の着順を1レース分足す。着順が数字でなければ（取消・除外など）数えない */
    public void add(@Nullable String honmeiRank) {
        if (honmeiRank == null || !honmeiRank.matches("\\d+")) return;
        int rank = Integer.parseInt(honmeiRank);
        races++;
        if (rank == 1) wins++;
        if (rank <= 2) top2++;
        if (rank <= 3) top3++;
    }

    /** 1レース分の予想と着順（normalize した馬名 → 着順）から◎の着順を足す */
    public void add(Predictions.Race race, Map<String, String> ranks) {
        String honmei = race.honmei();
        if (honmei != null) add(ranks.get(honmei));
    }

    /** 「◎ 5レース 1着2回・2着以内3回・3着以内4回（勝率40%・複勝率80%）」。レースが無ければ空文字 */
    public String summary() {
        if (races == 0) return "";
        return "◎ " + races + "レース　1着" + wins + "回・2着以内" + top2 + "回・3着以内" + top3 + "回"
                + "\n勝率" + percent(wins) + "・連対率" + percent(top2) + "・複勝率" + percent(top3);
    }

    private String percent(int count) {
        return Math.round(count * 100.0 / races) + "%";
    }
}
