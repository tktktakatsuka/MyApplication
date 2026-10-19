package com.tktkcompany.kakoRaceKeiba.ui.horse;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;
import com.tktkcompany.kakoRaceKeiba.ui.prediction.Predictions;
import com.tktkcompany.kakoRaceKeiba.util.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 1頭の成績のまとめと、自分が付けた印の答え合わせ（画面に依存しない部分） */
public final class HorseRecord {

    /** 自分が印を付けたレース1つ分 */
    public static final class MarkedRace {
        public final Predictions.Race race;
        public final String mark;
        /** そのレースの着順。まだ結果が無ければ null */
        public final String rank;

        MarkedRace(Predictions.Race race, String mark, String rank) {
            this.race = race;
            this.mark = mark;
            this.rank = rank;
        }
    }

    public final int starts;
    public final int wins;
    public final int top2;
    public final int top3;

    private HorseRecord(int starts, int wins, int top2, int top3) {
        this.starts = starts;
        this.wins = wins;
        this.top2 = top2;
        this.top3 = top3;
    }

    /** 結果の行から数える（取消・除外など着順が数字でない行は出走に数えない） */
    public static HorseRecord of(List<SupabaseManager.RaceResult> results) {
        int starts = 0, wins = 0, top2 = 0, top3 = 0;
        for (SupabaseManager.RaceResult r : results) {
            Integer rank = rank(r.getRank());
            if (rank == null) continue;
            starts++;
            if (rank == 1) wins++;
            if (rank <= 2) top2++;
            if (rank <= 3) top3++;
        }
        return new HorseRecord(starts, wins, top2, top3);
    }

    /** 「12戦3勝（2着2回・3着1回）　複勝率50%」。出走が無ければ空文字 */
    public String summary() {
        if (starts == 0) return "";
        int second = top2 - wins;
        int third = top3 - top2;
        long rate = Math.round(top3 * 100.0 / starts);
        return Lang.t(starts + "戦" + wins + "勝（2着" + second + "回・3着" + third + "回）　複勝率" + rate + "%",
                starts + " starts · " + wins + " wins · " + second + " 2nds · " + third + " 3rds · Top 3 " + rate + "%");
    }

    /** この馬に印を付けたレース（新しい順）。results から着順を引く */
    public static List<MarkedRace> marked(String horseName, List<Predictions.Race> races, List<SupabaseManager.RaceResult> results) {
        String key = FavoriteHorseNames.normalize(horseName);
        List<MarkedRace> list = new ArrayList<>();
        for (Predictions.Race race : races) {
            String mark = null;
            for (Map.Entry<String, String> e : race.marks.entrySet()) {
                if (FavoriteHorseNames.normalize(e.getKey()).equals(key)) mark = e.getValue();
            }
            if (mark == null) continue;
            String rank = null;
            for (SupabaseManager.RaceResult r : results) {
                if (race.date.equals(r.getKaisaibi()) && race.course.equals(r.getKaisaijo()) && race.raceNo == r.getRaceNo()) {
                    rank = r.getRank();
                }
            }
            list.add(new MarkedRace(race, mark, rank));
        }
        return list;
    }

    static Integer rank(String rank) {
        if (rank == null) return null;
        try {
            return Integer.parseInt(rank.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
