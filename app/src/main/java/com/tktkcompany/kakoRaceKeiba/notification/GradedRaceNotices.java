package com.tktkcompany.kakoRaceKeiba.notification;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.ui.entries.EntryForm;
import com.tktkcompany.kakoRaceKeiba.util.Lang;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 重賞の「まもなく発走」通知。重賞の見分け方と文面づくりだけを置く。
 */
public final class GradedRaceNotices {

    // レース名の「(ＧⅠ)」「(GIII)」「(ＪＧⅡ)」など。全角・半角どちらでも
    private static final Pattern GRADE = Pattern.compile("[(（]\\s*[JＪ]?[GＧ]\\s*([ⅠⅡⅢ]|I{1,3}|[1-3１-３])\\s*[)）]");

    /** 人気順の1頭 */
    public static final class Favorite {
        final int popularity;
        final String horseName;
        final String odds;

        public Favorite(int popularity, String horseName, String odds) {
            this.popularity = popularity;
            this.horseName = horseName;
            this.odds = odds;
        }
    }

    private GradedRaceNotices() {
    }

    public static boolean isGraded(@Nullable String raceTitle) {
        return raceTitle != null && GRADE.matcher(raceTitle).find();
    }

    public static String title(String raceTitle) {
        return "🏆 " + raceTitle.trim() + Lang.t(" まもなく発走", " starts soon");
    }

    /** 「15:45発走 東京11R（芝1600m）」の下に上位人気を並べる */
    public static String body(LocalDateTime start, String course, int raceNo, @Nullable String raceInfo,
                              List<Favorite> favorites, @Nullable String oddsTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(RaceStartNotices.raceHead(start, course, raceNo)).append(RaceStartNotices.courseSuffix(raceInfo));
        for (Favorite f : favorites) {
            sb.append('\n').append(Lang.popularity(f.popularity)).append(' ').append(f.horseName).append(' ').append(Lang.odds(f.odds));
        }
        if (!favorites.isEmpty() && oddsTime != null) sb.append(Lang.t("\nオッズは" + oddsTime + "時点", "\nOdds as of " + oddsTime));
        return sb.toString();
    }
}
