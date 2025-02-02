package com.tktkcompany.kakoRaceKeiba.ui.memo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tktkcompany.kakoRaceKeiba.MainActivity;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;

public class MemoCreateFragment extends Fragment {
    private MyDatabaseManager databaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memo_create, container, false);

        EditText titleInput = view.findViewById(R.id.edit_text_title);
        EditText contentInput = view.findViewById(R.id.edit_text_content);
        Button saveButton = view.findViewById(R.id.button_save);

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            String content = contentInput.getText().toString();

            // データベースマネージャーのインスタンス作成
            databaseManager = new MyDatabaseManager(getContext());
            // データベースをオープン
            databaseManager.open();
            // サンプルデータの挿入
            databaseManager.raceResultMemoInsertData(title, content);

            if (!title.isEmpty() && !content.isEmpty()) {
                // 新しいメモを保存 (簡略化のためメモリ上に保存)
                Memo newMemo = new Memo(title, content);
                // メモ一覧に戻る
                NavController navController = Navigation.findNavController(v);
                Bundle bundle = null;
                navController.navigate(R.id.action_navigation_memoCreate_to_navigation_memoLists, bundle);
            } else {
                Toast.makeText(getContext(), "Both fields are required", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}