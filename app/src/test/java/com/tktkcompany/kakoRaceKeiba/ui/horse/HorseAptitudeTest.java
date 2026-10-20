package com.tktkcompany.kakoRaceKeiba.ui.horse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class HorseAptitudeTest {

    @Test
    public void groupsBySurfaceDistanceAndCourse() {
        List<SupabaseManager.RaceResult> results = Arrays.asList(
                result("京都", "1", "3歳上1勝クラス 芝1600m 16頭 13:45発走"),
                result("東京", "3", "3歳上1勝クラス 芝1800m 12頭 14:20発走"),
                result("京都", "8", "3歳上1勝クラス ダ1200m 15頭 12:10発走"),
                result("京都", "取消", "3歳上1勝クラス 芝2000m 10頭 15:00発走"));
        SupabaseManager.RaceEntry next = new SupabaseManager.RaceEntry(null, "20261025", "京都", 11, null,
                "3歳オープン 芝1600m 18頭 15:40発走", null, null, "テスト", null, null, null, null, null, null, null,
                null, null, null, false, null, null, null, null);
        List<HorseAptitude.Group> groups = HorseAptitude.of(results, next);
        assertEquals(3, groups.size());

        HorseAptitude.Row turf = groups.get(0).rows.get(0);
        assertEquals("芝", turf.label);
        assertEquals(2, turf.starts);
        assertEquals(1, turf.wins);
        assertEquals(100, turf.top3Rate());
        assertTrue(turf.matchesNext);
        assertFalse(groups.get(0).rows.get(1).matchesNext);

        // 1600m と 1800m は同じ区分
        assertEquals("1500〜1800m", groups.get(1).rows.get(0).label);
        assertEquals(2, groups.get(1).rows.get(0).starts);
        assertTrue(groups.get(1).rows.get(0).matchesNext);

        assertEquals("京都", groups.get(2).rows.get(0).label);
        assertEquals(2, groups.get(2).rows.get(0).starts);
    }

    private static SupabaseManager.RaceResult result(String course, String rank, String info) {
        return new SupabaseManager.RaceResult(null, "20261018", course, 8, null, rank, null, null, "テスト",
                null, null, null, null, null, null, null, null, null, null, null, info);
    }
}
