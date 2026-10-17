package com.tktkcompany.kakoRaceKeiba.ui.raceResult;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentRaceresultsBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.db.KeibalabResults;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemoDialog;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemos;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RaceResultsFragment extends Fragment {
    private FragmentRaceresultsBinding binding;
    private final List<TableLayout> tableLayouts = new ArrayList<>();
    private final List<TextView> titleTextViews = new ArrayList<>();
    private final List<TextView> timeTextViews = new ArrayList<>();
    private String raceDate;
    private Map<String, HorseMemos.Memo> memos = new HashMap<>();
    private static final ExecutorService LIVE_EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRaceresultsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews();
        loadBannerAd();

        Bundle args = getArguments();
        if (args != null) {
            String date = args.getString("key");
            String jo = args.getString("jo");
            if (date != null && jo != null) {
                setScreenTitle(WeekendDays.formatShort(date) + " " + jo + "のレース結果");
                raceDate = date;
                // 各行の📝にメモの有無を出すため、先にメモを読んでおく
                HorseMemos.load(loaded -> {
                    if (binding == null || !isAdded()) return;
                    memos = new HashMap<>(loaded);
                    loadRaceResults(date, jo);
                });
            }
        }

        return root;
    }

    // どの開催日・競馬場の結果かを画面上部のタイトルに出す
    private void setScreenTitle(String title) {
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) actionBar.setTitle(title);
        }
    }

    private void initViews() {
        for (int i = 1; i <= 12; i++) {
            try {
                java.lang.reflect.Field tableField = binding.getClass().getDeclaredField("tableLayout" + i);
                tableLayouts.add((TableLayout) tableField.get(binding));
                java.lang.reflect.Field titleField = binding.getClass().getDeclaredField("textDashboard" + i);
                titleTextViews.add((TextView) titleField.get(binding));
                java.lang.reflect.Field timeField = binding.getClass().getDeclaredField("textHassouTime" + i);
                timeTextViews.add((TextView) timeField.get(binding));
            } catch (Exception e) {
                Log.e("RaceResultsFragment", "Error mapping views: " + e.getMessage());
            }
        }
    }

    private void loadRaceResults(String date, String joName) {
        SupabaseManager.fetchRaceResults(date, joName, new SupabaseManager.SupabaseCallback<List<SupabaseManager.RaceResult>>() {
            @Override
            public void onSuccess(List<SupabaseManager.RaceResult> results) {
                if (binding == null || !isAdded()) return;
                Set<Integer> loaded = new HashSet<>();
                for (SupabaseManager.RaceResult result : results) {
                    int raceNo = result.getRaceNo();
                    if (raceNo >= 1 && raceNo <= 12) {
                        updateRaceUI(raceNo, result);
                        loaded.add(raceNo);
                    }
                }
                loadTodayResultsLive(date, joName, loaded);
            }
            @Override
            public void onFailure(@NonNull String message) {
                Log.e("RaceResultsFragment", "Supabase load failed: " + message);
                if (binding == null || !isAdded()) return;
                loadTodayResultsLive(date, joName, new HashSet<>());
            }
        });
    }

    /**
     * 今日の開催は、夜の取り込みを待たずに keibalab から確定したレースの結果を取って出す。
     * レースは番号順に行われるので、結果がまだ無いレースが見つかったらそこで止める
     */
    private void loadTodayResultsLive(String date, String joName, Set<Integer> alreadyShown) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (!today.equals(date)) return;
        LIVE_EXECUTOR.execute(() -> {
            for (int raceNo = 1; raceNo <= 12; raceNo++) {
                if (alreadyShown.contains(raceNo)) continue;
                List<SupabaseManager.RaceResult> rows;
                try {
                    rows = KeibalabResults.fetchRaceBlocking(date, joName, raceNo);
                } catch (Exception e) {
                    Log.w("RaceResultsFragment", "当日の結果の取得に失敗: " + raceNo + "R", e);
                    return;
                }
                if (rows.isEmpty()) return;
                int no = raceNo;
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (binding == null || !isAdded()) return;
                    for (SupabaseManager.RaceResult row : rows) updateRaceUI(no, row);
                });
            }
        });
    }

    private void updateRaceUI(int raceNo, SupabaseManager.RaceResult result) {
        int index = raceNo - 1;
        TableLayout table = tableLayouts.get(index);
        TextView titleTv = titleTextViews.get(index);
        TextView timeTv = timeTextViews.get(index);

        if (table.getChildCount() == 0) {
            titleTv.setText(raceNo + "R   " + (result.getRaceTitle() != null ? result.getRaceTitle() : ""));
            titleTv.setPadding(16, 8, 16, 8);
            timeTv.setText(formatHassouTime(result.getHassouTime()));
            timeTv.setTextSize(12);
            timeTv.setPadding(16, 8, 16, 16);
            table.addView(createHeaderRow());
        }
        table.addView(createDataRow(result));
    }

    private String formatHassouTime(String rawTime) {
        if (rawTime == null) return "";
        int ageIndex = rawTime.indexOf("馬齢");
        if (ageIndex != -1) return rawTime.substring(ageIndex);
        int startIndex = rawTime.indexOf("発走");
        if (startIndex != -1) return rawTime.substring(0, startIndex + 2);
        return rawTime;
    }

    private TableRow createHeaderRow() {
        TableRow row = new TableRow(getContext());
        row.setBackgroundColor(Color.LTGRAY);
        String[] headers = {"★", "📝", "着", "枠", "馬名", "性齢", "騎手", "人気", "単勝", "タイム"};
        for (String h : headers) row.addView(createTextView(h));
        return row;
    }

    private TableRow createDataRow(SupabaseManager.RaceResult result) {
        TableRow row = new TableRow(getContext());
        String horseName = result.getHorseName() != null ? result.getHorseName() : "";
        TextView favTv = createTextView("☆");
        favTv.setOnClickListener(v -> toggleFavorite(row, favTv, horseName));
        favTv.setClickable(true);
        favTv.setFocusable(true);
        if (getContext() != null) {
            android.util.TypedValue outValue = new android.util.TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            favTv.setBackgroundResource(outValue.resourceId);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // お気に入りの登録状態はお気に入り一覧と同じFirebaseを参照する
            FirebaseManager.isFavoriteHorse(user.getUid(), horseName, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isFav = snapshot.getValue(Boolean.class);
                    if (isFav != null && isFav) {
                        favTv.setText("★");
                        favTv.setTextColor(Color.parseColor("#FFD700"));
                        row.setBackgroundColor(Color.parseColor("#33FFD700"));
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        row.addView(favTv);
        row.addView(createMemoView(result, horseName));

        row.addView(createTextView(result.getRank()));
        row.addView(createTextView(result.getWaku()));
        row.addView(createTextView(result.getHorseName()));
        row.addView(createTextView(result.getAge()));
        row.addView(createTextView(result.getJockey()));
        row.addView(createTextView(result.getPopular()));
        row.addView(createTextView(result.getWinOdds()));
        row.addView(createTextView(result.getTime()));
        return row;
    }

    // 📝 をタップするとその馬のメモを書ける。メモがある馬は濃く表示する
    private TextView createMemoView(SupabaseManager.RaceResult result, String horseName) {
        TextView memoTv = createTextView("📝");
        memoTv.setAlpha(HorseMemos.find(memos, horseName) != null ? 1f : 0.35f);
        String rank = result.getRank() != null ? result.getRank() : "";
        String title = result.getRaceTitle() != null ? result.getRaceTitle().trim() : "";
        String from = (WeekendDays.formatShort(raceDate) + " " + result.getKaisaijo() + result.getRaceNo() + "R "
                + title + " " + (rank.matches("\\d+") ? rank + "着" : rank)).replaceAll("\\s+", " ").trim();
        memoTv.setOnClickListener(v -> HorseMemoDialog.show(requireContext(), memos, horseName, from,
                memo -> memoTv.setAlpha(memo != null ? 1f : 0.35f)));
        return memoTv;
    }

    private void toggleFavorite(TableRow row, TextView textView, String horseName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "お気に入り機能を利用するにはログインが必要です", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String safeName = SupabaseManager.sanitizeKey(horseName);
        if (safeName.isEmpty()) return;

        if ("☆".equals(textView.getText().toString())) {
            FirebaseManager.addFavoriteHorse(uid, horseName);
            SupabaseManager.addFavorite(uid, horseName);
            textView.setText("★");
            textView.setTextColor(Color.parseColor("#FFD700"));
            row.setBackgroundColor(Color.parseColor("#33FFD700"));
            Toast.makeText(getContext(), safeName + "をお気に入りに追加しました", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseManager.removeFavoriteHorse(uid, horseName);
            SupabaseManager.removeFavorite(uid, horseName);
            textView.setText("☆");
            textView.setTextColor(Color.BLACK);
            row.setBackgroundColor(Color.TRANSPARENT);
            Toast.makeText(getContext(), safeName + "をお気に入りから削除しました", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text != null ? text : "");
        tv.setPadding(16, 8, 16, 8);
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(2, Color.BLACK);
        tv.setBackground(gd);
        return tv;
    }

    private void loadBannerAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.setAdListener(new AdListener() {
            @Override public void onAdFailedToLoad(@NonNull LoadAdError adError) { Log.e("Ads", adError.getMessage()); }
        });
        binding.adView.loadAd(adRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
