package com.tktkcompany.kakoRaceKeiba.ui.horse;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;
import com.tktkcompany.kakoRaceKeiba.ui.home.MainRaces;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 馬を探す画面で、名前を入れる前に出す「最初の1頭」の候補（画面に依存しない部分）。
 * 今週の重賞（G1・G2・G3）に出る馬を、レースごとにまとめる
 */
public final class HorseDiscover {

    /** 重賞1レース分 */
    public static final class GradedRace {
        public final String date;
        public final String course;
        public final int raceNo;
        public final String title;
        public final int grade;
        public final List<String> horses = new ArrayList<>();

        GradedRace(String date, String course, int raceNo, String title, int grade) {
            this.date = date;
            this.course = course;
            this.raceNo = raceNo;
            this.title = title;
            this.grade = grade;
        }
    }

    private HorseDiscover() {
    }

    /** 出馬表から重賞だけを取り出す。G1 から、同じ格なら開催日の早い順。取消の馬は入れない */
    public static List<GradedRace> gradedRaces(List<SupabaseManager.RaceEntry> entries) {
        Map<String, GradedRace> races = new LinkedHashMap<>();
        for (SupabaseManager.RaceEntry e : entries) {
            int grade = MainRaces.grade(e.getRaceTitle());
            if (grade > MainRaces.G3 || e.getScratched()) continue;
            String key = e.getKaisaibi() + "|" + e.getKaisaijo() + "|" + e.getRaceNo();
            GradedRace race = races.get(key);
            if (race == null) {
                race = new GradedRace(e.getKaisaibi(), e.getKaisaijo(), e.getRaceNo(), MainRaces.plainTitle(e.getRaceTitle()), grade);
                races.put(key, race);
            }
            String name = FavoriteHorseNames.normalize(e.getHorseName());
            if (!name.isEmpty() && !race.horses.contains(name)) race.horses.add(name);
        }
        List<GradedRace> list = new ArrayList<>(races.values());
        list.sort((a, b) -> {
            int c = Integer.compare(a.grade, b.grade);
            if (c != 0) return c;
            c = a.date.compareTo(b.date);
            return c != 0 ? c : Integer.compare(a.raceNo, b.raceNo);
        });
        return list;
    }

    /** みんなのお気に入りは、2人以上が登録している馬だけ出す（1人だけだと「人気」とは言えないため） */
    public static List<SupabaseManager.FavoriteRank> popular(@Nullable List<SupabaseManager.FavoriteRank> ranking, int max) {
        List<SupabaseManager.FavoriteRank> list = new ArrayList<>();
        if (ranking == null) return list;
        for (SupabaseManager.FavoriteRank r : ranking) {
            if (r.getFans() >= 2 && list.size() < max) list.add(r);
        }
        return list;
    }
}
