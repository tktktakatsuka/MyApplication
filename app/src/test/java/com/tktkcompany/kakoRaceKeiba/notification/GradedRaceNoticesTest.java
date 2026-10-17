package com.tktkcompany.kakoRaceKeiba.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.threeten.bp.LocalDateTime;

import java.util.Arrays;

public class GradedRaceNoticesTest {

    @Test
    public void detectsGradedRaces() {
        assertTrue(GradedRaceNotices.isGraded("富士ステークス (ＧⅡ)"));
        assertTrue(GradedRaceNotices.isGraded("秋華賞 (ＧⅠ)"));
        assertTrue(GradedRaceNotices.isGraded("京都大賞典(GII)"));
        assertTrue(GradedRaceNotices.isGraded("東京ハイジャンプ (ＪＧⅡ)"));
        assertFalse(GradedRaceNotices.isGraded("オパールステークス (L)"));
        assertFalse(GradedRaceNotices.isGraded("トルマリンステークス"));
        assertFalse(GradedRaceNotices.isGraded(null));
    }

    @Test
    public void bodyListsTheFavorites() {
        assertEquals("🏆 富士ステークス (ＧⅡ) まもなく発走", GradedRaceNotices.title("富士ステークス (ＧⅡ)"));
        assertEquals("15:45発走 東京11R（芝1600m）\n1番人気 ソーロブスト 2.5倍\n2番人気 トナリティ 4.1倍\nオッズは15:15時点",
                GradedRaceNotices.body(LocalDateTime.of(2026, 10, 17, 15, 45), "東京", 11,
                        "3歳上オープン (国)(指) 別定 芝1600m 14頭 15:45発走",
                        Arrays.asList(new GradedRaceNotices.Favorite(1, "ソーロブスト", "2.5"),
                                new GradedRaceNotices.Favorite(2, "トナリティ", "4.1")),
                        "15:15"));
    }
}
