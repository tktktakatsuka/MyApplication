package com.tktkcompany.kakoRaceKeiba.db;

import androidx.annotation.Nullable;

import com.tktkcompany.kakoRaceKeiba.util.Lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当日の出馬表（馬体重・出走取消・乗り替わり後の騎手・天気・馬場）。
 * 開催日に PC のバッチ（live）が keibalab から取って race_entries に入れたものを読む。
 * アプリから keibalab へは取りに行かない。
 */
public final class LiveRaceCard {

    /** 1頭分の当日の情報 */
    public static final class Horse {
        /** 「490(+8)」。発表前なら null */
        @Nullable public final String weight;
        /** 出走取消・競走除外 */
        public final boolean scratched;
        /** 今の騎手（略称）。まだ取っていなければ null */
        @Nullable public final String jockey;

        /** 馬番・枠番（先頭の0なし）と馬名 */
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

    private LiveRaceCard() {
    }

    /** 必ずバックグラウンドスレッドから呼ぶこと */
    public static Card fetchBlocking(String date, String course, int raceNo) throws Exception {
        return card(SupabaseManager.fetchRaceEntriesBlocking(date, course, raceNo));
    }

    /** 出馬表の行から作る。バッチがまだ当日の情報を入れていない馬は入れない */
    static Card card(List<SupabaseManager.RaceEntry> entries) {
        Map<String, Horse> horses = new HashMap<>();
        String weather = null;
        String ground = null;
        for (SupabaseManager.RaceEntry e : entries) {
            if (e.getCardUpdatedAt() == null) continue;
            String number = LiveOdds.stripZeros(e.getHorseNumber());
            if (number == null) continue;
            Horse horse = new Horse(e.getHorseWeight(), e.getScratched(), e.getCardJockey());
            horse.number = number;
            horse.waku = LiveOdds.stripZeros(e.getWaku());
            horse.name = e.getHorseName();
            horses.put(number, horse);
            if (weather == null) weather = e.getWeather();
            if (ground == null) ground = e.getGround();
        }
        return new Card(horses, weather, ground);
    }
}
