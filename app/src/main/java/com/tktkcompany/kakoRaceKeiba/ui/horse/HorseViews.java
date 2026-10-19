package com.tktkcompany.kakoRaceKeiba.ui.horse;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

/** 馬を探す画面・馬カルテで使う、カードと文字の部品 */
final class HorseViews {

    static final int GREEN = 0xFF2E7D32;
    static final int GRAY = 0xFF757575;
    static final int TEXT = 0xFF212121;

    private HorseViews() {
    }

    static int dp(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    /** 角丸のカード。中身は縦に並べる LinearLayout（戻り値の getChildAt(0)） */
    static MaterialCardView card(Context context, int background) {
        MaterialCardView card = new MaterialCardView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(context, 6), 0, dp(context, 6));
        card.setLayoutParams(params);
        card.setRadius(dp(context, 14));
        card.setCardElevation(dp(context, 2));
        card.setCardBackgroundColor(background);
        card.setStrokeWidth(0);
        LinearLayout inner = new LinearLayout(context);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(context, 14);
        inner.setPadding(pad, pad, pad, pad);
        card.addView(inner);
        return card;
    }

    static LinearLayout inner(MaterialCardView card) {
        return (LinearLayout) card.getChildAt(0);
    }

    static TextView text(Context context, CharSequence text, float sizeSp, int color, boolean bold) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        return tv;
    }

    /** 見出し（カードの外に置く、緑の太字） */
    static TextView section(Context context, String title) {
        TextView tv = text(context, title, 15, GREEN, true);
        tv.setPadding(dp(context, 4), dp(context, 14), 0, dp(context, 2));
        return tv;
    }

    /** 着順の色（1着赤・2着青・3着緑・それ以外は黒） */
    static int rankColor(String rank) {
        Integer r = HorseRecord.rank(rank);
        if (r == null) return GRAY;
        if (r == 1) return 0xFFC62828;
        if (r == 2) return 0xFF1565C0;
        if (r == 3) return GREEN;
        return TEXT;
    }
}
