package com.tktkcompany.kakoRaceKeiba.ui.horse;

import static org.junit.Assert.assertEquals;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class HorseDiscoverTest {

    @Test
    public void onlyGradedRacesG1First() {
        List<HorseDiscover.GradedRace> races = HorseDiscover.gradedRaces(Arrays.asList(
                entry("20261024", 11, "富士S (GII)", "ジャンタルマンタル", false),
                entry("20261025", 11, "菊花賞 (ＧⅠ)", "ダノンデサイル", false),
                entry("20261025", 11, "菊花賞 (ＧⅠ)", "ショウナンラプンタ", false),
                entry("20261025", 11, "菊花賞 (ＧⅠ)", "トリケシ", true),
                entry("20261024", 10, "カシオペアS (L)", "ほかの馬", false)));
        assertEquals(2, races.size());
        assertEquals("菊花賞", races.get(0).title);
        assertEquals(Arrays.asList("ダノンデサイル", "ショウナンラプンタ"), races.get(0).horses);
        assertEquals("富士S", races.get(1).title);
    }

    @Test
    public void popularNeedsTwoFans() {
        List<SupabaseManager.FavoriteRank> popular = HorseDiscover.popular(Arrays.asList(
                new SupabaseManager.FavoriteRank("ドウデュース", 5),
                new SupabaseManager.FavoriteRank("ひとりだけ", 1)), 10);
        assertEquals(1, popular.size());
        assertEquals("ドウデュース", popular.get(0).getHorseName());
    }

    private static SupabaseManager.RaceEntry entry(String date, int raceNo, String title, String name, boolean scratched) {
        return new SupabaseManager.RaceEntry(null, date, "京都", raceNo, title, null, null, null, name,
                null, null, null, null, null, null, null, null, null, null, scratched, null, null, null, null);
    }
}
