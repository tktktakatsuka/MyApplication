package com.tktkcompany.kakoRaceKeiba.ui.home;

import static org.junit.Assert.assertEquals;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainRacesTest {

    @Test
    public void grade() {
        assertEquals(MainRaces.G1, MainRaces.grade("秋華賞 (ＧⅠ)"));
        assertEquals(MainRaces.G2, MainRaces.grade("府中牝馬S (GII)"));
        assertEquals(MainRaces.G3, MainRaces.grade("新潟記念(G3)"));
        assertEquals(MainRaces.LISTED, MainRaces.grade("ブラジルカップ (L)"));
        assertEquals(MainRaces.OTHER, MainRaces.grade("オクトーバーS"));
        assertEquals("秋華賞", MainRaces.plainTitle("秋華賞 (ＧⅠ)"));
    }

    @Test
    public void meta() {
        assertEquals("ダ2100m・13頭・15:20発走",
                MainRaces.meta("3歳上オープン (国)(特) ハンデ ダ2100m 13頭 15:20発走 本賞金 2400万"));
    }

    @Test
    public void g1FirstAndOnlyTheLatestDay() {
        List<SupabaseManager.RaceResult> niigata = race("20261018", "新潟", "新潟牝馬S (L)");
        List<SupabaseManager.RaceResult> kyoto = race("20261018", "京都", "秋華賞 (ＧⅠ)");
        List<SupabaseManager.RaceResult> old = race("20261012", "東京", "府中牝馬S (GII)");
        List<List<SupabaseManager.RaceResult>> sorted = MainRaces.sort(new ArrayList<>(Arrays.asList(niigata, old, kyoto)));
        assertEquals(Arrays.asList(kyoto, niigata), sorted);
    }

    private static List<SupabaseManager.RaceResult> race(String date, String course, String title) {
        return Collections.singletonList(new SupabaseManager.RaceResult(null, date, course, 11, title,
                "1", null, null, "テスト", null, null, null, null, null, null, null, null, null, null, null, null));
    }
}
