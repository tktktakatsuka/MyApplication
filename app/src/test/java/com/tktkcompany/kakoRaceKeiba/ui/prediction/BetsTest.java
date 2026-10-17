package com.tktkcompany.kakoRaceKeiba.ui.prediction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BetsTest {

    @Test
    public void summaryCountsOnlySettledBets() {
        Bets.Summary s = new Bets.Summary();
        s.add(new Bets.Bet("a", "20261017", "京都", 11, null, "馬連", "5-10", 1000, 40300));
        s.add(new Bets.Bet("b", "20261017", "京都", 11, null, "単勝", "5", 1000, 0));
        s.add(new Bets.Bet("c", "20261017", "京都", 11, null, "ワイド", "5-9", 1000, null));
        assertEquals("投資 2,000円・払戻 40,300円（回収率 2015%）・的中 1/2", s.label());
    }

    @Test
    public void validatesNumbersAndAmount() {
        assertNull(Bets.validate("馬連", "5-10", 500));
        assertNull(Bets.validate("3連単", "10 5 9", 100));
        assertEquals("馬連は2頭（馬番をハイフンで区切って）で入力してください", Bets.validate("馬連", "5", 100));
        assertEquals("同じ馬番が入っています", Bets.validate("馬連", "5-5", 100));
        assertNull(Bets.validate("枠連", "3-3", 100));
        assertEquals("金額は100円単位で入力してください", Bets.validate("単勝", "5", 150));
    }

    @Test
    public void emptySummary() {
        assertEquals("", new Bets.Summary().label());
    }
}
