package com.tktkcompany.kakoRaceKeiba.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jsoup.Jsoup;
import org.junit.Test;

import java.util.List;

public class KeibalabResultsTest {

    // keibalab の結果ページの表（見出しの並びは実際のページと同じ。2頭分）
    private static final String PAGE = "<div class=\"raceTitle\">サラ系2歳未勝利</div>"
            + "<div class=\"classCourseSyokin\">[指] 馬齢 ダ1400m 10頭 09:55発走</div>"
            + "<table class=\"DbTable stripe resulttable\"><tr><th>マイ印</th><th>着</th><th>枠</th><th>馬</th><th>馬名</th>"
            + "<th>性齢</th><th>斤量</th><th>騎手</th><th>人</th><th>単勝</th><th>タイム</th><th>着差</th><th>通過順</th>"
            + "<th>上り</th><th>調教師</th><th>馬体重</th></tr>"
            + "<tr><td>…</td><td>1</td><td>2</td><td>2</td><td>ヴェルヴィエ</td><td>牡2</td><td>56.0</td><td>Mデムー</td>"
            + "<td>1</td><td>2.4</td><td>1:23.7</td><td></td><td>①①</td><td>37.0</td><td>[栗]小椋研介</td><td>458(+14)</td></tr>"
            + "<tr><td>…</td><td>2</td><td>8</td><td>9</td><td>(外)ユイリノベーション</td><td>牡2</td><td>56.0</td><td>藤懸貴志</td>"
            + "<td>5</td><td>8.3</td><td>1:24.9</td><td>7</td><td>②②</td><td>38.0</td><td>[栗]平田修</td><td>462(-2)</td></tr>"
            + "</table>";

    @Test
    public void readsTheResultTable() {
        List<SupabaseManager.RaceResult> rows = KeibalabResults.parse(Jsoup.parse(PAGE), "20261017", "京都", 1);
        assertEquals(2, rows.size());
        SupabaseManager.RaceResult first = rows.get(0);
        assertEquals("1", first.getRank());
        assertEquals("ヴェルヴィエ", first.getHorseName());
        assertEquals("2", first.getHorseNumber());
        assertEquals("1", first.getPopular());
        assertEquals("2.4", first.getWinOdds());
        assertEquals("サラ系2歳未勝利", first.getRaceTitle());
        assertEquals("[指] 馬齢 ダ1400m 10頭 09:55発走", first.getHassouTime());
        // 馬名の (外) などの印は外す
        assertEquals("ユイリノベーション", rows.get(1).getHorseName());
    }

    @Test
    public void notFinishedYet() {
        assertTrue(KeibalabResults.parse(Jsoup.parse("<html><table class=\"syutsuba\"></table></html>"),
                "20261017", "京都", 11).isEmpty());
    }
}
