package com.tktkcompany.kakoRaceKeiba.db;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * keibalab.jp から今のオッズを取得する。
 * 通知には単勝だけの軽い JSON（オッズのページが読み込んでいるもの）を、出走表の画面には単勝・複勝の表を使う。
 * 取れなくても通知や画面は出せるよう、失敗は呼び出し側で無視する。
 */
public final class KeibalabOdds {

    private static final String URL_FORMAT =
            "https://ipat-f.keibalab.jp/jquery/odds.html?kind=tan&opdt=%s&rcoursecd=%s&rno=%02d&device=pc";
    // スマホの User-Agent だとスマホ版のページに転送され、単勝・複勝の表が無いので PC 版として取得する
    static final String USER_AGENT ="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 10_000;

    /** 競馬場名 → keibalab の競馬場コード */
    private static final Map<String, String> COURSE_CODES = new HashMap<>();
    static {
        String[] names = {"札幌", "函館", "福島", "新潟", "東京", "中山", "中京", "京都", "阪神", "小倉"};
        for (int i = 0; i < names.length; i++) COURSE_CODES.put(names[i], String.format("%02d", i + 1));
    }

    private static final String ODDS_PAGE_FORMAT = "https://www.keibalab.jp/db/race/%s%s%02d/odds.html";

    /** 1頭分の単勝・複勝オッズ */
    public static final class HorseOdds {
        /** 単勝 "5.3"。発売前・取消なら null */
        @Nullable public final String win;
        /** 複勝の下限・上限 "1.9" "2.2"。無ければ null */
        @Nullable public final String placeMin;
        @Nullable public final String placeMax;
        @Nullable public final Integer popularity;

        public HorseOdds(@Nullable String win, @Nullable String placeMin, @Nullable String placeMax,
                         @Nullable Integer popularity) {
            this.win = win;
            this.placeMin = placeMin;
            this.placeMax = placeMax;
            this.popularity = popularity;
        }

        /** 「単勝 5.3倍（3番人気）　複勝 1.9〜2.2倍」。どちらも無ければ空文字 */
        public String label() {
            StringBuilder sb = new StringBuilder();
            boolean ja = com.tktkcompany.kakoRaceKeiba.util.Lang.ja();
            if (win != null) {
                sb.append(ja ? "単勝 " + win + "倍" : "Win " + win);
                if (popularity != null) {
                    sb.append(ja ? "（" + popularity + "番人気）" : " (" + com.tktkcompany.kakoRaceKeiba.util.Lang.popularity(popularity) + ")");
                }
            }
            if (placeMin != null) {
                if (sb.length() > 0) sb.append('　');
                sb.append(ja ? "複勝 " : "Place ").append(placeMin);
                if (placeMax != null && !placeMax.equals(placeMin)) sb.append(ja ? "〜" : "–").append(placeMax);
                if (ja) sb.append('倍');
            }
            return sb.toString();
        }
    }

    private KeibalabOdds() {
    }

    /** 競馬場名 → keibalab の競馬場コード（"京都" → "08"）。JRA 以外なら null */
    @Nullable
    static String courseCode(String course) {
        return COURSE_CODES.get(course);
    }

    /**
     * 単勝・複勝オッズの表（keibalab のオッズのページ）を取得する。馬番（先頭の0なし）→ オッズ。
     * ネットワークを使うので、必ずバックグラウンドスレッドから呼ぶこと。
     */
    public static Map<String, HorseOdds> fetchOddsTableBlocking(String date, String course, int raceNo) throws IOException {
        String code = COURSE_CODES.get(course);
        if (code == null) return new HashMap<>();
        Document doc = Jsoup.connect(String.format(ODDS_PAGE_FORMAT, date, code, raceNo))
                .userAgent(USER_AGENT).timeout(TIMEOUT_MS).get();
        return parseTable(doc);
    }

    /** 表の列は 枠番・馬番・馬名・単勝・複勝(下限)・"-"・複勝(上限)・人気 */
    static Map<String, HorseOdds> parseTable(Document doc) {
        Map<String, HorseOdds> odds = new HashMap<>();
        Element table = doc.selectFirst("table#oddsTanTable");
        if (table == null) return odds;
        for (Element tr : table.select("tr")) {
            Elements td = tr.select("td");
            if (td.size() < 8) continue;
            String number = stripZeros(td.get(1).text());
            if (number == null) continue;
            String win = numberOrNull(td.get(3).text());
            String placeMin = numberOrNull(td.get(4).text());
            String placeMax = numberOrNull(td.get(6).text());
            String popularity = numberOrNull(td.get(7).text());
            if (win == null && placeMin == null) continue;
            odds.put(number, new HorseOdds(win, placeMin, placeMax,
                    win != null && popularity != null ? (int) Double.parseDouble(popularity) : null));
        }
        return odds;
    }

    @Nullable
    private static String numberOrNull(String s) {
        String t = s.trim();
        return toNumber(t) != null ? t : null;
    }

    /**
     * 馬番（先頭の0なし）→ 単勝オッズ。競馬場が JRA 以外なら空。
     * ネットワークを使うので、必ずバックグラウンドスレッドから呼ぶこと。
     */
    public static Map<String, String> fetchWinOddsBlocking(String date, String course, int raceNo) throws IOException {
        String code = COURSE_CODES.get(course);
        if (code == null) return new HashMap<>();
        HttpURLConnection conn = (HttpURLConnection) new URL(String.format(URL_FORMAT, date, code, raceNo)).openConnection();
        try {
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Referer", String.format("https://www.keibalab.jp/db/race/%s%s%02d/odds.html", date, code, raceNo));
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("odds: HTTP " + conn.getResponseCode());
            }
            try (InputStream in = conn.getInputStream()) {
                return parse(readAll(in));
            }
        } finally {
            conn.disconnect();
        }
    }

    /** {"normal":{"01":"7.3","02":"13.8",…}} から馬番 → オッズを取り出す */
    static Map<String, String> parse(String json) {
        Map<String, String> odds = new HashMap<>();
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (RuntimeException e) {
            return odds;
        }
        if (!root.isJsonObject()) return odds;
        JsonElement normal = root.getAsJsonObject().get("normal");
        if (normal == null || !normal.isJsonObject()) return odds;
        for (Map.Entry<String, JsonElement> e : ((JsonObject) normal).entrySet()) {
            if (!e.getValue().isJsonPrimitive()) continue;
            String number = stripZeros(e.getKey());
            if (number != null) odds.put(number, e.getValue().getAsString().trim());
        }
        return odds;
    }

    /** オッズの低い順の順位（＝人気）。オッズが数字でなければ（取消など）null */
    @Nullable
    public static Integer popularity(Map<String, String> odds, String horseNumber) {
        Double mine = toNumber(odds.get(stripZeros(horseNumber)));
        if (mine == null) return null;
        int rank = 1;
        for (String value : odds.values()) {
            Double other = toNumber(value);
            if (other != null && other < mine) rank++;
        }
        return rank;
    }

    /** 馬番からオッズを引く（"03" も "3" も同じ馬） */
    @Nullable
    public static String oddsOf(Map<String, String> odds, @Nullable String horseNumber) {
        String number = stripZeros(horseNumber);
        String value = number != null ? odds.get(number) : null;
        return toNumber(value) != null ? value : null;
    }

    @Nullable
    private static String stripZeros(@Nullable String number) {
        if (number == null) return null;
        String t = number.trim().replaceFirst("^0+(?=\\d)", "");
        return t.matches("\\d+") ? t : null;
    }

    @Nullable
    private static Double toNumber(@Nullable String s) {
        if (s == null) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
