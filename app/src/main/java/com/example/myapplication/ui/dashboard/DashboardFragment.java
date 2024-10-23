package com.example.myapplication.ui.dashboard;


import android.graphics.Color;
import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentDashboardBinding;
import com.example.myapplication.db.MyDatabaseHelper;
import com.example.myapplication.db.MyDatabaseManager;
import com.example.myapplication.util.WeekendDays;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.List;

public class DashboardFragment extends Fragment {
    private MyDatabaseHelper dbHelper;

    private FragmentDashboardBinding binding;

    /**
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

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
        for (String date: datelist) {

            if (dbManager.getRaceResults(date, "1", "東京").size() != 0) {

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
                newButton.setText(date);
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
        for (String date: datelist) {
            if (dbManager.getRaceResults(date, "1", "京都").size() != 0) {
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
                newButton.setText(date);
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
        for (String date: datelist) {
            if (dbManager.getRaceResults(date, "1", "新潟").size() != 0) {
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
                newButton.setText(date);
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
        String valueToSend = date;
        String valueToSend2 = jo;
        // Bundleを作成して値を詰める
        Bundle bundle = new Bundle();
        bundle.putString("key", valueToSend);
        bundle.putString("jo", valueToSend2);
        return bundle;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}