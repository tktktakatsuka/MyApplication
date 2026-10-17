package com.tktkcompany.kakoRaceKeiba.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jsoup.Jsoup;
import org.junit.Test;

import java.util.List;

public class KeibalabRaceDayTest {

    // keibalab の出馬表（見出しの並びは実際のページと同じ。2頭目は出走取消）
    private static final String CARD = "<div class=\"weather_ground fL\"><ul><li class=\"db_sunny fL\">晴</li>"
            + "<li class=\"db_good fL\">稍重</li></ul></div>"
            + "<table summary=\"トルマリンステークスの出馬表\"><thead><tr><th>枠</th><th>馬</th><th>マイ印</th><th>馬名</th>"
            + "<th>性齢</th><th>斤量</th><th>騎手</th><th>単勝</th><th>人気</th><th>馬体重</th><th>調教師</th></tr></thead><tbody>"
            + "<tr class=\"\"><td>1</td><td>1</td><td></td><td data-hsnm=\"ウィンターダフネ\">ウィンターダフネ</td>"
            + "<td>牝6</td><td>56.0</td><td>柴田裕</td><td>34.3</td><td>10</td><td>490(0)</td><td>[栗]橋田宜</td></tr>"
            + "<tr class=\"bgGray\"><td>2</td><td>2</td><td></td><td data-hsnm=\"ジェットシェヴロン\">ジェットシェヴロン</td>"
            + "<td>牡2</td><td>56.0</td><td>岩田望</td><td></td><td></td><td></td><td>[美]宮田敬</td></tr>"
            + "<tr class=\"\"><td>3</td><td>3</td><td></td><td data-hsnm=\"コンドゥイア\">コンドゥイア</td>"
            + "<td>牝4</td><td>56.0</td><td>松若風</td><td>22.8</td><td>8</td><td>502(+8)</td><td>[栗]井上智</td></tr>"
            + "</tbody></table>";

    // 払い戻しの表（実際のページと同じ並び）
    private static final String PAYOUT = "<div class=\"haraimodoshi\"><table class=\"DbTable stripe\">"
            + "<tr><td>単勝</td><td>10</td><td>810円</td><td>馬単</td><td>10-5</td><td>8,030円</td></tr>"
            + "<tr><td>複勝</td><td>10<br />5<br />9</td><td>310円<br />300円<br />220円</td>"
            + "<td>ワイド</td><td>5-10<br />9-10<br />5-9</td><td>1,430円<br />1,240円<br />980円</td></tr>"
            + "<tr><td>枠連</td><td>3-6</td><td>3,470円</td><td>3連複</td><td>5-9-10</td><td>9,100円</td></tr>"
            + "<tr><td>馬連</td><td>5-10</td><td>4,030円</td><td>3連単</td><td>10-5-9</td><td>61,650円</td></tr>"
            + "</table></div>";

    @Test
    public void readsWeightScratchAndConditions() {
        KeibalabRaceCard.Card card = KeibalabRaceCard.parse(Jsoup.parse(CARD));
        assertEquals("490(0)", card.horses.get("1").weight);
        assertEquals("502(+8)", card.horses.get("3").weight);
        assertFalse(card.horses.get("1").scratched);
        assertTrue(card.horses.get("2").scratched);
        assertNull(card.horses.get("2").weight);
        assertEquals("柴田裕", card.horses.get("1").jockey);
        assertEquals("天気 晴・馬場 稍重", card.conditionLabel());
    }

    @Test
    public void readsPayouts() {
        List<KeibalabPayouts.Payout> payouts = KeibalabPayouts.parse(Jsoup.parse(PAYOUT));
        assertEquals(12, payouts.size());
        assertEquals(810, KeibalabPayouts.payoutFor(payouts, "単勝", "10"));
        assertEquals(300, KeibalabPayouts.payoutFor(payouts, "複勝", "5"));
        assertEquals(61650, KeibalabPayouts.payoutFor(payouts, "3連単", "10-5-9"));
    }

    @Test
    public void unorderedBetsMatchInAnyOrder() {
        List<KeibalabPayouts.Payout> payouts = KeibalabPayouts.parse(Jsoup.parse(PAYOUT));
        assertEquals(4030, KeibalabPayouts.payoutFor(payouts, "馬連", "10-5"));
        assertEquals(1240, KeibalabPayouts.payoutFor(payouts, "ワイド", "10 9"));
        assertEquals(9100, KeibalabPayouts.payoutFor(payouts, "3連複", "10-9-5"));
        // 馬単・3連単は順番どおりでないと外れ
        assertEquals(0, KeibalabPayouts.payoutFor(payouts, "馬単", "5-10"));
        assertEquals(0, KeibalabPayouts.payoutFor(payouts, "3連単", "5-10-9"));
    }

    @Test
    public void weightOnlyWhenAnnounced() {
        assertNull(KeibalabRaceCard.weight("計不"));
        assertNull(KeibalabRaceCard.weight(""));
        assertEquals("468(-4)", KeibalabRaceCard.weight("468（－4）"));
        assertEquals("440(初)", KeibalabRaceCard.weight("440(---)"));
    }
}
