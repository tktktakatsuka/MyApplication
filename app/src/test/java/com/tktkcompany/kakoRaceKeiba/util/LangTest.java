package com.tktkcompany.kakoRaceKeiba.util;

import static org.junit.Assert.assertEquals;

import com.tktkcompany.kakoRaceKeiba.notification.LiveResultNotices;

import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;

public class LangTest {

    private final Locale original = Locale.getDefault();

    @After
    public void restore() {
        Locale.setDefault(original);
    }

    @Test
    public void japanese() {
        Locale.setDefault(Locale.JAPAN);
        assertEquals("3着", Lang.rank("3"));
        assertEquals("東京11R", Lang.race("東京", 11));
        assertEquals("4,160円", Lang.yen(4160));
        assertEquals("2026年10月17日(土)", WeekendDays.formatShort("20261017"));
    }

    @Test
    public void english() {
        Locale.setDefault(Locale.US);
        assertEquals("3rd", Lang.rank("3"));
        assertEquals("11th", Lang.rank("11"));
        assertEquals("22nd", Lang.rank("22"));
        assertEquals("Scratched", Lang.rank("取消"));
        assertEquals("Tokyo R11", Lang.race("東京", 11));
        assertEquals("Trifecta", Lang.betType("3連単"));
        assertEquals("¥4,160", Lang.yen(4160));
        assertEquals("1st fav", Lang.popularity("1"));
        assertEquals("Oct 17, 2026 (Sat)", WeekendDays.formatShort("20261017"));
    }

    @Test
    public void englishNotification() {
        Locale.setDefault(Locale.US);
        LiveResultNotices.Finish f = new LiveResultNotices.Finish("アンムート", "1", "3", "5.3", "1:21.5");
        assertEquals("★アンムート won! (3rd fav)", LiveResultNotices.title("京都", 8, Collections.singletonList(f)));
        assertEquals("Oct 17, 2026 (Sat) Kyoto R8 もみじステークス\nWin 5.3・1:21.5\n📝 Tap to add a note",
                LiveResultNotices.body("20261017", "京都", 8, "もみじステークス", Collections.singletonList(f)));
    }
}
