package com.tktkcompany.kakoRaceKeiba.ui.prediction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PredictionStatsTest {

    @Test
    public void countsHonmeiFinishes() {
        PredictionStats stats = new PredictionStats();
        stats.add("1");
        stats.add("2");
        stats.add("5");
        stats.add("取消");   // 数えない
        stats.add((String) null);
        assertEquals(3, stats.races);
        assertEquals("◎ 3レース　1着1回・2着以内2回・3着以内2回\n勝率33%・連対率67%・複勝率67%", stats.summary());
    }

    @Test
    public void noRacesNoSummary() {
        assertEquals("", new PredictionStats().summary());
    }

    @Test
    public void usesTheHonmeiOfTheRace() {
        Map<String, String> marks = new LinkedHashMap<>();
        marks.put("アミラル", "○");
        marks.put("ビズロード", "◎");
        Predictions.Race race = new Predictions.Race("20261017", "京都", 7, null, marks);
        Map<String, String> ranks = new HashMap<>();
        ranks.put("アミラル", "1");
        ranks.put("ビズロード", "3");
        PredictionStats stats = new PredictionStats();
        stats.add(race, ranks);
        assertEquals(1, stats.races);
        assertEquals(0, stats.wins);
        assertEquals(1, stats.top3);
    }

    @Test
    public void markOrder() {
        assertEquals("◎", Predictions.nextMark(null));
        assertEquals("○", Predictions.nextMark("◎"));
        assertEquals("△", Predictions.nextMark("▲"));
        assertNull(Predictions.nextMark("△"));
    }
}
