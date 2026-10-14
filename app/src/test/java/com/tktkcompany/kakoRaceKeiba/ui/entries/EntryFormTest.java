package com.tktkcompany.kakoRaceKeiba.ui.entries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntryFormTest {

    private static final String THIS_RACE = "3歳上オープン (国)(特) ハンデ 芝2000m 18頭 15:30発走";

    @Test
    public void surfaceAndDistanceAreParsed() {
        assertEquals("芝", EntryForm.surface(THIS_RACE));
        assertEquals(Integer.valueOf(2000), EntryForm.meters(THIS_RACE));
        assertEquals("ダ1400", EntryForm.courseLabel("馬齢 ダ1400m 16頭 09:50発走"));
        assertEquals("", EntryForm.courseLabel("条件不明"));
    }

    @Test
    public void noRunsGivesNoBadges() {
        assertTrue(EntryForm.badges("20261017", THIS_RACE, "武豊", Collections.emptyList()).isEmpty());
    }

    @Test
    public void intervalDistanceJockeyAndRecord() {
        List<EntryForm.Run> runs = Arrays.asList(
                new EntryForm.Run("20260927", "3歳上オープン 芝1800m 16頭", "2", "横山武史"),
                new EntryForm.Run("20260801", "3歳上オープン 芝2000m 12頭", "1", "横山武史"),
                new EntryForm.Run("20260601", "3歳上オープン 芝2000m 12頭", "5", "横山武史"));

        List<String> badges = EntryForm.badges("20261017", THIS_RACE, "武豊", runs);

        // 9/27 → 10/17 は20日 = 中1週
        assertEquals(Arrays.asList("中1週", "距離延長 +200m", "乗り替わり", "同条件 1-0-0-1"), badges);
    }

    @Test
    public void longRestBackToBackAndSurfaceChange() {
        List<String> rest = EntryForm.badges("20261017", THIS_RACE, null,
                Collections.singletonList(new EntryForm.Run("20260701", "ダ2000m", "3", "武豊")));
        // 108日ぶり・ダートから芝へ。距離は同じで、今回の騎手は未定なので乗り替わりは付けない
        assertEquals(Arrays.asList("休み明け", "ダ→芝"), rest);

        List<String> backToBack = EntryForm.badges("20261017", THIS_RACE, "武豊",
                Collections.singletonList(new EntryForm.Run("20261011", "芝2000m", "中止", "武豊")));
        // 連闘、同距離・同騎手、着順が数字でない走は成績に数えない
        assertEquals(Collections.singletonList("連闘"), backToBack);
    }

    @Test
    public void jockeyMarksAreIgnored() {
        assertTrue(EntryForm.sameJockey("▲長浜鴻緒", "長浜鴻緒"));
        // 出馬表は先頭3文字に縮められる
        assertTrue(EntryForm.sameJockey("吉村誠", "吉村誠之"));
        assertTrue(EntryForm.sameJockey("ルメー", "ルメール"));
        assertFalse(EntryForm.sameJockey("武豊", "横山武史"));
    }
}
