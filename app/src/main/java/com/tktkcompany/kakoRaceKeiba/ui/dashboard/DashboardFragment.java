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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentDashboardBinding;
import com.tktkcompany.kakoRaceKeiba.db.FirebaseManager;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseHelper;

import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;


import java.util.List;

public class DashboardFragment extends Fragment {
    private MyDatabaseHelper dbHelper;

    private FragmentDashboardBinding binding;

    private final String TOKYO = "東京";
    private final String NAKAYAMA = "中山";
    private final String HUKUSIMA = "福島";
    private final String TYUKYO = "中京";
    private final String NIIGATA = "新潟";
    private LinearLayout buttonContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ボタンを追加するLinearLayoutの参照を取得
        buttonContainer = root.findViewById(R.id.button_container);

        // 各競馬場のリストを作成
        List<String> joNames = List.of(TOKYO, NAKAYAMA, HUKUSIMA, TYUKYO, NIIGATA);

        // 日付のリストを取得
        List<String> datelist = WeekendDays.getPastWeekendsInCurrentMonth();

        // 日付ごとに競馬場のクエリを順番に実行
        executeSequentialQueriesForAllLocations(datelist, joNames);

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


    private void executeSequentialQueriesForAllLocations(List<String> datelist, List<String> joNames) {
        Task<Void> sequence = Tasks.forResult(null); // 最初のタスクを空タスクで初期化

        // 競馬場ごとに処理を順次追加
        for (String joName : joNames) {
            sequence = sequence.continueWithTask(task -> {
                // ラベルを追加 (各競馬場ごとに)
                TextView textView = new TextView(getActivity());
                textView.setText(joName);
                textView.setTextSize(18);

                getActivity().runOnUiThread(() -> buttonContainer.addView(textView));

                // 日付ごとにクエリを実行
                return executeSequentialQueriesForDates(datelist, joName);
            });
        }

        // 全ての競馬場の処理が完了した後の処理（任意）
        sequence.addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
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

            } else {
                System.err.println("エラーが発生しました: " + task.getException());
            }
        });
    }

    private Task<Void> executeSequentialQueriesForDates(List<String> datelist, String joName) {
        Task<Void> sequence = Tasks.forResult(null); // 空タスクで初期化

        // 日付ごとに非同期タスクを順次実行
        for (String date : datelist) {
            sequence = sequence.continueWithTask(task -> queryDataAsTask(date, joName));
        }
        return sequence; // 全てのクエリが完了するタスクを返す
    }

    private Task<Void> queryDataAsTask(String date, String joName) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        FirebaseManager.queryData("raceResult" + "/" + joName + "/" + date, "tyaku", "1", new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String sRaceNo = childSnapshot.child("raceNo").getValue(String.class);
                    String sTyaku = childSnapshot.child("tyaku").getValue(String.class);

                    if ("1".equals(sTyaku) && "1".equals(sRaceNo)) {
                        fooMethod(joName, date);
                    }
                }
                // クエリが成功したらタスクを完了
                taskCompletionSource.setResult(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Query failed: " + error.getMessage());
                taskCompletionSource.setException(error.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    private void fooMethod(String joName, String date) {
        // ボタンを生成
        Button newButton = new Button(getActivity());
        newButton.setText(date + getDayOfWeek(date));
        newButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = hoge(date, joName);
            navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
        });

        // ボタンを追加
        getActivity().runOnUiThread(() -> buttonContainer.addView(newButton));
    }

}


