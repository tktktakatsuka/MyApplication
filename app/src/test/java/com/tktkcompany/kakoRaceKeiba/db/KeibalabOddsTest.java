package com.tktkcompany.kakoRaceKeiba.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;

public class KeibalabOddsTest {

    // keibalab の単勝オッズの JSON（京都8R もみじS、一部を省略）。取消の馬は数字でない値になる
    private static final String JSON = "{\"normal\":{\"01\":\"7.3\",\"02\":\"13.8\",\"03\":\"5.3\",\"04\":\"32.3\","
            + "\"05\":\"---\",\"06\":\"3.8\",\"07\":\"1.9\"},\"sort\":{\"07\":\"1.9\"},\"race\":{\"posttm\":\"1345\"}}";

    @Test
    public void readsOddsByHorseNumber() {
        Map<String, String> odds = KeibalabOdds.parse(JSON);
        assertEquals("5.3", KeibalabOdds.oddsOf(odds, "3"));
        assertEquals("5.3", KeibalabOdds.oddsOf(odds, "03"));
        assertNull(KeibalabOdds.oddsOf(odds, "5"));
        assertNull(KeibalabOdds.oddsOf(odds, null));
    }

    @Test
    public void popularityIsTheRankByOdds() {
        Map<String, String> odds = KeibalabOdds.parse(JSON);
        assertEquals(Integer.valueOf(1), KeibalabOdds.popularity(odds, "7"));
        assertEquals(Integer.valueOf(3), KeibalabOdds.popularity(odds, "3"));
        assertEquals(Integer.valueOf(6), KeibalabOdds.popularity(odds, "4"));
        assertNull(KeibalabOdds.popularity(odds, "5"));
    }

    // keibalab のオッズのページの単勝・複勝の表（2頭分と、取消の馬）
    private static final String TABLE = "<table id=\"oddsTanTable\"><thead><tr><th>枠番</th><th>馬番</th><th>馬名</th>"
            + "<th>単勝</th><th colspan=\"3\">複勝</th><th>人気</th></tr></thead><tbody>"
            + "<tr><td>1</td><td>1</td><td>ノヴァーリス</td><td><i>7.3</i></td><td><i>2.7</i></td><td>-</td><td><i>3.3</i></td><td><i>4</i></td></tr>"
            + "<tr><td>3</td><td>3</td><td>アンムート</td><td><i>5.3</i></td><td><i>1.9</i></td><td>-</td><td><i>2.2</i></td><td><i>3</i></td></tr>"
            + "<tr><td>5</td><td>5</td><td>トリケシ</td><td><i>取消</i></td><td><i>-</i></td><td>-</td><td><i>-</i></td><td><i>-</i></td></tr>"
            + "</tbody></table>";

    @Test
    public void readsWinAndPlaceOddsFromTheTable() {
        Map<String, KeibalabOdds.HorseOdds> odds = KeibalabOdds.parseTable(org.jsoup.Jsoup.parse(TABLE));
        assertEquals(2, odds.size());
        assertEquals("単勝 5.3倍（3番人気）　複勝 1.9〜2.2倍", odds.get("3").label());
        assertEquals("単勝 7.3倍（4番人気）　複勝 2.7〜3.3倍", odds.get("1").label());
        assertNull(odds.get("5"));
    }

    @Test
    public void labelWithOnlyWinOdds() {
        assertEquals("単勝 12.0倍", new KeibalabOdds.HorseOdds("12.0", null, null, null).label());
        assertEquals("", new KeibalabOdds.HorseOdds(null, null, null, null).label());
    }

    @Test
    public void brokenResponseGivesNoOdds() {
        assertTrue(KeibalabOdds.parse("<html>メンテナンス中</html>").isEmpty());
        assertTrue(KeibalabOdds.parse("{\"normal\":[]}").isEmpty());
    }
}
