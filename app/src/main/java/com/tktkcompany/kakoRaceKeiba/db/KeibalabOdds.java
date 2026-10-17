package com.tktkcompany.kakoRaceKeiba.db;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * keibalab.jp から今の単勝オッズを取得する（出走前の通知に載せるため）。
 * keibalab のオッズのページが読み込んでいる JSON を使う。取れなくても通知は出せるよう、失敗は呼び出し側で無視する。
 */
public final class KeibalabOdds {

    private static final String URL_FORMAT =
            "https://ipat-f.keibalab.jp/jquery/odds.html?kind=tan&opdt=%s&rcoursecd=%s&rno=%02d&device=pc";
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Mobile Safari/537.36";
    private static final int TIMEOUT_MS = 10_000;

    /** 競馬場名 → keibalab の競馬場コード */
    private static final Map<String, String> COURSE_CODES = new HashMap<>();
    static {
        String[] names = {"札幌", "函館", "福島", "新潟", "東京", "中山", "中京", "京都", "阪神", "小倉"};
        for (int i = 0; i < names.length; i++) COURSE_CODES.put(names[i], String.format("%02d", i + 1));
    }

    private KeibalabOdds() {
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
