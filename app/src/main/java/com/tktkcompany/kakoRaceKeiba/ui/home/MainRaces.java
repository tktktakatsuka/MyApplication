package com.tktkcompany.kakoRaceKeiba.ui.home;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.util.Lang;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** ホームの「前回の開催のメインレース」の並べ方と表示の文字 */
final class MainRaces {

    static final int G1 = 1, G2 = 2, G3 = 3, LISTED = 4, OTHER = 5;

    private static final Pattern GRADE = Pattern.compile("\\(\\s*(?:G|Jpn)\\s*(III|II|I|3|2|1)\\s*\\)");
    private static final Pattern LISTED_MARK = Pattern.compile("\\(\\s*L\\s*\\)");
    private static final Pattern SURFACE = Pattern.compile("(芝|ダ|障)[^\\s]*?(\\d{3,4})m");
    private static final Pattern HEADS = Pattern.compile("(\\d+)頭");
    private static final Pattern POST = Pattern.compile("(\\d{1,2}:\\d{2})発走");

    private MainRaces() {
    }

    /** レース名の「(ＧⅠ)」「(G2)」「(L)」から格を読む */
    static int grade(@Nullable String title) {
        if (title == null) return OTHER;
        String t = Normalizer.normalize(title, Normalizer.Form.NFKC);
        Matcher m = GRADE.matcher(t);
        if (m.find()) {
            switch (m.group(1)) {
                case "I":
                case "1":
                    return G1;
                case "II":
                case "2":
                    return G2;
                default:
                    return G3;
            }
        }
        return LISTED_MARK.matcher(t).find() ? LISTED : OTHER;
    }

    @Nullable
    static String gradeLabel(int grade) {
        switch (grade) {
            case G1:
                return "G1";
            case G2:
                return "G2";
            case G3:
                return "G3";
            case LISTED:
                return "L";
            default:
                return null;
        }
    }

    /** 「秋華賞 (ＧⅠ)」→「秋華賞」。格はラベルで出すので名前からは外す */
    static String plainTitle(@Nullable String title) {
        if (title == null) return "";
        String t = Normalizer.normalize(title, Normalizer.Form.NFKC);
        t = GRADE.matcher(t).replaceAll("");
        t = LISTED_MARK.matcher(t).replaceAll("");
        return t.trim();
    }

    /** 「3歳上オープン (国)(特) ハンデ ダ2100m 13頭 15:20発走 本賞金…」→「ダ2100m・13頭・15:20発走」 */
    static String meta(@Nullable String info) {
        if (info == null) return "";
        List<String> parts = new ArrayList<>();
        Matcher s = SURFACE.matcher(info);
        if (s.find()) {
            String surface = s.group(1);
            parts.add(Lang.t(surface + s.group(2) + "m",
                    ("芝".equals(surface) ? "Turf " : "ダ".equals(surface) ? "Dirt " : "Jump ") + s.group(2) + "m"));
        }
        Matcher h = HEADS.matcher(info);
        if (h.find()) parts.add(Lang.t(h.group(1) + "頭", h.group(1) + " runners"));
        Matcher p = POST.matcher(info);
        if (p.find()) parts.add(Lang.t(p.group(1) + "発走", "Post " + p.group(1)));
        return String.join(Lang.t("・", " · "), parts);
    }

    /** 競馬場ごとのメインレースの結果を、いちばん新しい開催日の分だけにして、G1 から順に並べる */
    static List<List<SupabaseManager.RaceResult>> sort(List<List<SupabaseManager.RaceResult>> races) {
        String latest = "";
        for (List<SupabaseManager.RaceResult> r : races) {
            if (!r.isEmpty() && r.get(0).getKaisaibi().compareTo(latest) > 0) latest = r.get(0).getKaisaibi();
        }
        List<List<SupabaseManager.RaceResult>> shown = new ArrayList<>();
        for (List<SupabaseManager.RaceResult> r : races) {
            if (!r.isEmpty() && r.get(0).getKaisaibi().equals(latest)) shown.add(r);
        }
        // 同じ格なら元の順（競馬場の並び）のまま
        Collections.sort(shown, (a, b) -> Integer.compare(grade(a.get(0).getRaceTitle()), grade(b.get(0).getRaceTitle())));
        return shown;
    }
}
