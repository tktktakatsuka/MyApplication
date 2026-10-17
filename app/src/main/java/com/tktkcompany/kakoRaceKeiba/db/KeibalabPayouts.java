package com.tktkcompany.kakoRaceKeiba.db;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * レース結果のページの払い戻し（単勝〜3連単）を keibalab から読む。
 */
public final class KeibalabPayouts {

    private static final String URL_FORMAT = "https://www.keibalab.jp/db/race/%s%s%02d/";
    private static final int TIMEOUT_MS = 10_000;

    /** 券種の並び（表示順） */
    public static final List<String> TYPES = Arrays.asList("単勝", "複勝", "枠連", "馬連", "ワイド", "馬単", "3連複", "3連単");

    /** 払い戻し1件（複勝・ワイドは的中の組み合わせごとに1件） */
    public static final class Payout {
        public final String type;
        /** 「10」「5-10」「10-5-9」 */
        public final String numbers;
        /** 100円あたりの払戻（円） */
        public final int yen;

        public Payout(String type, String numbers, int yen) {
            this.type = type;
            this.numbers = numbers;
            this.yen = yen;
        }
    }

    private KeibalabPayouts() {
    }

    /** まだ確定していなければ空。ネットワークを使うので、必ずバックグラウンドスレッドから呼ぶこと */
    public static List<Payout> fetchBlocking(String date, String course, int raceNo) throws IOException {
        String code = KeibalabOdds.courseCode(course);
        if (code == null) return new ArrayList<>();
        Document doc = Jsoup.connect(String.format(URL_FORMAT, date, code, raceNo))
                .userAgent(KeibalabOdds.USER_AGENT).timeout(TIMEOUT_MS).get();
        return parse(doc);
    }

    /** 表は「券種・組番・払戻」が1行に2組ずつ並ぶ。複勝・ワイドは改行で複数入る */
    static List<Payout> parse(Document doc) {
        List<Payout> payouts = new ArrayList<>();
        Element table = doc.selectFirst(".haraimodoshi table");
        if (table == null) return payouts;
        for (Element tr : table.select("tr")) {
            Elements td = tr.select("td");
            for (int i = 0; i + 2 < td.size(); i += 3) {
                String type = td.get(i).text().trim();
                if (!TYPES.contains(type)) continue;
                List<String> numbers = lines(td.get(i + 1));
                List<String> yens = lines(td.get(i + 2));
                for (int j = 0; j < Math.min(numbers.size(), yens.size()); j++) {
                    Integer yen = yen(yens.get(j));
                    if (yen != null) payouts.add(new Payout(type, numbers.get(j), yen));
                }
            }
        }
        return payouts;
    }

    /** <br> で区切られた行を取り出す */
    private static List<String> lines(Element cell) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (org.jsoup.nodes.Node node : cell.childNodes()) {
            if (node instanceof Element && ((Element) node).tagName().equals("br")) {
                if (current.toString().trim().length() > 0) lines.add(current.toString().trim());
                current.setLength(0);
            } else if (node instanceof TextNode) {
                current.append(((TextNode) node).text());
            } else if (node instanceof Element) {
                current.append(((Element) node).text());
            }
        }
        if (current.toString().trim().length() > 0) lines.add(current.toString().trim());
        return lines;
    }

    @Nullable
    private static Integer yen(String s) {
        String digits = s.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 買い目が当たっていれば、100円あたりの払戻を返す。外れなら 0。
     * 枠連・馬連・ワイド・3連複は順不同、馬単・3連単は順番どおりで照合する
     */
    public static int payoutFor(List<Payout> payouts, String type, String numbers) {
        String key = normalize(type, numbers);
        for (Payout p : payouts) {
            if (p.type.equals(type) && normalize(type, p.numbers).equals(key)) return p.yen;
        }
        return 0;
    }

    /** 「10-5」「5 10」「05→10」などを比べられる形にそろえる */
    static String normalize(String type, String numbers) {
        List<Integer> list = new ArrayList<>();
        for (String part : numbers.split("[^0-9]+")) {
            if (!part.isEmpty()) list.add(Integer.parseInt(part));
        }
        boolean unordered = type.equals("枠連") || type.equals("馬連") || type.equals("ワイド") || type.equals("3連複");
        if (unordered) java.util.Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        for (Integer n : list) {
            if (sb.length() > 0) sb.append('-');
            sb.append(n);
        }
        return sb.toString();
    }
}
