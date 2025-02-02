package com.tktkcompany.kakoRaceKeiba.ui.memo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tktkcompany.kakoRaceKeiba.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tktkcompany.kakoRaceKeiba.MainActivity;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentMemoListBinding;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentRaceresultsBinding;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoListFragment extends Fragment {
    private MyDatabaseManager databaseManager;
    private List<Memo> memoList = new ArrayList<>();
    private MemoAdapter adapter;
    private FragmentMemoListBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memo_list, container, false);
        binding = FragmentMemoListBinding.inflate(inflater, container, false);

        // RecyclerViewのセットアップ
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // データベースマネージャーのインスタンス作成
        databaseManager = new MyDatabaseManager(getContext());
        databaseManager.open();
        // サンプルデータの取得
        List<Memo> list = databaseManager.getMemo();

        // サンプルデータ
        memoList.clear();  // 追加する前にリストをクリア
        for (int i = 0; i < list.size(); i++) {
            memoList.add(list.get(i));
        }

        // アダプターを設定
        adapter = new MemoAdapter(memoList, databaseManager, memo -> {});
        recyclerView.setAdapter(adapter);

        // 新規メモ作成ボタン
        Button createButton = view.findViewById(R.id.button_create_memo);
        createButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = null;
            navController.navigate(R.id.action_navigation_memoLists_to_navigation_memoCreate, bundle);
        });



        return view;
    }
}
