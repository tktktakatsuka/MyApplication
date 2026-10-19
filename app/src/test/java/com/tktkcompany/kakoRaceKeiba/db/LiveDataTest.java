package com.tktkcompany.kakoRaceKeiba.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** PC のバッチが Supabase に入れた当日の情報（オッズ・出馬表・払い戻し）の読み方 */
public class LiveDataTest {

    @Test
    public void oddsTableFromEntries() {
        Map<String, LiveOdds.HorseOdds> odds = LiveOdds.table(Arrays.asList(
                entry("01", "7.3", "4", "2.7", "3.3", false, null, null, null),
                entry("3", "5.3", "3", "1.9", "2.2", false, null, null, null),
                entry("5", null, null, null, null, true, null, null, null),
                entry("6", null, null, null, null, false, null, null, null)));
        assertEquals(2, odds.size());
        assertEquals("単勝 7.3倍（4番人気）　複勝 2.7〜3.3倍", odds.get("1").label());
        assertEquals(Integer.valueOf(3), odds.get("3").popularity);
        // 取消の馬・オッズがまだ無い馬は入れない
        assertNull(odds.get("5"));
        assertNull(odds.get("6"));
    }

    @Test
    public void popularityIsTheRankByOdds() {
        Map<String, String> odds = new HashMap<>();
        odds.put("1", "7.3");
        odds.put("3", "5.3");
        odds.put("5", "---");
        odds.put("7", "1.9");
        assertEquals(Integer.valueOf(1), LiveOdds.popularity(odds, "07"));
        assertEquals(Integer.valueOf(3), LiveOdds.popularity(odds, "1"));
        assertNull(LiveOdds.popularity(odds, "5"));
        assertEquals("5.3", LiveOdds.oddsOf(odds, "03"));
        assertNull(LiveOdds.oddsOf(odds, "5"));
    }

    @Test
    public void labelWithOnlyWinOdds() {
        assertEquals("単勝 12.0倍", new LiveOdds.HorseOdds("12.0", null, null, null).label());
        assertEquals("", new LiveOdds.HorseOdds(null, null, null, null).label());
    }

    @Test
    public void raceCardFromEntries() {
        LiveRaceCard.Card card = LiveRaceCard.card(Arrays.asList(
                entry("1", null, null, null, null, false, "490(0)", "柴田裕", "2026-10-18T01:00:00Z"),
                entry("2", null, null, null, null, true, null, "岩田望", "2026-10-18T01:00:00Z"),
                // バッチがまだ当日の情報を入れていない馬は出さない
                entry("3", null, null, null, null, false, null, null, null)));
        assertEquals(2, card.horses.size());
        assertEquals("490(0)", card.horses.get("1").weight);
        assertEquals("柴田裕", card.horses.get("1").jockey);
        assertFalse(card.horses.get("1").scratched);
        assertTrue(card.horses.get("2").scratched);
        assertEquals("天気 晴・馬場 稍重", card.conditionLabel());
    }

    @Test
    public void nothingYetGivesAnEmptyCard() {
        LiveRaceCard.Card card = LiveRaceCard.card(Arrays.asList(entry("1", null, null, null, null, false, null, null, null)));
        assertTrue(card.horses.isEmpty());
        assertEquals("", card.conditionLabel());
    }

    @Test
    public void unorderedBetsMatchInAnyOrder() {
        List<LivePayouts.Payout> payouts = Arrays.asList(
                new LivePayouts.Payout("馬連", "5-10", 4030),
                new LivePayouts.Payout("ワイド", "9-10", 1240),
                new LivePayouts.Payout("3連複", "5-9-10", 9100),
                new LivePayouts.Payout("馬単", "10-5", 8030),
                new LivePayouts.Payout("3連単", "10-5-9", 61650));
        assertEquals(4030, LivePayouts.payoutFor(payouts, "馬連", "10-5"));
        assertEquals(1240, LivePayouts.payoutFor(payouts, "ワイド", "10 9"));
        assertEquals(9100, LivePayouts.payoutFor(payouts, "3連複", "10-9-5"));
        assertEquals(61650, LivePayouts.payoutFor(payouts, "3連単", "10-5-9"));
        // 馬単・3連単は順番どおりでないと外れ
        assertEquals(0, LivePayouts.payoutFor(payouts, "馬単", "5-10"));
        assertEquals(0, LivePayouts.payoutFor(payouts, "3連単", "5-10-9"));
    }

    private static SupabaseManager.RaceEntry entry(String number, String win, String popular, String placeMin, String placeMax,
                                                   boolean scratched, String horseWeight, String cardJockey, String cardUpdatedAt) {
        boolean card = cardUpdatedAt != null;
        return new SupabaseManager.RaceEntry(null, "20261018", "京都", 11, null, null, "1", number, "馬" + number,
                null, null, null, null, win, popular, null, placeMin, placeMax, horseWeight, scratched, cardJockey,
                card ? "晴" : null, card ? "稍重" : null, cardUpdatedAt);
    }
}
