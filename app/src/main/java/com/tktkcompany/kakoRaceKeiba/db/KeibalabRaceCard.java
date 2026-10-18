package com.tktkcompany.kakoRaceKeiba.db;

import com.tktkcompany.kakoRaceKeiba.util.Lang;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当日の出馬表のうち、朝の取り込みより後に変わる情報（馬体重・出走取消・騎手・天気・馬場）を keibalab から読む。
 * 出走表を開いたときに取りに行く。
 */
public final class KeibalabRaceCard {

    private static final String URL_FORMAT = "https://www.keibalab.jp/db/race/%s%s%02d/syutsuba.html";
    private static final int TIMEOUT_MS = 10_000;

    /** 1頭分の当日の情報 */
    public static final class Horse {
        /** 「490(+8)」。発表前なら null */
        @Nullable public final String weight;
        /** 出走取消・競走除外 */
        public final boolean scratched;
        /** 今の騎手（略称） */
        @Nullable public final String jockey;

        /** 馬番・枠番（先頭の0なし）と馬名（印なし） */
        public String number = "";
        @Nullable public String waku;
        @Nullable public String name;

        Horse(@Nullable String weight, boolean scratched, @Nullable String jockey) {
            this.weight = weight;
            this.scratched = scratched;
            this.jockey = jockey;
        }
    }

    /** 1レース分 */
    public static final class Card {
        /** 馬番（先頭の0なし）→ 情報 */
        public final Map<String, Horse> horses;
        /** 「晴」「曇」など。発表前なら null */
        @Nullable public final String weather;
        /** 「良」「稍重」など。発表前なら null */
        @Nullable public final String ground;

        Card(Map<String, Horse> horses, @Nullable String weather, @Nullable String ground) {
            this.horses = horses;
            this.weather = weather;
            this.ground = ground;
        }

        /** 「天気 晴・馬場 良」。どちらも無ければ空文字 */
        public String conditionLabel() {
            StringBuilder sb = new StringBuilder();
            if (weather != null) sb.append(Lang.t("天気 ", "Weather ")).append(weather);
            if (ground != null) {
                if (sb.length() > 0) sb.append('・');
                sb.append(Lang.t("馬場 ", "Track ")).append(ground);
            }
            return sb.toString();
        }
    }

    private KeibalabRaceCard() {
    }

    /** ネットワークを使うので、必ずバックグラウンドスレッドから呼ぶこと */
    public static Card fetchBlocking(String date, String course, int raceNo) throws IOException {
        String code = KeibalabOdds.courseCode(course);
        if (code == null) return new Card(new HashMap<>(), null, null);
        Document doc = Jsoup.connect(String.format(URL_FORMAT, date, code, raceNo))
                .userAgent(KeibalabOdds.USER_AGENT).timeout(TIMEOUT_MS).get();
        return parse(doc);
    }

    static Card parse(Document doc) {
        Map<String, Horse> horses = new HashMap<>();
        Element table = doc.selectFirst("table[summary$=出馬表]");
        if (table != null) {
            Map<String, Integer> col = new HashMap<>();
            Elements headers = table.select("thead th");
            if (headers.isEmpty() && table.selectFirst("tr") != null) headers = table.selectFirst("tr").select("th,td");
            for (int i = 0; i < headers.size(); i++) col.put(headers.get(i).text().trim(), i);
            for (Element tr : table.select("tbody tr")) {
                if (tr.selectFirst("td[data-hsnm]") == null) continue;
                Elements td = tr.select("td");
                String number = stripZeros(cell(td, col, "馬"));
                if (number == null) continue;
                // 取消・除外の馬は行が灰色になり、オッズと馬体重が空になる
                boolean scratched = tr.hasClass("bgGray");
                Horse horse = new Horse(weight(cell(td, col, "馬体重")), scratched, cell(td, col, "騎手"));
                horse.number = number;
                horse.waku = stripZeros(cell(td, col, "枠"));
                Element nameCell = tr.selectFirst("td[data-hsnm]");
                horse.name = KeibalabResults.horseName(nameCell != null ? nameCell.attr("data-hsnm") : null);
                horses.put(number, horse);
            }
        }
        List<Element> conditions = doc.select(".weather_ground li");
        String weather = conditions.size() > 0 ? nonEmpty(conditions.get(0).text()) : null;
        String ground = conditions.size() > 1 ? nonEmpty(conditions.get(1).text()) : null;
        return new Card(horses, weather, ground);
    }

    /** 「490(+8)」「490(0)」の形だけを馬体重とみなす（「計不」や空欄は null） */
    @Nullable
    static String weight(@Nullable String s) {
        if (s == null) return null;
        String t = s.replaceAll("\\s+", "").replace('（', '(').replace('）', ')').replace('＋', '+').replace('－', '-');
        // 初出走の馬は増減が「---」になる
        if (t.matches("\\d{3}\\(-+\\)")) return t.replaceAll("\\(-+\\)", "(初)");
        return t.matches("\\d{3}\\([+-]?\\d+\\)") ? t : null;
    }

    @Nullable
    private static String cell(Elements td, Map<String, Integer> col, String header) {
        Integer i = col.get(header);
        if (i == null || i >= td.size()) return null;
        return nonEmpty(td.get(i).text());
    }

    @Nullable
    private static String nonEmpty(@Nullable String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() || t.equals("-") || t.equals("…") ? null : t;
    }

    @Nullable
    private static String stripZeros(@Nullable String number) {
        if (number == null) return null;
        String t = number.trim().replaceFirst("^0+(?=\\d)", "");
        return t.matches("\\d+") ? t : null;
    }
}
