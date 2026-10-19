package com.tktkcompany.kakoRaceKeiba.ui.horse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.prediction.Predictions;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HorseSearchTest {

    @Test
    public void hiraganaBecomesKatakana() {
        assertEquals("アーモンドアイ", HorseSearch.toKatakana(" あーもんどあい "));
        assertEquals("ドウデュース", HorseSearch.toKatakana("ドウデュース"));
    }

    @Test
    public void exactNameFirstThenUpcomingThenRecent() {
        List<SupabaseManager.RaceResult> results = Arrays.asList(
                result("ドウデュースJr", "20261018", "3"),
                result("ドウデュース", "20261011", "1"),
                result("ドウデュース", "20260901", "2"));
        List<SupabaseManager.RaceEntry> entries = Collections.singletonList(entry("ドウデュースII", "20261024"));
        List<HorseSearch.Hit> hits = HorseSearch.merge("ドウデュース", results, entries);
        assertEquals(Arrays.asList("ドウデュース", "ドウデュースII", "ドウデュースJr"),
                Arrays.asList(hits.get(0).name, hits.get(1).name, hits.get(2).name));
        // 前走はいちばん新しい結果
        assertEquals("20261011", hits.get(0).last.getKaisaibi());
        assertNotNull(hits.get(1).next);
        assertNull(hits.get(1).last);
    }

    @Test
    public void recordAndMarks() {
        List<SupabaseManager.RaceResult> results = Arrays.asList(
                result("アンムート", "20261017", "1"), result("アンムート", "20260920", "3"),
                result("アンムート", "20260830", "取消"), result("アンムート", "20260801", "7"));
        HorseRecord record = HorseRecord.of(results);
        assertEquals(3, record.starts);
        assertEquals("3戦1勝（2着0回・3着1回）　複勝率67%", record.summary());

        Map<String, String> marks = new HashMap<>();
        marks.put("アンムート", "◎");
        marks.put("ほかの馬", "○");
        List<Predictions.Race> races = Arrays.asList(
                new Predictions.Race("20261017", "京都", 8, null, marks),
                new Predictions.Race("20261024", "京都", 8, null, Collections.singletonMap("ほかの馬", "◎")));
        List<HorseRecord.MarkedRace> marked = HorseRecord.marked("アンムート", races, results);
        assertEquals(1, marked.size());
        assertEquals("◎", marked.get(0).mark);
        assertEquals("1", marked.get(0).rank);
    }

    private static SupabaseManager.RaceResult result(String name, String date, String rank) {
        return new SupabaseManager.RaceResult(null, date, "京都", 8, null, rank, null, null, name,
                null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private static SupabaseManager.RaceEntry entry(String name, String date) {
        return new SupabaseManager.RaceEntry(null, date, "京都", 11, null, null, null, null, name,
                null, null, null, null, null, null, null, null, null, null, false, null, null, null, null);
    }
}
