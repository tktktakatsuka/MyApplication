package com.tktkcompany.kakoRaceKeiba.ui.horse;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 馬を名前で探す。結果と出走予定から、馬ごとに「前走」と「次走」をまとめる（画面に依存しない部分） */
public final class HorseSearch {

    /** 見つかった1頭 */
    public static final class Hit {
        public final String name;
        @Nullable public SupabaseManager.RaceResult last;
        @Nullable public SupabaseManager.RaceEntry next;

        Hit(String name) {
            this.name = name;
        }
    }

    /** 1回の検索で出す最大の頭数 */
    static final int MAX_HITS = 30;

    private HorseSearch() {
    }

    /** ひらがなをカタカナにする（馬名はカタカナなので「あーもんど」でも見つかるように）。前後の空白も外す */
    public static String toKatakana(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.trim().toCharArray()) {
            sb.append(c >= 'ぁ' && c <= 'ゖ' ? (char) (c + 0x60) : c);
        }
        return sb.toString();
    }

    /**
     * 結果（新しい順）と出走予定（古い順）を馬ごとにまとめる。
     * 名前がぴったり合う馬、出走予定のある馬、最近走った馬の順に並べる
     */
    public static List<Hit> merge(String query, List<SupabaseManager.RaceResult> results, List<SupabaseManager.RaceEntry> entries) {
        Map<String, Hit> hits = new LinkedHashMap<>();
        for (SupabaseManager.RaceResult r : results) {
            String key = FavoriteHorseNames.normalize(r.getHorseName());
            if (key.isEmpty()) continue;
            Hit hit = hits.computeIfAbsent(key, Hit::new);
            if (hit.last == null) hit.last = r;
        }
        for (SupabaseManager.RaceEntry e : entries) {
            String key = FavoriteHorseNames.normalize(e.getHorseName());
            if (key.isEmpty()) continue;
            Hit hit = hits.computeIfAbsent(key, Hit::new);
            if (hit.next == null) hit.next = e;
        }
        String exact = FavoriteHorseNames.normalize(query);
        List<Hit> list = new ArrayList<>(hits.values());
        list.sort((a, b) -> {
            int c = Boolean.compare(b.name.equals(exact), a.name.equals(exact));
            if (c != 0) return c;
            c = Boolean.compare(b.next != null, a.next != null);
            if (c != 0) return c;
            return lastDate(b).compareTo(lastDate(a));
        });
        return list.size() > MAX_HITS ? new ArrayList<>(list.subList(0, MAX_HITS)) : list;
    }

    private static String lastDate(Hit h) {
        return h.last != null ? h.last.getKaisaibi() : "";
    }
}
