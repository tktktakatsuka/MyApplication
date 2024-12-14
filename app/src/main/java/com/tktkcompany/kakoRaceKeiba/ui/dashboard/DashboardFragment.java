package com.tktkcompany.kakoRaceKeiba.ui.dashboard;


import android.os.Bundle;


import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.DayOfWeek;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentDashboardBinding;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseHelper;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;


import java.util.List;

public class DashboardFragment extends Fragment {
    private MyDatabaseHelper dbHelper;

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        //DB接続
        MyDatabaseManager dbManager = new MyDatabaseManager(getContext());
        dbManager.open();

        // DatabaseHelperの初期化
        dbHelper = new MyDatabaseHelper(getContext());

        // ボタンを追加するLinearLayoutの参照を取得
        LinearLayout buttonContainer = root.findViewById(R.id.button_container);

        List<String> datelist = WeekendDays.getPastWeekendsInCurrentMonth();

        boolean textFlg = true;
        // 動的にボタンを生成して追加
        for (String date : datelist) {

            if (!dbManager.getRaceResults(date, "1", "東京").isEmpty()) {

                if (textFlg) {
                    // ボタンをLinearLayoutに追加
                    TextView newTextView = new TextView(getActivity());
                    newTextView.setText("東京");
                    newTextView.setTextSize(18);  // テキストサイズを設定
                    newTextView.setPadding(10, 20, 10, 20);  // パディングを設定
                    // TextView を LinearLayout に追加
                    buttonContainer.addView(newTextView);
                    textFlg = false;
                }
                // 新しいボタンを作成
                Button newButton = new Button(getActivity());
                newButton.setText(date + getDayOfWeek(date));
                // クリックリスナーを設定
                newButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    // 渡したい値を用意する
                    Bundle bundle = hoge(date, "東京");
                    // 値を使って何か処理
                    navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
                });
                // ボタンをLinearLayoutに追加
                buttonContainer.addView(newButton);
            }
        }

        textFlg = true;
        for (String date : datelist) {
            if (!dbManager.getRaceResults(date, "1", "京都").isEmpty()) {
                if (textFlg) {
                    // ボタンをLinearLayoutに追加
                    TextView newTextView = new TextView(getActivity());
                    newTextView.setText("京都");
                    newTextView.setTextSize(18);  // テキストサイズを設定
                    newTextView.setPadding(10, 20, 10, 20);  // パディングを設定
                    // TextView を LinearLayout に追加
                    buttonContainer.addView(newTextView);
                    textFlg = false;
                }

                // 新しいボタンを作成
                Button newButton = new Button(getActivity());
                newButton.setText(date + getDayOfWeek(date));
                // クリックリスナーを設定
                newButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    // 渡したい値を用意する
                    Bundle bundle = hoge(date, "京都");
                    // 値を使って何か処理
                    navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
                });
                // ボタンをLinearLayoutに追加
                buttonContainer.addView(newButton);
            }
        }

        textFlg = true;
        for (String date : datelist) {
            if (!dbManager.getRaceResults(date, "1", "新潟").isEmpty()) {
                if (textFlg) {
                    // ボタンをLinearLayoutに追加
                    TextView newTextView = new TextView(getActivity());
                    newTextView.setText("新潟");
                    newTextView.setTextSize(18);  // テキストサイズを設定
                    newTextView.setPadding(10, 20, 10, 20);  // パディングを設定
                    // TextView を LinearLayout に追加
                    buttonContainer.addView(newTextView);
                    textFlg = false;
                }
                // 新しいボタンを作成
                Button newButton = new Button(getActivity());
                newButton.setText(date + getDayOfWeek(date));
                // クリックリスナーを設定
                newButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    // 渡したい値を用意する
                    Bundle bundle = hoge(date, "新潟");
                    // 値を使って何か処理
                    navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
                });
                // ボタンをLinearLayoutに追加
                buttonContainer.addView(newButton);
            }
        }


        textFlg = true;
        for (String date : datelist) {
            if (!dbManager.getRaceResults(date, "1", "福島").isEmpty()) {
                if (textFlg) {
                    // ボタンをLinearLayoutに追加
                    TextView newTextView = new TextView(getActivity());
                    newTextView.setText("福島");
                    newTextView.setTextSize(18);  // テキストサイズを設定
                    newTextView.setPadding(10, 20, 10, 20);  // パディングを設定
                    // TextView を LinearLayout に追加
                    buttonContainer.addView(newTextView);
                    textFlg = false;
                }
                // 新しいボタンを作成
                Button newButton = new Button(getActivity());
                newButton.setText(date + getDayOfWeek(date));
                // クリックリスナーを設定
                newButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    // 渡したい値を用意する
                    Bundle bundle = hoge(date, "福島");
                    // 値を使って何か処理
                    navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
                });
                // ボタンをLinearLayoutに追加
                buttonContainer.addView(newButton);
            }
        }


        textFlg = true;
        for (String date : datelist) {
            if (!dbManager.getRaceResults(date, "1", "中京").isEmpty()) {
                if (textFlg) {
                    // ボタンをLinearLayoutに追加
                    TextView newTextView = new TextView(getActivity());
                    newTextView.setText("中京");
                    newTextView.setTextSize(18);  // テキストサイズを設定
                    newTextView.setPadding(10, 20, 10, 20);  // パディングを設定
                    // TextView を LinearLayout に追加
                    buttonContainer.addView(newTextView);
                    textFlg = false;
                }
                // 新しいボタンを作成
                Button newButton = new Button(getActivity());
                newButton.setText(date + getDayOfWeek(date));
                // クリックリスナーを設定
                newButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    // 渡したい値を用意する
                    Bundle bundle = hoge(date, "中京");
                    // 値を使って何か処理
                    navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
                });
                // ボタンをLinearLayoutに追加
                buttonContainer.addView(newButton);
            }
        }

        textFlg = true;
        for (String date : datelist) {
            if (!dbManager.getRaceResults(date, "1", "中山").isEmpty()) {
                if (textFlg) {
                    // ボタンをLinearLayoutに追加
                    TextView newTextView = new TextView(getActivity());
                    newTextView.setText("中山");
                    newTextView.setTextSize(18);  // テキストサイズを設定
                    newTextView.setPadding(10, 20, 10, 20);  // パディングを設定
                    // TextView を LinearLayout に追加
                    buttonContainer.addView(newTextView);
                    textFlg = false;
                }
                // 新しいボタンを作成
                Button newButton = new Button(getActivity());
                newButton.setText(date + getDayOfWeek(date));
                // クリックリスナーを設定
                newButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    // 渡したい値を用意する
                    Bundle bundle = hoge(date, "中山");
                    // 値を使って何か処理
                    navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
                });
                // ボタンをLinearLayoutに追加
                buttonContainer.addView(newButton);
            }
        }

        TextView newTextView = new TextView(getActivity());
        newTextView.setText("");
        newTextView.setTextSize(18);  // テキストサイズを設定
        newTextView.setPadding(10, 20, 10, 20);  // パディングを設定
        // TextView を LinearLayout に追加
        buttonContainer.addView(newTextView);

        TextView newTextView2 = new TextView(getActivity());
        newTextView2.setText("");
        newTextView2.setTextSize(18);  // テキストサイズを設定
        newTextView2.setPadding(10, 20, 10, 20);  // パディングを設定
        // TextView を LinearLayout に追加
        buttonContainer.addView(newTextView2);


        return root;
    }

    private Bundle hoge(String date, String jo) {
        // 渡したい値を用意する
        // Bundleを作成して値を詰める
        Bundle bundle = new Bundle();
        bundle.putString("key", date);
        bundle.putString("jo", jo);
        return bundle;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public static String getDayOfWeek(String dateStr) {
        try {
            // 8桁の日付フォーマットを定義
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(dateStr, formatter);

            // 曜日を取得
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            // 日本語の曜日名を返す
            switch (dayOfWeek) {
                case MONDAY:
                    return " (月)";
                case TUESDAY:
                    return " (火)";
                case WEDNESDAY:
                    return " (水)";
                case THURSDAY:
                    return " (木)";
                case FRIDAY:
                    return " (金)";
                case SATURDAY:
                    return " (土)";
                case SUNDAY:
                    return " (日)";
                default:
                    return "";
            }
        } catch (DateTimeParseException e) {
            return "日付の形式が正しくありません。";
        }
    }





}


