package com.tktkcompany.kakoRaceKeiba.ui.memo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.db.MyDatabaseManager;

public class MemoEditFragment extends Fragment {
    private Memo memo;
    private MyDatabaseManager databaseManager;
    private TextView editTitle, editContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memo_edit, container, false);
        editTitle = view.findViewById(R.id.edit_text_title);
        editContent = view.findViewById(R.id.edit_text_content);
        Button saveButton = view.findViewById(R.id.button_save_memo);

        databaseManager = new MyDatabaseManager(getContext());
        databaseManager.open();

        if (getArguments() != null) {
            memo = (Memo) getArguments().getSerializable("memo");
            editTitle.setText(memo.getTitle());
            editContent.setText(memo.getContent());
        }

        saveButton.setOnClickListener(v -> updateMemo());

        return view;
    }

    private void updateMemo() {
        String newTitle = editTitle.getText().toString();
        String newContent = editContent.getText().toString();

        if (!newTitle.isEmpty()) {
            memo.setTitle(newTitle);
            memo.setContent(newContent);
            databaseManager.updateMemo(memo);
            Toast.makeText(getContext(), "メモを更新しました", Toast.LENGTH_SHORT).show();

            // 一覧に戻る
            Navigation.findNavController(requireView()).popBackStack();
        } else {
            Toast.makeText(getContext(), "タイトルは必須です", Toast.LENGTH_SHORT).show();
        }
    }
}

