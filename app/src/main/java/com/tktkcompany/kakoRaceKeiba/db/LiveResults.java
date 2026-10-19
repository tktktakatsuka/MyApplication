package com.tktkcompany.kakoRaceKeiba.db;

import java.util.List;

/**
 * 開催日のレース結果。発走の数分後に PC のバッチ（live）が keibalab から取って race_results に入れたものを読む。
 * アプリから keibalab へは取りに行かない（利用者が増えても keibalab に負担をかけないため）。
 */
public final class LiveResults {

    private LiveResults() {
    }

    /** 1レースの結果（着順の表の並び）。まだ入っていなければ空。必ずバックグラウンドスレッドから呼ぶこと */
    public static List<SupabaseManager.RaceResult> fetchRaceBlocking(String date, String course, int raceNo) throws Exception {
        return SupabaseManager.fetchRaceResultBlocking(date, course, raceNo);
    }
}
