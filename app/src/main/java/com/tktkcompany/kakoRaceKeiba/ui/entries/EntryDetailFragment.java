package com.tktkcompany.kakoRaceKeiba.ui.entries;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.ads.RecentRunsReward;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentEntriesBinding;
import com.tktkcompany.kakoRaceKeiba.db.KeibalabOdds;
import com.tktkcompany.kakoRaceKeiba.db.KeibalabPayouts;
import com.tktkcompany.kakoRaceKeiba.db.KeibalabRaceCard;
import com.tktkcompany.kakoRaceKeiba.db.KeibalabResults;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.notification.FavoriteResultWorker;
import com.tktkcompany.kakoRaceKeiba.notification.RaceStartNotices;
import com.tktkcompany.kakoRaceKeiba.ui.prediction.Bets;
import com.tktkcompany.kakoRaceKeiba.ui.prediction.Predictions;
import com.tktkcompany.kakoRaceKeiba.ui.raceResult.RaceResultsFragment;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemoDialog;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemos;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
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

/** 1レース分の出走馬と、それぞれの近走成績・予想の手がかりを表示する */
public class EntryDetailFragment extends Fragment {

    // 近走を10走まで広げるリワード広告（画面を開いたときに読み込んでおく）
    private final RecentRunsReward reward = new RecentRunsReward();
    // 読み込んだ近走（10走分）。広告を見たあとに読み直さずに表示を広げるため
    private Map<String, List<SupabaseManager.RaceResult>> lastResults = new HashMap<>();

    // 枠番ごとの色（1白 2黒 3赤 4青 5黄 6緑 7橙 8桃）
    private static final int[] WAKU_BACKGROUND = {
            0xFFFFFFFF, 0xFF222222, 0xFFE53935, 0xFF1E88E5,
            0xFFFDD835, 0xFF43A047, 0xFFFB8C00, 0xFFF06292};
    private static final int[] WAKU_TEXT = {
            0xFF222222, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
            0xFF222222, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};

    private FragmentEntriesBinding binding;
    private String raceDate;
    // メモを書いた場面として残す「10/17(土) 京都9R もみじステークス」
    private String raceLabel;
    private Set<String> favorites = new HashSet<>();
    private Map<String, HorseMemos.Memo> memos = new HashMap<>();
    // 馬番 → オッズを出す欄（オッズは出走表を出したあとに読み込む）
    private final Map<String, TextView> oddsViews = new HashMap<>();
    // 自分の予想の印（normalize した馬名 → 印）と、印・結果を出す欄
    private Map<String, String> marks = new HashMap<>();
    private final Map<String, TextView> markViews = new HashMap<>();
    private final Map<String, TextView> resultViews = new HashMap<>();
    // 馬番（先頭の0なし）→ 馬のカード（当日の馬体重・取消を出すため）
    private final Map<String, View> horseCards = new HashMap<>();
    // このレースの買い目と払い戻し
    @Nullable private LinearLayout betsSection;
    @Nullable private SupabaseManager.RaceEntry raceForBets;
    private List<Bets.Bet> raceBets = new ArrayList<>();
    private List<KeibalabPayouts.Payout> payouts = new ArrayList<>();
    private static final ExecutorService ODDS_EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntriesBinding.inflate(inflater, container, false);

        Bundle args = getArguments();
        String date = args != null ? args.getString(EntriesFragment.ARG_DATE) : null;
        String course = args != null ? args.getString(EntriesFragment.ARG_COURSE) : null;
        int raceNo = args != null ? args.getInt(EntriesFragment.ARG_RACE_NO) : 0;
        if (date == null || course == null) return binding.getRoot();

        raceDate = date;
        setScreenTitle(WeekendDays.formatShort(date) + " " + course + raceNo + "R");
        String raceTitle = args.getString(EntriesFragment.ARG_RACE_TITLE, "");
        raceLabel = (WeekendDays.formatShort(date) + " " + course + raceNo + "R " + raceTitle.trim()).trim();
        binding.textEntriesLead.setText(raceTitle);
        loadEntries(date, course, raceNo);
        reward.preload(requireContext());
        return binding.getRoot();
    }

    private void setScreenTitle(String title) {
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) actionBar.setTitle(title);
        }
    }

    private void loadEntries(String date, String course, int raceNo) {
        SupabaseManager.fetchRaceEntries(date, course, raceNo, new SupabaseManager.SupabaseCallback<List<SupabaseManager.RaceEntry>>() {
            @Override
            public void onSuccess(List<SupabaseManager.RaceEntry> entries) {
                if (binding == null || !isAdded()) return;
                if (entries.isEmpty()) {
                    binding.progressBar.setVisibility(View.GONE);
                    showMessage("出走馬のデータがありません");
                    return;
                }
                showHeader(entries);
                FavoriteHorseNames.load(loaded -> {
                    if (binding == null || !isAdded()) return;
                    favorites = new HashSet<>(loaded);
                    HorseMemos.load(loadedMemos -> {
                        if (binding == null || !isAdded()) return;
                        memos = new HashMap<>(loadedMemos);
                        Predictions.loadRace(date, course, raceNo, loadedMarks -> {
                            if (binding == null || !isAdded()) return;
                            marks = loadedMarks;
                            loadPastResults(entries);
                        });
                    });
                });
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.e("EntryDetailFragment", "Supabase load failed: " + message);
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                showMessage("出走馬の読み込みに失敗しました");
            }
        });
    }

    /** レース名・条件・枠順確定の状況を画面上部に出す */
    private void showHeader(List<SupabaseManager.RaceEntry> entries) {
        SupabaseManager.RaceEntry first = entries.get(0);
        SpannableStringBuilder text = new SpannableStringBuilder();
        String title = first.getRaceTitle() != null ? first.getRaceTitle() : "";
        text.append(title, new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        String conditions = conditions(first.getRaceInfo());
        if (!conditions.isEmpty()) text.append("\n").append(conditions);

        text.append("\n");
        if (isBeforeDraw(entries)) {
            text.append("枠順確定前（特別登録 " + entries.size() + "頭）。出走できる頭数を超える場合は抽選・除外があります");
        } else {
            text.append(entries.size() + "頭立て");
        }
        text.append("\n各馬の近走" + RecentRunsReward.runs(requireContext()) + "走（新しい順）。行をタップするとそのレースの結果を表示します");
        binding.textEntriesLead.setText(text);
        binding.textEntriesLead.setTextColor(0xFF333333);
    }

    /** 「1勝クラス　(混) 定量 芝2200m 34頭 発走」から頭数と「発走」を除いた条件を返す */
    static String conditions(String raceInfo) {
        if (raceInfo == null) return "";
        return raceInfo.replaceAll("\\s*\\d+頭", "")
                .replaceAll("\\s*発走.*$", "")
                .replaceAll("[\\s　]+", " ")
                .trim();
    }

    private static boolean isBeforeDraw(List<SupabaseManager.RaceEntry> entries) {
        for (SupabaseManager.RaceEntry e : entries) {
            if (e.getHorseNumber() != null) return false;
        }
        return true;
    }

    private void loadPastResults(List<SupabaseManager.RaceEntry> entries) {
        List<String> names = new ArrayList<>();
        for (SupabaseManager.RaceEntry entry : entries) names.add(entry.getHorseName());

        SupabaseManager.fetchRecentResultsForHorses(names, RecentRunsReward.REWARDED_RUNS, new SupabaseManager.SupabaseCallback<Map<String, List<SupabaseManager.RaceResult>>>() {
            @Override
            public void onSuccess(Map<String, List<SupabaseManager.RaceResult>> results) {
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                render(entries, results);
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.e("EntryDetailFragment", "Past results load failed: " + message);
                if (binding == null || !isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                // 成績が取れなくても出走馬の一覧は出す
                render(entries, Collections.emptyMap());
            }
        });
    }

    private void showMessage(String message) {
        binding.textEntriesEmpty.setText(message);
        binding.textEntriesEmpty.setVisibility(View.VISIBLE);
    }

    private void render(List<SupabaseManager.RaceEntry> entries, Map<String, List<SupabaseManager.RaceResult>> results) {
        lastResults = results;
        LayoutInflater inflater = getLayoutInflater();
        binding.entriesContainer.removeAllViews();
        oddsViews.clear();
        markViews.clear();
        resultViews.clear();
        horseCards.clear();
        // 4走以上ある馬が1頭もいなければ、広告を見ても増えないのでボタンを出さない
        boolean anyHidden = false;
        for (List<SupabaseManager.RaceResult> runs : results.values()) {
            if (runs.size() > RecentRunsReward.NORMAL_RUNS) anyHidden = true;
        }
        if (anyHidden || RecentRunsReward.isUnlocked(requireContext())) {
            binding.entriesContainer.addView(createRewardView(entries));
        }
        // 買い目の記録（このレースの分）と、確定後の払い戻し
        betsSection = new LinearLayout(requireContext());
        betsSection.setOrientation(LinearLayout.VERTICAL);
        binding.entriesContainer.addView(betsSection);
        raceForBets = entries.get(0);
        loadBets();
        for (SupabaseManager.RaceEntry entry : entries) {
            View card = inflater.inflate(R.layout.item_entry_horse, binding.entriesContainer, false);

            TextView number = card.findViewById(R.id.text_entry_number);
            if (entry.getHorseNumber() != null) {
                number.setText(entry.getHorseNumber());
                styleWaku(number, entry.getWaku());
            } else {
                // 枠順確定前は馬番の枠を出さない
                number.setVisibility(View.GONE);
            }

            TextView name = card.findViewById(R.id.text_entry_horse);
            name.setText(entry.getHorseName());
            bindFavorite(card, entry.getHorseName());
            bindMemo(card, entry.getHorseName());
            bindMark(card, entry);
            resultViews.put(FavoriteHorseNames.normalize(entry.getHorseName()), card.findViewById(R.id.text_entry_result));

            List<String> right = new ArrayList<>();
            if (entry.getSexAge() != null && !entry.getSexAge().endsWith("0")) right.add(entry.getSexAge());
            if (entry.getJockey() != null) right.add(entry.getJockey());
            if (entry.getWeight() != null) right.add(entry.getWeight());
            ((TextView) card.findViewById(R.id.text_entry_jockey)).setText(android.text.TextUtils.join(" ", right));

            List<SupabaseManager.RaceResult> runs = results.get(entry.getHorseName());
            showBadges(card.findViewById(R.id.entry_badges), entry, runs);

            LinearLayout pastContainer = card.findViewById(R.id.entry_past_results);
            if (runs == null || runs.isEmpty()) {
                TextView none = new TextView(requireContext());
                none.setText("過去の成績データはありません");
                none.setTextSize(12);
                none.setTextColor(0xFF999999);
                none.setPadding(0, 6, 0, 6);
                pastContainer.addView(none);
            } else {
                // 印の計算には取れた分を全部使い、表示はふだん3走（広告を見たら10走）にする
                int shown = Math.min(runs.size(), RecentRunsReward.runs(requireContext()));
                for (SupabaseManager.RaceResult run : runs.subList(0, shown)) {
                    pastContainer.addView(createPastRunView(inflater, pastContainer, run));
                }
                if (runs.size() > shown) {
                    TextView more = new TextView(requireContext());
                    more.setText("ほか" + (runs.size() - shown) + "走（上のボタンから広告を見ると表示）");
                    more.setTextSize(12);
                    more.setTextColor(0xFF1565C0);
                    more.setPadding(0, 6, 0, 6);
                    pastContainer.addView(more);
                }
            }
            binding.entriesContainer.addView(card);
            if (entry.getHorseNumber() != null) {
                String no = entry.getHorseNumber().trim().replaceFirst("^0+(?=\\d)", "");
                oddsViews.put(entry.getHorseNumber().trim(), card.findViewById(R.id.text_entry_odds));
                horseCards.put(no, card);
            }
        }
        // 行がフォーカスを取ってスクロール位置がずれないよう、先頭から表示する
        binding.getRoot().post(() -> {
            if (binding != null) binding.getRoot().scrollTo(0, 0);
        });
        loadOdds(entries);
        loadRaceCard(entries);
        loadTodayResult(entries.get(0));
    }

    /**
     * 当日の馬体重・出走取消・騎手変更と、天気・馬場を keibalab から取って出す。
     * 朝の取り込みより後の変更を反映するため、出走表を開くたびに取りに行く。枠順確定前は何もしない
     */
    private void loadRaceCard(List<SupabaseManager.RaceEntry> entries) {
        if (horseCards.isEmpty()) return;
        SupabaseManager.RaceEntry first = entries.get(0);
        ODDS_EXECUTOR.execute(() -> {
            KeibalabRaceCard.Card card;
            try {
                card = KeibalabRaceCard.fetchBlocking(first.getKaisaibi(), first.getKaisaijo(), first.getRaceNo());
            } catch (Exception e) {
                Log.w("EntryDetailFragment", "当日の出馬表の取得に失敗", e);
                return;
            }
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (binding == null || !isAdded()) return;
                showRaceCard(entries, card);
            });
        });
    }

    private void showRaceCard(List<SupabaseManager.RaceEntry> entries, KeibalabRaceCard.Card card) {
        String condition = card.conditionLabel();
        if (!condition.isEmpty()) binding.textEntriesLead.append("\n" + condition);
        int scratchedCount = 0;
        for (SupabaseManager.RaceEntry entry : entries) {
            if (entry.getHorseNumber() == null) continue;
            String no = entry.getHorseNumber().trim().replaceFirst("^0+(?=\\d)", "");
            View view = horseCards.get(no);
            KeibalabRaceCard.Horse horse = card.horses.get(no);
            if (view == null || horse == null) continue;
            TextView today = view.findViewById(R.id.text_entry_today);
            SpannableStringBuilder text = new SpannableStringBuilder();
            if (horse.scratched) {
                scratchedCount++;
                appendStyled(text, "出走取消・除外", 0xFFC62828);
                // 取り消された馬は薄く表示し、オッズは出さない
                view.setAlpha(0.45f);
                view.findViewById(R.id.text_entry_odds).setVisibility(View.GONE);
            } else {
                if (horse.weight != null) {
                    text.append("馬体重 ");
                    appendStyled(text, horse.weight, weightColor(horse.weight));
                }
                String jockeyBefore = entry.getJockey() != null ? entry.getJockey().trim() : null;
                if (horse.jockey != null && jockeyBefore != null && !sameJockey(jockeyBefore, horse.jockey)) {
                    if (text.length() > 0) text.append("　");
                    appendStyled(text, "騎手変更 " + jockeyBefore + "→" + horse.jockey, 0xFFE65100);
                }
            }
            if (text.length() == 0) continue;
            today.setText(text);
            today.setVisibility(View.VISIBLE);
        }
        if (scratchedCount > 0) binding.textEntriesLead.append("\n出走取消・除外 " + scratchedCount + "頭");
    }

    private void loadBets() {
        if (!Bets.isSignedIn() || raceForBets == null) {
            renderBets();
            return;
        }
        SupabaseManager.RaceEntry race = raceForBets;
        Bets.loadAll(all -> {
            if (binding == null || !isAdded()) return;
            List<Bets.Bet> mine = new ArrayList<>();
            for (Bets.Bet b : all) {
                if (b.sameRace(race.getKaisaibi(), race.getKaisaijo(), race.getRaceNo())) mine.add(b);
            }
            raceBets = payouts.isEmpty() ? mine : Bets.settle(mine, payouts);
            renderBets();
        });
    }

    /** 買い目の一覧・記録ボタン・払い戻しの表を出し直す */
    private void renderBets() {
        if (betsSection == null || binding == null) return;
        betsSection.removeAllViews();
        float density = getResources().getDisplayMetrics().density;

        if (!payouts.isEmpty()) betsSection.addView(createPayoutView(density));

        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (12 * density);
        box.setPadding(pad, (int) (8 * density), pad, (int) (8 * density));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(10 * density);
        bg.setColor(0xFFF5F5F5);
        box.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, (int) (8 * density));
        box.setLayoutParams(params);

        Bets.Summary summary = new Bets.Summary();
        for (Bets.Bet b : raceBets) {
            summary.add(b);
            TextView row = new TextView(requireContext());
            SpannableStringBuilder text = new SpannableStringBuilder("🎫 " + b.label());
            if (b.isSettled()) {
                text.append("　");
                if (b.payout > 0) appendStyled(text, "的中 " + String.format("%,d", b.payout) + "円", 0xFFC62828);
                else text.append("はずれ");
            }
            row.setText(text);
            row.setTextSize(14);
            row.setTextColor(0xFF333333);
            row.setPadding(0, (int) (4 * density), 0, (int) (4 * density));
            row.setOnLongClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setMessage(b.label() + " を消しますか？")
                        .setPositiveButton("消す", (d, w) -> {
                            Bets.delete(b);
                            raceBets.remove(b);
                            renderBets();
                        })
                        .setNegativeButton("キャンセル", null)
                        .show();
                return true;
            });
            box.addView(row);
        }
        if (summary.count > 0) {
            TextView total = new TextView(requireContext());
            total.setText(summary.label());
            total.setTextSize(13);
            total.setTextColor(0xFF555555);
            box.addView(total);
        }
        TextView add = new TextView(requireContext());
        add.setText(raceBets.isEmpty() ? "🎫 買い目を記録する（記録だけで、馬券は買いません）" : "＋ 買い目を追加（長押しで消す）");
        add.setTextSize(14);
        add.setTextColor(0xFF1565C0);
        add.setPadding(0, (int) (6 * density), 0, (int) (6 * density));
        add.setOnClickListener(v -> showAddBetDialog());
        box.addView(add);
        betsSection.addView(box);
    }

    /** 払い戻しの表（券種ごとに1行） */
    private View createPayoutView(float density) {
        TextView view = new TextView(requireContext());
        SpannableStringBuilder text = new SpannableStringBuilder();
        appendStyled(text, "払い戻し", 0xFF333333);
        String lastType = null;
        for (KeibalabPayouts.Payout p : payouts) {
            if (!p.type.equals(lastType)) {
                text.append("\n").append(p.type).append("　");
                lastType = p.type;
            } else {
                text.append("　");
            }
            text.append(p.numbers).append(" ").append(String.format("%,d", p.yen)).append("円");
        }
        view.setText(text);
        view.setTextSize(13);
        view.setTextColor(0xFF333333);
        view.setLineSpacing(0, 1.2f);
        int pad = (int) (12 * density);
        view.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(10 * density);
        bg.setColor(0xFFFFFDE7);
        bg.setStroke((int) Math.max(1, density), 0xFFFBC02D);
        view.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, (int) (8 * density));
        view.setLayoutParams(params);
        return view;
    }

    private void showAddBetDialog() {
        if (!Bets.isSignedIn()) {
            Toast.makeText(getContext(), "買い目を記録するにはログインが必要です", Toast.LENGTH_SHORT).show();
            return;
        }
        if (raceForBets == null) return;
        float density = getResources().getDisplayMetrics().density;
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * density);
        form.setPadding(pad, (int) (8 * density), pad, 0);

        android.widget.Spinner type = new android.widget.Spinner(requireContext());
        type.setAdapter(new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, KeibalabPayouts.TYPES));
        form.addView(type);
        android.widget.EditText numbers = new android.widget.EditText(requireContext());
        numbers.setHint("馬番（例: 5-10。枠連は枠番）");
        numbers.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        form.addView(numbers);
        android.widget.EditText amount = new android.widget.EditText(requireContext());
        amount.setHint("金額（円）");
        amount.setText("100");
        amount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        form.addView(amount);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("買い目を記録")
                .setView(form)
                .setPositiveButton("記録", null)
                .setNegativeButton("キャンセル", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String t = (String) type.getSelectedItem();
            String n = numbers.getText().toString().trim();
            int yen;
            try {
                yen = Integer.parseInt(amount.getText().toString().trim());
            } catch (NumberFormatException e) {
                yen = 0;
            }
            String error = Bets.validate(t, n, yen);
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                return;
            }
            SupabaseManager.RaceEntry r = raceForBets;
            Bets.add(r.getKaisaibi(), r.getKaisaijo(), r.getRaceNo(), r.getRaceTitle(), t, n, yen);
            dialog.dismiss();
            loadBets();
        }));
        dialog.show();
    }

    /** 増減が大きい（±10kg以上）ときは目立たせる */
    private static int weightColor(String weight) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(([+-]?\\d+)\\)").matcher(weight);
        if (m.find() && Math.abs(Integer.parseInt(m.group(1))) >= 10) return 0xFFC62828;
        return 0xFF333333;
    }

    /** 出馬表の騎手名は略称で、取り込み時と同じとは限らないので、片方がもう片方で始まれば同じ騎手とみなす */
    static boolean sameJockey(String a, String b) {
        String x = a.replaceAll("[\\s▲△☆★◇]", ""), y = b.replaceAll("[\\s▲△☆★◇]", "");
        return x.startsWith(y) || y.startsWith(x);
    }

    private static void appendStyled(SpannableStringBuilder sb, String text, int color) {
        int from = sb.length();
        sb.append(text);
        sb.setSpan(new android.text.style.ForegroundColorSpan(color), from, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.BOLD), from, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * 出走馬の一覧の上に出す、近走を10走まで広げるボタン（リワード広告）。
     * 今日すでに見ていれば、10走を表示中であることだけ出す
     */
    private View createRewardView(List<SupabaseManager.RaceEntry> entries) {
        float density = getResources().getDisplayMetrics().density;
        TextView view = new TextView(requireContext());
        int pad = (int) (12 * density);
        view.setPadding(pad, pad, pad, pad);
        view.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, (int) (4 * density), 0, (int) (8 * density));
        view.setLayoutParams(params);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(10 * density);
        if (RecentRunsReward.isUnlocked(requireContext())) {
            view.setText("✓ 今日は近走を" + RecentRunsReward.REWARDED_RUNS + "走まで表示しています");
            view.setTextColor(0xFF2E7D32);
            bg.setColor(0xFFE8F5E9);
        } else {
            view.setText("▶ 広告を見て近走を" + RecentRunsReward.REWARDED_RUNS + "走まで表示（今日いっぱい）");
            view.setTextColor(0xFF0D47A1);
            view.setTypeface(view.getTypeface(), Typeface.BOLD);
            bg.setColor(0xFFE3F2FD);
            bg.setStroke((int) Math.max(1, density), 0xFF1565C0);
            view.setOnClickListener(v -> reward.show(requireActivity(), new RecentRunsReward.Listener() {
                @Override
                public void onRewarded() {
                    if (binding == null || !isAdded()) return;
                    Toast.makeText(getContext(), "今日いっぱい、近走を" + RecentRunsReward.REWARDED_RUNS + "走まで表示します",
                            Toast.LENGTH_SHORT).show();
                    // 10走分はもう読んであるので、表示だけ広げる
                    showHeader(entries);
                    render(entries, lastResults);
                }

                @Override
                public void onNotShown(String message) {
                    if (isAdded()) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }));
        }
        view.setBackground(bg);
        return view;
    }

    /** 印をタップするたびに ◎→○→▲→△→なし。◎○▲は1頭だけなので、ほかの馬の印も書き直す */
    private void bindMark(View card, SupabaseManager.RaceEntry entry) {
        TextView view = card.findViewById(R.id.text_entry_mark);
        String key = FavoriteHorseNames.normalize(entry.getHorseName());
        markViews.put(key, view);
        paintMark(view, marks.get(key));
        view.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(getContext(), "予想の印を付けるにはログインが必要です", Toast.LENGTH_SHORT).show();
                return;
            }
            Predictions.setMark(entry.getKaisaibi(), entry.getKaisaijo(), entry.getRaceNo(), entry.getRaceTitle(),
                    marks, entry.getHorseName(), Predictions.nextMark(marks.get(key)));
            for (Map.Entry<String, TextView> e : markViews.entrySet()) paintMark(e.getValue(), marks.get(e.getKey()));
        });
    }

    private static void paintMark(TextView view, @Nullable String mark) {
        view.setText(mark != null ? mark : "印");
        view.setTextSize(mark != null ? 20 : 12);
        view.setTextColor(mark == null ? 0xFFBDBDBD : "◎".equals(mark) ? 0xFFC62828 : 0xFF1565C0);
    }

    /**
     * 今日のレースで発走から時間がたっていれば、keibalab から結果を取って各馬に着順を出す。
     * 印を付けていれば、◎の着順も画面の上に出す（答え合わせ）
     */
    private void loadTodayResult(SupabaseManager.RaceEntry race) {
        org.threeten.bp.LocalDateTime start = RaceStartNotices.startTime(race.getKaisaibi(), race.getRaceInfo());
        if (start == null || org.threeten.bp.LocalDateTime.now().isBefore(start.plusMinutes(10))) return;
        if (!race.getKaisaibi().equals(org.threeten.bp.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))) return;
        ODDS_EXECUTOR.execute(() -> {
            List<SupabaseManager.RaceResult> rows;
            List<KeibalabPayouts.Payout> fetched;
            try {
                rows = KeibalabResults.fetchRaceBlocking(race.getKaisaibi(), race.getKaisaijo(), race.getRaceNo());
                fetched = rows.isEmpty() ? new ArrayList<>()
                        : KeibalabPayouts.fetchBlocking(race.getKaisaibi(), race.getKaisaijo(), race.getRaceNo());
            } catch (Exception e) {
                Log.w("EntryDetailFragment", "結果の取得に失敗", e);
                return;
            }
            if (rows.isEmpty() || getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (binding == null || !isAdded()) return;
                showResult(rows);
                payouts = fetched;
                raceBets = Bets.settle(raceBets, payouts);
                renderBets();
            });
        });
    }

    private void showResult(List<SupabaseManager.RaceResult> rows) {
        String honmeiLine = null;
        for (SupabaseManager.RaceResult r : rows) {
            String key = FavoriteHorseNames.normalize(r.getHorseName());
            TextView view = resultViews.get(key);
            if (view == null) continue;
            String rank = r.getRank() != null ? r.getRank() : "-";
            String label = "結果 " + (rank.matches("\\d+") ? rank + "着" : rank);
            if (r.getPopular() != null) label += "（" + r.getPopular() + "番人気）";
            view.setText(label);
            view.setTextColor("1".equals(rank) ? 0xFFC62828 : ("2".equals(rank) || "3".equals(rank)) ? 0xFF2E7D32 : 0xFF555555);
            view.setVisibility(View.VISIBLE);
            if ("◎".equals(marks.get(key))) {
                honmeiLine = "◎" + r.getHorseName() + "は" + (rank.matches("\\d+") ? rank + "着" : rank)
                        + ("1".equals(rank) ? "　的中！" : "");
            }
        }
        binding.textEntriesLead.append("\n" + (honmeiLine != null ? honmeiLine : "結果が確定しました"));
    }

    /**
     * 今の単勝・複勝オッズを keibalab から取って各馬に出す。
     * 取れなければ、取り込み済みの単勝オッズ（当日のバッチで更新）を出す。枠順確定前は何もしない
     */
    private void loadOdds(List<SupabaseManager.RaceEntry> entries) {
        if (oddsViews.isEmpty()) return;
        SupabaseManager.RaceEntry first = entries.get(0);
        ODDS_EXECUTOR.execute(() -> {
            Map<String, KeibalabOdds.HorseOdds> live = Collections.emptyMap();
            try {
                live = KeibalabOdds.fetchOddsTableBlocking(first.getKaisaibi(), first.getKaisaijo(), first.getRaceNo());
            } catch (Exception e) {
                Log.w("EntryDetailFragment", "オッズの取得に失敗（取り込み済みのオッズを出す）", e);
            }
            Map<String, KeibalabOdds.HorseOdds> odds = live;
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (binding == null || !isAdded()) return;
                showOdds(entries, odds, time);
            });
        });
    }

    private void showOdds(List<SupabaseManager.RaceEntry> entries, Map<String, KeibalabOdds.HorseOdds> live, String liveTime) {
        String shownTime = null;
        for (SupabaseManager.RaceEntry entry : entries) {
            if (entry.getHorseNumber() == null) continue;
            String number = entry.getHorseNumber().trim();
            TextView view = oddsViews.get(number);
            if (view == null) continue;
            KeibalabOdds.HorseOdds odds = live.get(number.replaceFirst("^0+(?=\\d)", ""));
            String time = liveTime;
            if (odds == null && entry.getWinOdds() != null) {
                odds = new KeibalabOdds.HorseOdds(entry.getWinOdds(), null, null, parseIntOrNull(entry.getPopular()));
                time = storedTime(entry.getOddsUpdatedAt());
            }
            String label = odds != null ? odds.label() : "";
            if (label.isEmpty()) continue;
            view.setText(label);
            view.setVisibility(View.VISIBLE);
            if (shownTime == null) shownTime = time;
        }
        if (shownTime != null) {
            binding.textEntriesLead.append("\nオッズは" + shownTime + "時点（開き直すと更新します）");
        }
    }

    @Nullable
    private static Integer parseIntOrNull(@Nullable String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 取り込んだ時刻（"2026-10-17T04:15:00+00:00"）を端末の時刻の "13:15" にする。読めなければ null */
    @Nullable
    private static String storedTime(@Nullable String timestamp) {
        if (timestamp == null) return null;
        try {
            return OffsetDateTime.parse(timestamp).atZoneSameInstant(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("H:mm"));
        } catch (DateTimeException e) {
            return null;
        }
    }

    private void bindFavorite(View card, String horseName) {
        TextView star = card.findViewById(R.id.text_entry_favorite);
        View cardView = card.findViewById(R.id.card_entry_horse);
        Runnable paint = () -> {
            boolean fav = FavoriteHorseNames.contains(favorites, horseName);
            star.setText(fav ? "★" : "☆");
            star.setTextColor(fav ? 0xFFFFB300 : 0xFFBDBDBD);
            ((MaterialCardView) cardView)
                    .setCardBackgroundColor(fav ? 0xFFFFF8E1 : 0xFFFFFFFF);
        };
        paint.run();
        star.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(getContext(), "お気に入り機能を利用するにはログインが必要です", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean added = FavoriteHorseNames.toggle(favorites, horseName);
            paint.run();
            // このレースの発走前の通知をすぐ予約する（定期チェックを待つと間に合わないことがある）
            if (added) FavoriteResultWorker.runOnce(requireContext());
            Toast.makeText(getContext(), horseName + (added ? "をお気に入りに追加しました" : "をお気に入りから削除しました"), Toast.LENGTH_SHORT).show();
        });
    }

    private void bindMemo(View card, String horseName) {
        TextView button = card.findViewById(R.id.text_entry_memo);
        TextView body = card.findViewById(R.id.text_entry_memo_body);
        Runnable paint = () -> {
            HorseMemos.Memo memo = HorseMemos.find(memos, horseName);
            button.setAlpha(memo != null ? 1f : 0.35f);
            if (memo == null) {
                body.setVisibility(View.GONE);
            } else {
                body.setText("📝 " + memo.text);
                body.setVisibility(View.VISIBLE);
            }
        };
        paint.run();
        View.OnClickListener edit = v -> HorseMemoDialog.show(requireContext(), memos, horseName, raceLabel,
                memo -> paint.run());
        button.setOnClickListener(edit);
        body.setOnClickListener(edit);
    }

    private void showBadges(ChipGroup group, SupabaseManager.RaceEntry entry, List<SupabaseManager.RaceResult> runs) {
        List<EntryForm.Run> formRuns = new ArrayList<>();
        if (runs != null) {
            for (SupabaseManager.RaceResult r : runs) {
                formRuns.add(new EntryForm.Run(r.getKaisaibi(), r.getHassouTime(), r.getRank(), r.getJockey()));
            }
        }
        List<String> badges = EntryForm.badges(raceDate, entry.getRaceInfo(), entry.getJockey(), formRuns);
        if (badges.isEmpty()) {
            group.setVisibility(View.GONE);
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        for (String text : badges) {
            // チップより背の低い枠線つきのラベルにする（1頭あたりの高さを抑えるため）
            boolean caution = text.equals("休み明け") || text.equals("連闘") || text.equals("乗り替わり") || text.contains("→");
            TextView badge = new TextView(requireContext());
            badge.setText(text);
            badge.setTextSize(11);
            badge.setTextColor(caution ? 0xFFE65100 : 0xFF2E7D32);
            badge.setPadding((int) (8 * density), (int) (2 * density), (int) (8 * density), (int) (2 * density));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(10 * density);
            bg.setStroke((int) Math.max(1, density), caution ? 0xFFFB8C00 : 0xFF43A047);
            badge.setBackground(bg);
            group.addView(badge);
        }
    }

    private void styleWaku(TextView view, String waku) {
        int index = 0;
        try {
            index = Integer.parseInt(waku != null ? waku.trim() : "") - 1;
        } catch (NumberFormatException ignored) {
        }
        if (index < 0 || index >= WAKU_BACKGROUND.length) index = 0;
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(6 * getResources().getDisplayMetrics().density);
        bg.setColor(WAKU_BACKGROUND[index]);
        bg.setStroke(2, 0xFFBDBDBD);
        view.setBackground(bg);
        view.setTextColor(WAKU_TEXT[index]);
    }

    private View createPastRunView(LayoutInflater inflater, ViewGroup parent, SupabaseManager.RaceResult run) {
        View row = inflater.inflate(R.layout.item_past_run, parent, false);
        String date = WeekendDays.formatShort(run.getKaisaibi());
        // 「2026年10月12日(月)」→「2026年10月12日」（列の幅を揃えるため曜日は省く）
        int paren = date.indexOf('(');
        ((TextView) row.findViewById(R.id.text_run_date)).setText(paren > 0 ? date.substring(0, paren) : date);
        ((TextView) row.findViewById(R.id.text_run_course)).setText(run.getKaisaijo());
        ((TextView) row.findViewById(R.id.text_run_title)).setText(run.getRaceTitle() != null ? run.getRaceTitle().trim() : "");
        ((TextView) row.findViewById(R.id.text_run_distance)).setText(EntryForm.courseLabel(run.getHassouTime()));

        String rank = run.getRank() != null ? run.getRank() : "-";
        String result = rank.matches("\\d+") ? rank + "着" : rank;
        if (run.getPopular() != null && !run.getPopular().isEmpty()) result += " " + run.getPopular() + "人";
        TextView resultView = row.findViewById(R.id.text_run_result);
        resultView.setText(result);
        if ("1".equals(rank)) {
            resultView.setTextColor(0xFFC62828);
            resultView.setTypeface(resultView.getTypeface(), Typeface.BOLD);
        } else if ("2".equals(rank) || "3".equals(rank)) {
            resultView.setTextColor(0xFF2E7D32);
            resultView.setTypeface(resultView.getTypeface(), Typeface.BOLD);
        } else {
            resultView.setTextColor(0xFF333333);
        }

        // その日の結果画面を、このレースまでスクロールしてこの馬の行に色を付けた状態で開く
        row.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_raceResults,
                RaceResultsFragment.args(run.getKaisaibi(), run.getKaisaijo(), run.getRaceNo(), run.getHorseName())));
        return row;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
