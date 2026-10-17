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

    @Test
    public void brokenResponseGivesNoOdds() {
        assertTrue(KeibalabOdds.parse("<html>メンテナンス中</html>").isEmpty());
        assertTrue(KeibalabOdds.parse("{\"normal\":[]}").isEmpty());
    }
}
