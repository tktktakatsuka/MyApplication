package com.tktkcompany.kakoRaceKeiba.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 払い戻し。PC のバッチが結果と一緒に race_payouts に入れたものを読む（アプリから keibalab へは取りに行かない）。
 */
public final class LivePayouts {

    /** 券種（表の並び順） */
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

    private LivePayouts() {
    }

    /** まだ確定していなければ空。必ずバックグラウンドスレッドから呼ぶこと */
    public static List<Payout> fetchBlocking(String date, String course, int raceNo) throws Exception {
        List<Payout> payouts = new ArrayList<>();
        for (SupabaseManager.RacePayout p : SupabaseManager.fetchPayoutsBlocking(date, course, raceNo)) {
            payouts.add(new Payout(p.getBetType(), p.getNumbers(), p.getYen()));
        }
        return payouts;
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
        if (unordered) Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        for (Integer n : list) {
            if (sb.length() > 0) sb.append('-');
            sb.append(n);
        }
        return sb.toString();
    }
}
