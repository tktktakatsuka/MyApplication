package com.tktkcompany.kakoRaceKeiba.db;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当日のレース結果を keibalab.jp から直接取得する（夜のバッチでの取り込みを待たずに見せるため）。
 * 読み取り方はバッチの RaceResultsJob と同じ。正式な記録は夜のバッチが race_results に入れる。
 */
public final class KeibalabResults {

    private static final String URL_FORMAT = "https://www.keibalab.jp/db/race/%s%s%02d/";
    private static final int TIMEOUT_MS = 10_000;

    private KeibalabResults() {
    }

    /**
     * 1レースの結果（着順の順）。まだ確定していなければ空。
     * ネットワークを使うので、必ずバックグラウンドスレッドから呼ぶこと。
     */
    public static List<SupabaseManager.RaceResult> fetchRaceBlocking(String date, String course, int raceNo) throws IOException {
        String code = KeibalabOdds.courseCode(course);
        if (code == null) return new ArrayList<>();
        Document doc = Jsoup.connect(String.format(URL_FORMAT, date, code, raceNo))
                .userAgent(KeibalabOdds.USER_AGENT).timeout(TIMEOUT_MS).get();
        return parse(doc, date, course, raceNo);
    }

    static List<SupabaseManager.RaceResult> parse(Document doc, String date, String course, int raceNo) {
        List<SupabaseManager.RaceResult> rows = new ArrayList<>();
        Element table = doc.selectFirst("table.resulttable");
        if (table == null) return rows;

        Map<String, Integer> col = new HashMap<>();
        Element headerRow = table.selectFirst("tr");
        if (headerRow == null) return rows;
        Elements headers = headerRow.select("th,td");
        for (int i = 0; i < headers.size(); i++) col.put(headers.get(i).text().trim(), i);

        Element titleEl = doc.selectFirst(".raceTitle");
        Element infoEl = doc.selectFirst(".classCourseSyokin");
        String title = titleEl != null ? titleEl.text().trim() : null;
        String info = infoEl != null ? infoEl.text().trim() : null;

        for (Element tr : table.select("tr")) {
            if (tr == headerRow) continue;
            Elements td = tr.select("td");
            String horse = horseName(cell(td, col, "馬名"));
            if (horse == null) continue;
            rows.add(new SupabaseManager.RaceResult(null, date, course, raceNo, title,
                    cell(td, col, "着"), cell(td, col, "枠"), cell(td, col, "馬"), horse,
                    cell(td, col, "性齢"), cell(td, col, "斤量"), cell(td, col, "騎手"),
                    cell(td, col, "人"), cell(td, col, "単勝"), cell(td, col, "タイム"),
                    cell(td, col, "着差"), cell(td, col, "通過順"), cell(td, col, "上り"),
                    cell(td, col, "調教師"), cell(td, col, "馬体重"), info));
        }
        return rows;
    }

    /** 馬名の先頭の「(外)」「(地)」「[父]」などの印を外す（バッチの取り込みと同じ） */
    @Nullable
    static String horseName(@Nullable String s) {
        if (s == null) return null;
        String t = s.trim().replaceFirst("^[\\(（\\[［][^\\)）\\]］]{1,2}[\\)）\\]］]\\s*", "");
        return t.isEmpty() ? null : t;
    }

    @Nullable
    private static String cell(Elements td, Map<String, Integer> col, String header) {
        Integer i = col.get(header);
        if (i == null || i >= td.size()) return null;
        String t = td.get(i).text().trim();
        return t.isEmpty() || t.equals("-") || t.equals("…") ? null : t;
    }
}
