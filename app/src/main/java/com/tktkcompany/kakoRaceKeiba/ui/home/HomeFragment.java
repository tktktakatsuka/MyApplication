package com.tktkcompany.kakoRaceKeiba.ui.home;


import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tktkcompany.kakoRaceKeiba.MainActivity;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentHomeBinding;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.notification.FavoriteResultWorker;
import com.tktkcompany.kakoRaceKeiba.notification.GradedRaceWorker;
import com.tktkcompany.kakoRaceKeiba.ui.entries.FavoriteHorseNames;
import com.tktkcompany.kakoRaceKeiba.ui.horsememo.HorseMemos;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public static AdView bannerAdView;
    private SharedViewModel sharedViewModel;
    private FirebaseAuth mAuth; // mAuthをメンバー変数として追加
    private boolean exeSetRaceResult;
    // 「今日の開催」カード（開催日だけ表示）
    private TodayCard todayCard;
    // 「今週の開催」に出馬表の開催日・競馬場を出したか（出していれば開催中の競馬場の一覧で上書きしない）
    private boolean entriesSummaryShown;
    // yyyyMMdd形式用
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        View root = binding.getRoot();


        // AdViewのインスタンスを取得、ロード
        loadBannerAd(binding);

        // お気に入り一覧ボタンの設定
        binding.btnFavoriteHorses.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_navigation_home_to_navigation_favorites);
        });

        binding.btnPredictions.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_predictions));
        binding.cardEntries.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_navigation_entries));
        todayCard = new TodayCard(this, binding.cardToday, binding.todayContainer);
        loadUpcomingEntries();

        // ログイン・ログアウトボタンの設定
        binding.btnLogin.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).signInWithGoogle();
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).signOut();
            }
        });

        // Supabaseから開催中の競馬場リストを取得
        SupabaseManager.fetchActiveCourses(new SupabaseManager.SupabaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> joNames) {
                if (!isAdded()) return;
                sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                sharedViewModel.setJoNames(joNames);
            }

            @Override
            public void onFailure(@NonNull String message) {
                Toast.makeText(getContext(), "データの読み込みに失敗しました", Toast.LENGTH_SHORT).show();
            }
        });


        // 🔑 setJoNames 完了後にここが呼ばれる
        sharedViewModel.getJoNames().observe(getViewLifecycleOwner(), joNames -> {
            updateUpcomingCard(joNames);
            if (joNames == null || joNames.isEmpty()) return;

            int limit = Math.min(joNames.size(), 2);
            for (int i = 0; i < limit; i++) {
                final int index = i;
                String joName = joNames.get(i);
                // 各競馬場の最新のメインレース(11R)の結果を表示する
                SupabaseManager.fetchLatestMainRace(joName, new SupabaseManager.SupabaseCallback<List<SupabaseManager.RaceResult>>() {
                    @Override
                    public void onSuccess(List<SupabaseManager.RaceResult> results) {
                        if (!isAdded() || binding == null) return;
                        for (SupabaseManager.RaceResult result : results) {
                            if (index == 0) {
                                raceResultSet(result.getRank(), result.getRaceTitle(), result.getKaisaijo(), result.getHassouTime(),
                                        result.getKaisaibi(), result.getWinOdds(), result.getHorseName(), root);
                            } else if (index == 1) {
                                raceResultSet2(result.getRank(), result.getRaceTitle(), result.getKaisaijo(), result.getHassouTime(),
                                        result.getKaisaibi(), result.getWinOdds(), result.getHorseName(), root);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull String message) {
                        Log.e("HomeFragment", "Supabase load failed: " + message);
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (todayCard != null) todayCard.stop();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ★★★ Firebase Authを初期化 ★★★
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        // ★★★ ここからが重要な修正 ★★★
        // Fragmentが表示されるたびに、現在のログイン状態をチェックしてUIを更新する
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        // ★★★ ここまで ★★★
    }

    // ★★★ 自分の画面のUI更新は自分で行う ★★★
// (MainActivityから呼び出されるようにpublicにする)
    public void updateUI(FirebaseUser user) {
        if (binding == null) return; // 画面が破棄された後は何もしない

        updateUpcomingHint(user);

        if (user != null) {
            // ログイン中
            binding.layoutUserInfo.setVisibility(View.VISIBLE);
            binding.textUserName.setText(user.getDisplayName() + " さんとしてログイン中");
            binding.layoutLogin.setVisibility(View.GONE);
            binding.layoutQuickButtons.setVisibility(View.VISIBLE);
        } else {
            // 未ログイン
            binding.layoutUserInfo.setVisibility(View.GONE);
            binding.layoutLogin.setVisibility(View.VISIBLE);
            binding.layoutQuickButtons.setVisibility(View.GONE);
        }
        placeAuthCard(user != null);
    }

    /**
     * ログイン中はログインのカードを一番下（広告の上）に回し、今日の開催を一番上に出す。
     * 未ログインのときは、ログインを促すため一番上に置く
     */
    private void placeAuthCard(boolean signedIn) {
        ViewGroup parent = (ViewGroup) binding.cardUserAuth.getParent();
        if (parent == null) return;
        parent.removeView(binding.cardUserAuth);
        if (signedIn) {
            parent.addView(binding.cardUserAuth, parent.indexOfChild(binding.adView));
        } else {
            parent.addView(binding.cardUserAuth, 0);
        }
    }


    // 「今週の開催」カードに開催中の競馬場と次の開催日を表示する
    private void updateUpcomingCard(List<String> joNames) {
        if (binding == null) return;
        List<LocalDate> days = WeekendDays.getUpcomingRaceDays();
        if (joNames == null || joNames.isEmpty() || days.isEmpty()) {
            binding.cardUpcoming.setVisibility(View.GONE);
            return;
        }
        DateTimeFormatter md = DateTimeFormatter.ofPattern("M/d");
        List<String> dayLabels = new ArrayList<>();
        for (LocalDate d : days) {
            dayLabels.add(d.format(md) + WeekendDays.getDayOfWeek(d.format(formatter)).trim());
        }
        // 出馬表が読めたら、そちらの開催日・競馬場で書き直す（新潟などの開催も入るため）
        if (!entriesSummaryShown) {
            binding.textUpcomingBody.setText(TextUtils.join("・", dayLabels) + "　" + TextUtils.join("・", joNames));
        }
        binding.cardUpcoming.setVisibility(View.VISIBLE);
        updateUpcomingHint(mAuth != null ? mAuth.getCurrentUser() : null);
    }

    // 出馬表が登録されていれば「今週の出走馬」カードを出す
    private void loadUpcomingEntries() {
        String today = LocalDate.now().format(formatter);
        SupabaseManager.fetchUpcomingEntries(today, new SupabaseManager.SupabaseCallback<List<SupabaseManager.RaceEntry>>() {
            @Override
            public void onSuccess(List<SupabaseManager.RaceEntry> entries) {
                if (!isAdded() || binding == null || entries.isEmpty()) return;
                Set<String> days = new LinkedHashSet<>();
                Set<String> courses = new LinkedHashSet<>();
                Set<String> races = new LinkedHashSet<>();
                for (SupabaseManager.RaceEntry e : entries) {
                    days.add(WeekendDays.formatShort(e.getKaisaibi()));
                    courses.add(e.getKaisaijo());
                    races.add(e.getKaisaibi() + e.getKaisaijo() + e.getRaceNo());
                }
                binding.textUpcomingBody.setText(TextUtils.join("・", days) + "　" + TextUtils.join("・", courses));
                entriesSummaryShown = true;
                String body = races.size() + "レースの出走馬と近走成績";
                binding.textEntriesBody.setText(body);
                binding.cardEntries.setVisibility(View.VISIBLE);

                // お気に入り馬・メモした馬が出走予定なら、その馬名も出す
                FavoriteHorseNames.load(favorites -> HorseMemos.load(memos -> {
                    if (!isAdded() || binding == null) return;
                    Set<String> favoriteRunners = new LinkedHashSet<>();
                    Set<String> memoRunners = new LinkedHashSet<>();
                    for (SupabaseManager.RaceEntry e : entries) {
                        if (FavoriteHorseNames.contains(favorites, e.getHorseName())) favoriteRunners.add(e.getHorseName());
                        if (HorseMemos.find(memos, e.getHorseName()) != null) memoRunners.add(e.getHorseName());
                    }
                    // 馬名を全部並べると長くなるので、頭数と最初の2頭だけ出す
                    StringBuilder text = new StringBuilder(body);
                    if (!favoriteRunners.isEmpty()) text.append("\n★ お気に入り ").append(runnersLabel(favoriteRunners));
                    if (!memoRunners.isEmpty()) text.append("\n📝 メモした馬 ").append(runnersLabel(memoRunners));
                    binding.textEntriesBody.setText(text);
                    todayCard.show(entries, favorites);
                    // 開催日は今日の開催カードで足りるので、前の開催のメインレース結果は隠す
                    binding.layoutLastResults.setVisibility(
                            binding.cardToday.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }));
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.w("HomeFragment", "Entries load failed: " + message);
            }
        });
    }

    /** 1頭なら馬名、何頭もいれば「7頭（バルチックアンバー ほか）」 */
    static String runnersLabel(Set<String> names) {
        List<String> list = new ArrayList<>(names);
        if (list.size() == 1) return list.get(0);
        return list.size() + "頭（" + list.get(0) + " ほか）";
    }

    private void updateUpcomingHint(FirebaseUser user) {
        if (binding == null) return;
        binding.textUpcomingHint.setText("🔔 通知の設定 ›");
        binding.textUpcomingHint.setOnClickListener(v -> showNotificationSettings(user != null));
    }

    /** 通知の説明と設定（今は重賞の発走前の通知のオン・オフだけ） */
    private void showNotificationSettings(boolean signedIn) {
        boolean[] checked = {GradedRaceWorker.isEnabled(requireContext())};
        float density = getResources().getDisplayMetrics().density;
        TextView title = new TextView(requireContext());
        int pad = (int) (20 * density);
        title.setPadding(pad, pad, pad, (int) (8 * density));
        SpannableStringBuilder text = new SpannableStringBuilder("通知の設定");
        text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(1.3f), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.append("\n\n").append(signedIn
                ? "お気に入り馬・メモした馬の出走予定、お気に入り馬の発走30分前と結果（確定後すぐ）をお知らせします。"
                : "ログインしてお気に入り馬を登録すると、出走予定・発走前・結果もお知らせします。");
        title.setText(text);
        title.setTextColor(0xFF333333);
        new AlertDialog.Builder(requireContext())
                .setCustomTitle(title)
                .setMultiChoiceItems(new String[]{"重賞の発走30分前に上位人気とオッズを知らせる"}, checked,
                        (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("保存", (dialog, which) -> {
                    GradedRaceWorker.setEnabled(requireContext(), checked[0]);
                    // オンにしたら、次の定期チェックを待たずに予約する
                    if (checked[0]) FavoriteResultWorker.runOnce(requireContext());
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    //バナーを表示するメソッド
    public void loadBannerAd(FragmentHomeBinding binding) {
        bannerAdView = binding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();

        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdClosed() {
            }
        });

        bannerAdView.loadAd(adRequest);
    }

    private Bundle setKeyjoNameString(String date, String jo) {
        // 渡したい値を用意する
        // Bundleを作成して値を詰める
        Bundle bundle = new Bundle();
        bundle.putString("key", date);
        bundle.putString("jo", jo);
        return bundle;
    }

    private void raceResultSet(String tyaku, String raceTitle, String kaisaijo, String hassouTime, String raceDateStr, String winOdds, String horseName, View root) {

        if ("1".equals(tyaku)) {
            //レースタイトル,時刻、場名を設定する
            binding.textTodayRaceName.setText(raceTitle);
            binding.textTodayRaceLocation.setText(kaisaijo);
            binding.textTodayRaceTime.setText(hassouTime);
            binding.textTodayRaceNumber.setText(WeekendDays.formatShort(raceDateStr) + " " + kaisaijo + "のレース結果");
            binding.textPopularRaceResultMore.setText(kaisaijo + "のレース結果をもっとみる");
            binding.textRaceResult1Odds.setText(winOdds);
            //レースの着順を設定する。
            binding.textRaceResult1Name.setText(horseName);
            // ▼ ここで「もっと見る」にクリックリスナーを設定する
            TextView moreTextView = root.findViewById(R.id.text_popular_raceResult_more);
            moreTextView.setOnClickListener(v -> {
                // 例: 直近の「日付」と「競馬場」を渡して同じ画面に遷移
                String date = raceDateStr; // ← 実際には任意の date に置き換える
                String joName = kaisaijo;   // ← 実際には任意の joName に置き換える
                Bundle bundle = setKeyjoNameString(date, joName);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_raceResults, bundle);
            });

        } else if ("2".equals(tyaku)) {
            //レースの着順を設定する。
            binding.textRaceResult2Name.setText(horseName);
            binding.textRaceResult2Odds.setText(winOdds);
        } else if ("3".equals(tyaku)) {
            //レースの着順を設定する。
            binding.textRaceResult3Name.setText(horseName);
            binding.textRaceResult3Odds.setText(winOdds);
        }
    }

    private void raceResultSet2(String tyaku, String raceTitle, String kaisaijo, String hassouTime, String raceDateStr, String winOdds, String horseName, View root) {
        if ("1".equals(tyaku)) {
            //レースタイトル,時刻、場名を設定する
            binding.text2TodayRaceName.setText(raceTitle);
            binding.text2TodayRaceLocation.setText(kaisaijo);
            binding.text2TodayRaceTime.setText(hassouTime);
            binding.text2TodayRaceNumber.setText(WeekendDays.formatShort(raceDateStr) + " " + kaisaijo + "のレース結果");
            binding.text2PopularRaceResultMore.setText(kaisaijo + "のレース結果をもっとみる");
            binding.text2RaceResult1Odds.setText(winOdds);
            //レースの着順を設定する。
            binding.text2RaceResult1Name.setText(horseName);
            // ▼ ここで「もっと見る」にクリックリスナーを設定する
            TextView moreTextView = root.findViewById(R.id.text2_popular_raceResult_more);
            moreTextView.setOnClickListener(v -> {
                // 例: 直近の「日付」と「競馬場」を渡して同じ画面に遷移
                String date = raceDateStr; // ← 実際には任意の date に置き換える
                String joName = kaisaijo;   // ← 実際には任意の joName に置き換える
                Bundle bundle = setKeyjoNameString(date, joName);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_raceResults, bundle);
            });

        } else if ("2".equals(tyaku)) {
            //レースの着順を設定する。
            binding.text2RaceResult2Name.setText(horseName);
            binding.text2RaceResult2Odds.setText(winOdds);
        } else if ("3".equals(tyaku)) {
            //レースの着順を設定する。
            binding.text2RaceResult3Name.setText(horseName);
            binding.text2RaceResult3Odds.setText(winOdds);
        }
    }


}

