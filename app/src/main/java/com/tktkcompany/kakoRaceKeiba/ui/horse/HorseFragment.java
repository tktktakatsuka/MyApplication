package com.tktkcompany.kakoRaceKeiba.ui.horse;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemoDialog;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemos;
import com.tktkcompany.kakoRaceKeiba.ui.prediction.Predictions;
import com.tktkcompany.kakoRaceKeiba.ui.raceResult.RaceResultsFragment;
import com.tktkcompany.kakoRaceKeiba.util.Lang;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 馬カルテ。1頭の次走・自分のメモ・自分が付けた印の答え合わせ・全成績を1画面にまとめ、お気に入りに登録できる。
 * お気に入り一覧・馬を探す画面から開く
 */
public class HorseFragment extends Fragment {

    /** 馬名（ナビゲーションのラベルにも使う） */
    public static final String ARG_HORSE = "horse";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private String name;
    private LinearLayout content;
    private ProgressBar progress;

    // 読み込んだもの（すべて揃ったら描く）
    private List<SupabaseManager.RaceResult> results;
    private SupabaseManager.RaceEntry next;
    private Set<String> favorites;
    private Map<String, HorseMemos.Memo> memos;
    private List<Predictions.Race> predictions;

    public static Bundle args(String horseName) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_HORSE, FavoriteHorseNames.normalize(horseName));
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        name = getArguments() != null ? getArguments().getString(ARG_HORSE, "") : "";
        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(0xFFF5F5F5);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = HorseViews.dp(context, 12);
        root.setPadding(pad, pad, pad, HorseViews.dp(context, 24));
        progress = new ProgressBar(context);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        pp.topMargin = HorseViews.dp(context, 40);
        root.addView(progress, pp);
        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content);
        scroll.addView(root);
        load();
        return scroll;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void load() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        executor.execute(() -> {
            List<SupabaseManager.RaceResult> loaded = new ArrayList<>();
            SupabaseManager.RaceEntry upcoming = null;
            try {
                loaded = SupabaseManager.fetchResultsForHorsesBlocking(Collections.singletonList(name), null);
                for (SupabaseManager.RaceEntry e : SupabaseManager.fetchUpcomingEntriesBlocking(today)) {
                    if (FavoriteHorseNames.normalize(e.getHorseName()).equals(name)) {
                        upcoming = e;
                        break;
                    }
                }
            } catch (Exception e) {
                Log.w("HorseFragment", "成績の取得に失敗", e);
            }
            List<SupabaseManager.RaceResult> r = loaded;
            SupabaseManager.RaceEntry n = upcoming;
            main.post(() -> {
                results = r;
                next = n;
                render();
            });
        });
        FavoriteHorseNames.load(f -> {
            favorites = new HashSet<>(f);
            render();
        });
        HorseMemos.load(m -> {
            memos = new HashMap<>(m);
            render();
        });
        Predictions.loadAll(p -> {
            predictions = p;
            render();
        });
    }

    private void render() {
        Context context = getContext();
        if (context == null || results == null || favorites == null || memos == null || predictions == null) return;
        progress.setVisibility(View.GONE);
        content.removeAllViews();
        content.addView(header(context));
        if (next != null) content.addView(nextCard(context));
        content.addView(memoCard(context));

        List<HorseRecord.MarkedRace> marked = HorseRecord.marked(name, predictions, results);
        if (!marked.isEmpty()) {
            content.addView(HorseViews.section(context, Lang.t("自分が付けた印", "Your picks")));
            MaterialCardView card = HorseViews.card(context, 0xFFFFFFFF);
            for (HorseRecord.MarkedRace m : marked) HorseViews.inner(card).addView(markRow(context, m));
            content.addView(card);
        }

        content.addView(HorseViews.section(context, Lang.t("成績（2025年10月以降）", "Results (since Oct 2025)")));
        MaterialCardView card = HorseViews.card(context, 0xFFFFFFFF);
        if (results.isEmpty()) {
            HorseViews.inner(card).addView(HorseViews.text(context, Lang.t("まだ成績はありません", "No results yet"), 14, HorseViews.GRAY, false));
        }
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) HorseViews.inner(card).addView(divider(context));
            HorseViews.inner(card).addView(resultRow(context, results.get(i)));
        }
        content.addView(card);
    }

    private View header(Context context) {
        MaterialCardView card = HorseViews.card(context, 0xFFFFFFFF);
        LinearLayout inner = HorseViews.inner(card);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFF1B5E20, 0xFF43A047});
        inner.setBackground(bg);
        int pad = HorseViews.dp(context, 18);
        inner.setPadding(pad, pad, pad, pad);
        inner.addView(HorseViews.text(context, name, 26, 0xFFFFFFFF, true));
        String summary = HorseRecord.of(results).summary();
        if (!summary.isEmpty()) inner.addView(HorseViews.text(context, summary, 14, 0xE6FFFFFF, false));

        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, HorseViews.dp(context, 12), 0, 0);
        MaterialButton favorite = new MaterialButton(context);
        bindFavoriteButton(favorite);
        favorite.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(context, Lang.t("お気に入りを保存できませんでした。通信状態を確かめてください",
                        "Couldn't save the favorite. Check your connection"), Toast.LENGTH_SHORT).show();
                return;
            }
            boolean added = FavoriteHorseNames.toggle(favorites, name);
            bindFavoriteButton(favorite);
            if (added) {
                Toast.makeText(context, Lang.t("出走が決まったら、発走前と結果をお知らせします", "We'll notify you of entries, post time and results"),
                        Toast.LENGTH_LONG).show();
            }
        });
        buttons.addView(favorite, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        inner.addView(buttons);
        return card;
    }

    private void bindFavoriteButton(MaterialButton button) {
        boolean on = favorites.contains(name);
        button.setText(on ? Lang.t("★ お気に入り登録中", "★ Favorite") : Lang.t("☆ お気に入りに登録", "☆ Add to favorites"));
        button.setBackgroundColor(on ? 0xFFFFFFFF : 0xFFFFB300);
        button.setTextColor(on ? 0xFF1B5E20 : 0xFF3E2723);
    }

    private View nextCard(Context context) {
        MaterialCardView card = HorseViews.card(context, 0xFFFFF8E1);
        LinearLayout inner = HorseViews.inner(card);
        inner.addView(HorseViews.text(context, Lang.t("📅 次走", "📅 Next race"), 13, 0xFFE65100, true));
        inner.addView(HorseViews.text(context, HorseSearchFragment.raceLabel(next.getKaisaibi(), next.getKaisaijo(), next.getRaceNo(),
                next.getRaceTitle()), 17, HorseViews.TEXT, true));
        String info = next.getHorseNumber() != null ? Lang.t(next.getHorseNumber() + "番・", "No. " + next.getHorseNumber() + " · ") : "";
        if (next.getJockey() != null) info += next.getJockey();
        if (!info.isEmpty()) inner.addView(HorseViews.text(context, info, 13, HorseViews.GRAY, false));
        inner.addView(HorseViews.text(context, Lang.t("出走表を見る ›", "Open the race card ›"), 14, HorseViews.GREEN, true));
        card.setOnClickListener(v -> FavoriteHorseNames.openEntry(v, next));
        return card;
    }

    private View memoCard(Context context) {
        MaterialCardView card = HorseViews.card(context, 0xFFFFFFFF);
        LinearLayout inner = HorseViews.inner(card);
        HorseMemos.Memo memo = HorseMemos.find(memos, name);
        inner.addView(HorseViews.text(context, Lang.t("📝 メモ", "📝 Note"), 13, HorseViews.GREEN, true));
        if (memo == null) {
            inner.addView(HorseViews.text(context, Lang.t("気づいたことをメモしておけます（押して書く）", "Tap to write a note"), 14, HorseViews.GRAY, false));
        } else {
            inner.addView(HorseViews.text(context, memo.text, 15, HorseViews.TEXT, false));
            if (memo.from != null) inner.addView(HorseViews.text(context, memo.from, 12, HorseViews.GRAY, false));
        }
        card.setOnClickListener(v -> HorseMemoDialog.show(context, memos, name, null, changed -> {
            if (changed == null) memos.remove(name);
            else memos.put(name, changed);
            render();
        }));
        return card;
    }

    private View markRow(Context context, HorseRecord.MarkedRace m) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, HorseViews.dp(context, 4), 0, HorseViews.dp(context, 4));
        row.addView(HorseViews.text(context, m.mark, 20, 0xFFC62828, true));
        TextView label = HorseViews.text(context, "  " + WeekendDays.formatShort(m.race.date) + " " + Lang.race(m.race.course, m.race.raceNo),
                14, HorseViews.TEXT, false);
        row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(HorseViews.text(context, m.rank != null ? Lang.rank(m.rank) : Lang.t("結果待ち", "Pending"), 15,
                m.rank != null ? HorseViews.rankColor(m.rank) : HorseViews.GRAY, true));
        return row;
    }

    private View resultRow(Context context, SupabaseManager.RaceResult r) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, HorseViews.dp(context, 8), 0, HorseViews.dp(context, 8));

        TextView rank = HorseViews.text(context, Lang.rank(r.getRank()), 18, HorseViews.rankColor(r.getRank()), true);
        rank.setMinWidth(HorseViews.dp(context, 56));
        row.addView(rank);

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(HorseViews.text(context, HorseSearchFragment.raceLabel(r.getKaisaibi(), r.getKaisaijo(), r.getRaceNo(), r.getRaceTitle()),
                14, HorseViews.TEXT, true));
        StringBuilder sub = new StringBuilder();
        if (r.getPopular() != null) sub.append(Lang.popularity(r.getPopular()));
        if (r.getJockey() != null) sub.append(sub.length() > 0 ? "・" : "").append(r.getJockey());
        if (r.getTime() != null) sub.append(sub.length() > 0 ? "・" : "").append(r.getTime());
        if (sub.length() > 0) texts.addView(HorseViews.text(context, sub, 12, HorseViews.GRAY, false));
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(HorseViews.text(context, "›", 20, 0xFFBDBDBD, false));

        row.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_raceResults,
                RaceResultsFragment.args(r.getKaisaibi(), r.getKaisaijo(), r.getRaceNo(), r.getHorseName())));
        return row;
    }

    private View divider(Context context) {
        View line = new View(context);
        line.setBackgroundColor(0xFFEEEEEE);
        line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, HorseViews.dp(context, 1))));
        return line;
    }
}
